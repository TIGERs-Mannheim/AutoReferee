/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.visualizer.field;

import edu.tigers.sumatra.clock.FpsCounter;
import edu.tigers.sumatra.drawable.DrawableFieldBackground;
import edu.tigers.sumatra.drawable.EFieldTurn;
import edu.tigers.sumatra.drawable.EFontSize;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.drawable.IDrawableTool;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.drawable.ShapeMapSource;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.util.ScalingUtil;
import edu.tigers.sumatra.visualizer.field.coordinates.Coordinates;
import edu.tigers.sumatra.visualizer.field.recorder.EMediaOption;
import edu.tigers.sumatra.visualizer.field.recorder.VideoExporter;
import edu.tigers.sumatra.visualizer.field.ruler.Ruler;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import net.miginfocom.swing.MigLayout;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serial;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;


/**
 * Visualization of the field.
 */
@Log4j2
public class FieldPanel extends JPanel implements IDrawableTool
{
	@Serial
	private static final long serialVersionUID = 4330620225157027091L;

	private static final Color FIELD_COLOR = new Color(0, 160, 30);
	private static final Color FIELD_COLOR_DARK = new Color(77, 77, 77);
	private static final Color FIELD_COLOR_REFEREE = new Color(93, 93, 93);

	private transient Image offImage = null;
	private final transient Object offImageSync = new Object();


	// --- field constants / size of the loaded image in pixel ---
	private static final int DEF_FIELD_WIDTH = 10000;
	private static final double BORDER_TEXT_NORMALIZED_WIDTH = 750;

	private final int fieldWidth;
	private double fieldGlobalWidth = Geometry.getFieldWidth();
	private double fieldGlobalLength = Geometry.getFieldLength();
	private double fieldGlobalBoundaryWidth = Geometry.getBoundaryWidth();

	// --- field scrolling ---
	@Setter
	private double scaleFactor = 1;
	@Setter
	private double fieldOriginY = 0;
	@Setter
	private double fieldOriginX = 0;

	@Setter
	private boolean fancyPainting = false;
	@Setter
	private boolean darkMode = false;

	private final Set<String> showSources = new ConcurrentSkipListSet<>();
	private final Set<String> showCategories = new ConcurrentSkipListSet<>();
	private final transient Map<ShapeMapSource, ShapeMap> shapeMaps = new ConcurrentHashMap<>();
	private final Map<String, Boolean> shapeVisibilityMap = new ConcurrentHashMap<>();

	@Setter
	@Getter
	private EFieldTurn fieldTurn = EFieldTurn.NORMAL;

	private final transient FpsCounter fpsCounter = new FpsCounter();

	private transient Path takeScreenshotToPath;
	private EMediaOption mediaOption = EMediaOption.CURRENT_SECTION;
	private int snapshotWidthBase = 5000;
	private int snapshotHeightBase = 5000;
	private int snapshotWidth = 5000;
	private int snapshotHeight = 5000;
	private int adjustedWidth = 5000;
	private int adjustedHeight = 5000;

	private transient VideoExporter videoExporter = null;
	private long recordingAnimation = 0;

	@Setter
	private transient Ruler ruler = null;
	@Setter
	private transient List<Coordinates> coordinates = List.of();


	public FieldPanel()
	{
		this.fieldWidth = DEF_FIELD_WIDTH;

		setBorder(new EmptyBorder(0, 0, 0, 0));
		setLayout(new MigLayout("inset 0"));

		String strFieldTurn = SumatraModel.getInstance().getUserProperty(
				FieldPanel.class.getCanonicalName() + ".fieldTurn");
		if (strFieldTurn != null)
		{
			try
			{
				setFieldTurn(EFieldTurn.valueOf(strFieldTurn));
			} catch (IllegalArgumentException err)
			{
				log.error("Could not parse field turn.", err);
			}
		}
	}


	public IVector2 getFieldPos(int x, int y)
	{
		return Vector2.fromXY(x - fieldOriginX, y - fieldOriginY).multiply(1f / scaleFactor);
	}


	public void setShapeMap(final ShapeMapSource source, final ShapeMap shapeMap)
	{
		if (shapeMap == null)
		{
			this.shapeMaps.remove(source);
		} else
		{
			this.shapeMaps.put(source, shapeMap);
		}
	}


	public void addMouseAdapter(MouseAdapter mouseAdapter)
	{
		addMouseListener(mouseAdapter);
		addMouseMotionListener(mouseAdapter);
		addMouseWheelListener(mouseAdapter);
	}


	public void removeMouseAdapter(MouseAdapter mouseAdapter)
	{
		removeMouseListener(mouseAdapter);
		removeMouseMotionListener(mouseAdapter);
		removeMouseWheelListener(mouseAdapter);
	}


	@Override
	public void paint(final Graphics g1)
	{
		if (offImage != null)
		{
			synchronized (offImageSync)
			{
				g1.drawImage(offImage, 0, 0, this);
			}
		} else
		{
			g1.clearRect(0, 0, adjustedWidth, adjustedHeight);
		}
	}


	public void paintOffline()
	{
		adjustedWidth = getWidth();
		adjustedHeight = getHeight();

		/*
		 * Drawing only makes sense if we have a valid/existent drawing area because creating an image with size 0 will
		 * produce an error. This scenario is possible if the moduli start up before the GUI layouting has been completed
		 * and the component size is still 0|0.
		 */
		if ((adjustedWidth < 20) || (adjustedHeight < 20))
		{
			return;
		}

		if ((offImage == null) || (offImage.getHeight(this) != adjustedHeight)
				|| (offImage.getWidth(this) != adjustedWidth))
		{
			offImage = createImage(adjustedWidth, adjustedHeight);
			resetField();
		}

		synchronized (offImageSync)
		{
			final Graphics2D g2 = (Graphics2D) offImage.getGraphics();
			drawFieldGraphics(g2, this.fieldOriginX, this.fieldOriginY, this.adjustedWidth,
					this.adjustedHeight, this.scaleFactor, EMediaOption.VISUALIZER);
		}

		processScreenshot();
		processVideoRecording();
		repaint();
	}


	private void processScreenshot()
	{
		if (takeScreenshotToPath != null)
		{
			BufferedImage image = takeScreenshot();
			storeScreenshot(image, takeScreenshotToPath);
			takeScreenshotToPath = null;
		}
	}


	private BufferedImage takeScreenshot()
	{
		Image image = createImage(this.snapshotWidth, this.snapshotHeight);
		Graphics2D g2 = (Graphics2D) image.getGraphics();
		switch (mediaOption)
		{
			case FULL_FIELD -> drawFullFieldScreenshot(g2);
			case CURRENT_SECTION -> drawCurrentSectionScreenshot(g2);
			default -> {
				// nothing to do
			}
		}
		return toBufferedImage(image);
	}


	private void storeScreenshot(BufferedImage screenshotImage, Path path)
	{
		try
		{
			ImageIO.write(screenshotImage, "png", path.toFile());
			log.info("Finished saving screenshot to: " + path);
		} catch (IOException e)
		{
			log.warn("Could not take Screenshot", e);
		}
	}


	private void processVideoRecording()
	{
		if (videoExporter == null)
		{
			return;
		}
		try
		{
			BufferedImage image = takeScreenshot();
			videoExporter.addImageToVideo(image);
		} catch (OutOfMemoryError error)
		{
			log.error("Out of Memory, stopping recording of Video", error);
			stopRecordingVideo();
		}
	}


	private void calculateAndSetScreenshotDimensions()
	{
		int width = this.snapshotWidthBase;
		int height = this.snapshotHeightBase;
		EFieldTurn turn = getFieldTurn(width, height);
		double widthToHeightRatio;
		if (turn == EFieldTurn.NORMAL || turn == EFieldTurn.T180)
		{
			widthToHeightRatio = getFieldTotalHeight() / (double) getFieldTotalWidth();
		} else
		{
			widthToHeightRatio = getFieldTotalWidth() / (double) getFieldTotalHeight();
		}
		if (this.snapshotWidth <= 0 && this.snapshotHeight <= 0)
		{
			log.warn("Invalid screenshot size, taking image with default values");
			width = 1024;
			height = (int) (width * widthToHeightRatio);
		}
		if (this.snapshotWidth <= 0)
		{
			width = (int) (height / widthToHeightRatio);
		}
		if (this.snapshotHeight <= 0)
		{
			height = (int) (width * widthToHeightRatio);
		}

		double borderTextScale = calculateBorderTextScale(mediaOption);
		int refAreaOffset = calculateRefAreaOffset(mediaOption, borderTextScale);
		height += refAreaOffset;

		// numbers have to always be divisible by 2 for media encoding
		this.snapshotWidth = width + (width % 2);
		this.snapshotHeight = height + (height % 2);
	}


	private void drawFullFieldScreenshot(final Graphics2D g2)
	{
		double scale = getFieldScale(this.snapshotWidth, this.snapshotHeight);
		drawFieldGraphics(g2, 0, 0, this.snapshotWidth, this.snapshotHeight, scale,
				EMediaOption.FULL_FIELD);
	}


	private void drawCurrentSectionScreenshot(final Graphics2D g2)
	{
		final Point mPoint = new Point(0, 0);
		final double xLen = ((mPoint.x - fieldOriginX) / scaleFactor) * 2;
		final double yLen = ((mPoint.y - fieldOriginY) / scaleFactor) * 2;
		final double oldLenX = (xLen) * scaleFactor;
		final double oldLenY = (yLen) * scaleFactor;
		double normalizedScale = scaleFactor * Math
				.min(this.snapshotWidth / (double) getWidth(), this.snapshotHeight / (double) getHeight());
		final double newLenX = (xLen) * normalizedScale;
		final double newLenY = (yLen) * normalizedScale;
		double orgX = (fieldOriginX - ((newLenX - oldLenX) / 2.0));
		double orgY = (fieldOriginY - ((newLenY - oldLenY) / 2.0));
		drawFieldGraphics(g2, orgX, orgY, this.snapshotWidth, this.snapshotHeight,
				normalizedScale, EMediaOption.CURRENT_SECTION);
	}


	private void drawFieldGraphics(final Graphics2D g2,
			double offsetX, double offsetY,
			int width, int height, double scale,
			EMediaOption mediaOption)
	{
		final BasicStroke defaultStroke = new BasicStroke(Math.max(1, scaleYLength(10)));
		g2.setColor(FIELD_COLOR_REFEREE);
		g2.fillRect(0, 0, width, height);

		double borderTextScale = calculateBorderTextScale(mediaOption);
		int refAreaOffset = calculateRefAreaOffset(mediaOption, borderTextScale);

		g2.translate(offsetX, offsetY + refAreaOffset);
		g2.scale(scale, scale);

		EFieldTurn oldTurn = getFieldTurn();
		if (mediaOption != EMediaOption.VISUALIZER)
		{
			setFieldTurn(getFieldTurn(width, height));
		}

		if (fancyPainting)
		{
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		}

		shapeMaps.values().stream()
				.flatMap(m -> m.getAllShapeLayers().stream())
				.flatMap(l -> l.getShapes().stream())
				.filter(s -> s.getClass().equals(DrawableFieldBackground.class))
				.findAny()
				.map(DrawableFieldBackground.class::cast)
				.ifPresent(s -> {
					fieldGlobalBoundaryWidth = s.getBoundaryWidth();
					fieldGlobalLength = s.getFieldWithBorder().xExtent() - 2 * fieldGlobalBoundaryWidth;
					fieldGlobalWidth = s.getFieldWithBorder().yExtent() - 2 * fieldGlobalBoundaryWidth;
				});

		shapeMaps.entrySet().stream()
				.filter(s -> showSources.contains(s.getKey().getName()))
				.filter(s -> showCategories.containsAll(s.getKey().getCategories()))
				.map(Map.Entry::getValue)
				.map(ShapeMap::getAllShapeLayers)
				.flatMap(Collection::stream)
				.sorted()
				.filter(e -> shapeVisibilityMap.getOrDefault(e.getIdentifier().getId(), true))
				.forEach(shapeLayer -> paintShapeMap(g2, shapeLayer, defaultStroke));

		Optional.ofNullable(ruler).ifPresent(r -> r.paintShape(g2, this, false));

		g2.scale(1.0 / scale, 1.0 / scale);
		g2.translate(-offsetX, -offsetY - refAreaOffset);

		if (mediaOption != EMediaOption.VISUALIZER)
		{
			g2.setColor(FIELD_COLOR_REFEREE);
			g2.fillRect(0, 0, width, refAreaOffset);
		}

		g2.scale(borderTextScale, borderTextScale);

		shapeMaps.entrySet().stream()
				.filter(s -> showSources.contains(s.getKey().getName()))
				.filter(s -> showCategories.containsAll(s.getKey().getCategories()))
				.map(Map.Entry::getValue)
				.map(ShapeMap::getAllShapeLayers)
				.flatMap(Collection::stream)
				.sorted()
				.filter(e -> shapeVisibilityMap.getOrDefault(e.getIdentifier().getId(), true))
				.forEach(shapeLayer -> paintShapeMapBorderText(g2, shapeLayer, defaultStroke));

		g2.scale(1 / borderTextScale, 1 / borderTextScale);

		if (mediaOption == EMediaOption.VISUALIZER)
		{
			coordinates.forEach(c -> c.paintShape(g2, this, false));

			if (videoExporter != null)
			{
				g2.setColor(Color.red);
				int recordingRadius = 15 + (int) ((Math.sin(recordingAnimation / 7.0)) * 10);
				int recX = getWidth() - 50 - recordingRadius / 2;
				int recY = 15 - recordingRadius / 2;
				g2.fillOval(recX, recY, recordingRadius, recordingRadius);

				final BasicStroke newStroke = new BasicStroke(Math.max(1, scaleYLength(15)));

				g2.setStroke(newStroke);
				g2.drawString("REC", getWidth() - 50 + 17, 15 + 4);
				g2.setStroke(defaultStroke);
				recordingAnimation++;
			} else
			{
				paintFps(g2, width);
			}
		}
		setFieldTurn(oldTurn);
	}


	private int calculateRefAreaOffset(final EMediaOption mediaOption, final double borderTextScale)
	{
		return mediaOption == EMediaOption.VISUALIZER ? 0 : ((int) (70 * borderTextScale));
	}


	private double calculateBorderTextScale(final EMediaOption mediaOption)
	{
		int relevantViewWidth = mediaOption == EMediaOption.VISUALIZER ? getWidth() : this.snapshotWidth;
		return relevantViewWidth / BORDER_TEXT_NORMALIZED_WIDTH;
	}


	private void paintShapeMapBorderText(Graphics2D g, ShapeMap.ShapeLayer shapeLayer, BasicStroke defaultStroke)
	{
		final Graphics2D gDerived = (Graphics2D) g.create();
		gDerived.setStroke(defaultStroke);
		shapeLayer.getShapes().stream()
				.filter(IDrawableShape::isBorderText)
				.forEach(s -> s.paintShape(gDerived, this, shapeLayer.isInverted()));
		gDerived.dispose();
	}


	private void paintShapeMap(Graphics2D g, ShapeMap.ShapeLayer shapeLayer, BasicStroke defaultStroke)
	{
		final Graphics2D gDerived = (Graphics2D) g.create();
		gDerived.setStroke(defaultStroke);
		shapeLayer.getShapes().stream()
				.filter(e -> !e.isBorderText())
				.forEach(s -> s.paintShape(gDerived, this, shapeLayer.isInverted()));
		gDerived.dispose();
	}


	private void paintFps(final Graphics2D g, final int width)
	{
		fpsCounter.newFrame(System.nanoTime());
		int fontSize = ScalingUtil.getFontSize(EFontSize.SMALL);
		g.setFont(new Font("", Font.PLAIN, fontSize));
		g.setColor(Color.black);

		int x = width - fontSize * 3;
		int y = 20;
		g.drawString(String.format("%.1f", fpsCounter.getAvgFps()), x, y);
	}


	public void clearField()
	{
		offImage = null;
		shapeMaps.clear();
	}


	@Override
	public void turnField(final EFieldTurn fieldTurn, final double angle, final Graphics2D g2)
	{
		double translateSize = ((double) getFieldHeight() - fieldWidth) / 2.0;
		if (angle > 0)
		{
			switch (fieldTurn)
			{
				case T270 -> g2.translate(translateSize, translateSize);
				case T90 -> g2.translate(-translateSize, -translateSize);
				default -> {
					// nothing to do
				}
			}
		}

		long numRotations = Math.round(fieldTurn.getAngle() / AngleMath.PI_HALF);
		g2.rotate(numRotations * angle, getFieldTotalWidth() / 2.0, getFieldTotalHeight() / 2.0);

		if (angle < 0)
		{
			switch (fieldTurn)
			{
				case T270 -> g2.translate(-translateSize, -translateSize);
				case T90 -> g2.translate(translateSize, translateSize);
				default -> {
					// nothing to do
				}
			}
		}
	}


	private IVector2 turnGuiPoint(final EFieldTurn fieldTurn, final IVector2 point)
	{
		return switch (fieldTurn)
				{
					case NORMAL -> point;
					case T90 -> Vector2.fromXY(point.y(), getFieldTotalWidth() - point.x());
					case T180 -> Vector2.fromXY(-point.x() + getFieldTotalWidth(), -point.y() + getFieldTotalHeight());
					case T270 -> Vector2.fromXY(-point.y() + getFieldTotalHeight(), point.x());
				};
	}


	/**
	 * Transforms a global(field)position into a gui position.
	 *
	 * @param globalPosition
	 * @return guiPosition
	 */
	private IVector2 transformToGuiCoordinates(final IVector2 globalPosition)
	{
		final double yScaleFactor = fieldWidth / fieldGlobalWidth;
		final double xScaleFactor = getFieldHeight() / fieldGlobalLength;

		final IVector2 transPosition = globalPosition.addNew(Vector2.fromXY(fieldGlobalLength / 2,
				fieldGlobalWidth / 2.0));

		double y = transPosition.x() * xScaleFactor + fieldGlobalBoundaryWidth * xScaleFactor;
		double x = transPosition.y() * yScaleFactor + fieldGlobalBoundaryWidth * yScaleFactor;

		return turnGuiPoint(fieldTurn, Vector2.fromXY(x, y));
	}


	@Override
	public IVector2 transformToGuiCoordinates(final IVector2 globalPosition, final boolean invert)
	{
		int r = 1;
		if (invert)
		{
			r = -1;
		}
		return transformToGuiCoordinates(Vector2.fromXY(r * globalPosition.x(), r * globalPosition.y()));
	}


	/**
	 * Transforms a gui position into a global(field)position.
	 *
	 * @param guiPosition
	 * @return globalPosition
	 */
	public IVector2 transformToGlobalCoordinates(final IVector2 guiPosition)
	{
		IVector2 guiPosTurned = turnGlobalPoint(fieldTurn, guiPosition);

		final double xScaleFactor = fieldGlobalWidth / fieldWidth;
		final double yScaleFactor = fieldGlobalLength / getFieldHeight();

		final IVector2 transPosition = guiPosTurned.subtractNew(
				Vector2.fromXY(fieldGlobalBoundaryWidth / xScaleFactor, fieldGlobalBoundaryWidth / yScaleFactor));

		double x = (transPosition.y() * yScaleFactor) - fieldGlobalLength / 2.0;
		double y = (transPosition.x() * xScaleFactor) - fieldGlobalWidth / 2.0;

		return Vector2.fromXY(x, y);
	}


	private IVector2 turnGlobalPoint(final EFieldTurn fieldTurn, final IVector2 point)
	{
		final int width = getFieldTotalWidth();
		final int height = getFieldTotalHeight();
		return switch (fieldTurn)
				{
					case NORMAL -> point;
					case T90 -> Vector2.fromXY(width - point.y(), point.x());
					case T180 -> Vector2.fromXY(-point.x() + width, -point.y() + height);
					case T270 -> Vector2.fromXY(point.y(), -point.x() + height);
				};
	}


	public void scale(Point point, double scroll)
	{
		final double xLen = ((point.x - fieldOriginX) / scaleFactor) * 2;
		final double yLen = ((point.y - fieldOriginY) / scaleFactor) * 2;

		final double oldLenX = (xLen) * scaleFactor;
		final double oldLenY = (yLen) * scaleFactor;
		setScaleFactor(scaleFactor * scroll);
		final double newLenX = (xLen) * scaleFactor;
		final double newLenY = (yLen) * scaleFactor;
		setFieldOriginX(fieldOriginX - ((newLenX - oldLenX) / 2.0));
		setFieldOriginY(fieldOriginY - ((newLenY - oldLenY) / 2.0));
	}


	@Override
	public int scaleXLength(final double length)
	{
		final double xScaleFactor = getFieldHeight() / fieldGlobalLength;
		return (int) (length * xScaleFactor);
	}


	@Override
	public int scaleYLength(final double length)
	{
		final double yScaleFactor = fieldWidth / fieldGlobalWidth;
		return (int) (length * yScaleFactor);
	}


	public void setPanelVisible(final boolean visible)
	{
		setVisible(visible);
	}


	private int getFieldTotalWidth()
	{
		final double yScaleFactor = getFieldWidth() / fieldGlobalWidth;
		return getFieldWidth() + (int) ((2 * fieldGlobalBoundaryWidth) * yScaleFactor);
	}


	private int getFieldTotalHeight()
	{
		final double xScaleFactor = getFieldHeight() / fieldGlobalLength;
		return getFieldHeight() + (int) ((2 * fieldGlobalBoundaryWidth) * xScaleFactor);
	}


	private double getFieldRatio()
	{
		return fieldGlobalLength / fieldGlobalWidth;
	}


	@Override
	public final int getFieldHeight()
	{
		return (int) Math.round(getFieldRatio() * fieldWidth);
	}


	@Override
	public final int getFieldWidth()
	{
		return fieldWidth;
	}


	public void setShapeLayerVisibility(final String layerId, final boolean visible)
	{
		shapeVisibilityMap.put(layerId, visible);
	}


	public void setSourceVisibility(final String source, final boolean visible)
	{
		if (visible)
		{
			showSources.add(source);
		} else
		{
			showSources.remove(source);
		}
	}


	public void setSourceCategoryVisibility(final String category, final boolean visible)
	{
		if (visible)
		{
			showCategories.add(category);
		} else
		{
			showCategories.remove(category);
		}
	}


	public void turnNext()
	{
		switch (fieldTurn)
		{
			case NORMAL -> setFieldTurn(EFieldTurn.T90);
			case T90 -> setFieldTurn(EFieldTurn.T180);
			case T180 -> setFieldTurn(EFieldTurn.T270);
			case T270 -> setFieldTurn(EFieldTurn.NORMAL);
		}
		fieldOriginX = 0;
		fieldOriginY = 0;
	}


	private double getFieldScale(int width, int height)
	{
		double heightScaleFactor;
		double widthScaleFactor;
		if (width > height)
		{
			heightScaleFactor = (double) height / getFieldTotalWidth();
			widthScaleFactor = (double) width / getFieldTotalHeight();
		} else
		{
			heightScaleFactor = ((double) height) / getFieldTotalHeight();
			widthScaleFactor = ((double) width) / getFieldTotalWidth();
		}
		return Math.min(heightScaleFactor, widthScaleFactor);
	}


	private EFieldTurn getFieldTurn(int width, int height)
	{
		if (width > height)
		{
			return EFieldTurn.T90;
		}
		return EFieldTurn.NORMAL;
	}


	public void resetField()
	{
		setFieldTurn(getFieldTurn(adjustedWidth, adjustedHeight));
		double scale = getFieldScale(adjustedWidth, adjustedHeight);
		setScaleFactor(scale);
		setFieldOriginX(0);
		setFieldOriginY(0);
	}


	public void dragField(int dx, int dy)
	{
		setFieldOriginX(fieldOriginX + dx);
		setFieldOriginY(fieldOriginY + dy);
	}


	public void saveScreenshot(Path path)
	{
		this.takeScreenshotToPath = path;
	}


	public boolean startRecordingVideo(Path filePath)
	{
		try
		{
			VideoExporter newVideoExporter = new VideoExporter(filePath, this.snapshotWidth, this.snapshotHeight);
			videoExporter = newVideoExporter;
			return true;
		} catch (IOException e)
		{
			log.error("Could not record video", e);
			return false;
		}
	}


	public void stopRecordingVideo()
	{
		if (videoExporter != null)
		{
			videoExporter.stop();
			videoExporter = null;
		}
	}


	public void setMediaParameters(final int w, final int h, final EMediaOption mediaOption)
	{
		this.snapshotWidthBase = w;
		this.snapshotHeightBase = h;
		this.snapshotWidth = w;
		this.snapshotHeight = h;
		this.mediaOption = mediaOption;
		if (getFieldTurn() == EFieldTurn.NORMAL && mediaOption == EMediaOption.CURRENT_SECTION)
		{
			this.snapshotWidthBase = h;
			this.snapshotHeightBase = w;
			this.snapshotWidth = h;
			this.snapshotHeight = w;
		}
		calculateAndSetScreenshotDimensions();
	}


	private BufferedImage toBufferedImage(Image img)
	{
		if (img instanceof BufferedImage bufferedImage)
		{
			return bufferedImage;
		}

		// Create a buffered image with transparency
		BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

		// Draw the image on to the buffered image
		Graphics2D bGr = bimage.createGraphics();
		bGr.drawImage(img, 0, 0, null);
		bGr.dispose();

		// Return the buffered image
		return bimage;
	}


	@Override
	public int getFieldMargin()
	{
		final double yScaleFactor = fieldWidth / fieldGlobalWidth;
		return (int) (fieldGlobalBoundaryWidth * yScaleFactor);
	}


	@Override
	public Color getFieldColor()
	{
		return darkMode ? FIELD_COLOR_DARK : FIELD_COLOR;
	}
}
