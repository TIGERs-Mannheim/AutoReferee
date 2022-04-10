/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.visualizer.field;

import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.drawable.ShapeMapSource;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.views.ISumatraPresenter;
import edu.tigers.sumatra.visualizer.field.coordinates.CoordinatesMouseAdapter;
import edu.tigers.sumatra.visualizer.field.interaction.DragMouseAdapter;
import edu.tigers.sumatra.visualizer.field.interaction.ZoomMouseAdapter;
import edu.tigers.sumatra.visualizer.field.ruler.RulerMouseAdapter;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@RequiredArgsConstructor
public class VisualizerFieldPresenter implements ISumatraPresenter, IWorldFrameObserver
{
	@Getter
	private final FieldPanel fieldPanel = new FieldPanel();

	@Getter
	private final Map<ShapeMapSource, ShapeMap> shapeMaps = new ConcurrentHashMap<>();
	private final List<MouseAdapter> mouseAdapters = List.of(
			new CoordinatesMouseAdapter(fieldPanel),
			new RulerMouseAdapter(fieldPanel),
			new ZoomMouseAdapter(fieldPanel),
			new DragMouseAdapter(fieldPanel),
			new InteractionMouseEvents()
	);

	@Getter
	private final List<FieldMouseInteraction> onFieldClicks = new ArrayList<>();
	@Getter
	private final List<FieldMouseInteraction> onMouseMoves = new ArrayList<>();


	@Override
	public void onStart()
	{
		ISumatraPresenter.super.onStart();
		mouseAdapters.forEach(fieldPanel::addMouseAdapter);
		fieldPanel.setPanelVisible(true);
	}


	@Override
	public void onStop()
	{
		ISumatraPresenter.super.onStop();
		mouseAdapters.forEach(fieldPanel::removeMouseAdapter);
		fieldPanel.setPanelVisible(false);
		fieldPanel.clearField();
		shapeMaps.clear();
	}


	public void update()
	{
		shapeMaps.forEach(this::newShapeMap);
		fieldPanel.paintOffline();
	}


	private void newShapeMap(final ShapeMapSource source, final ShapeMap shapeMap)
	{
		fieldPanel.setShapeMap(source, shapeMap);
	}


	@Override
	public void onNewShapeMap(final long timestamp, final ShapeMap shapeMap, final ShapeMapSource source)
	{
		shapeMaps.put(source, shapeMap);
	}


	@Override
	public void onRemoveSourceFromShapeMap(final String source)
	{
		shapeMaps.keySet().stream()
				.filter(k -> k.getName().equals(source))
				.forEach(k -> shapeMaps.put(k, new ShapeMap()));
	}


	@Override
	public void onRemoveCategoryFromShapeMap(final String... category)
	{
		List<String> categories = Arrays.asList(category);
		shapeMaps.keySet().stream()
				.filter(k -> k.getCategories().containsAll(categories))
				.forEach(k -> shapeMaps.put(k, new ShapeMap()));
	}


	private class InteractionMouseEvents extends MouseAdapter
	{
		@Override
		public void mouseClicked(final MouseEvent e)
		{
			IVector2 guiPos = fieldPanel.getFieldPos(e.getX(), e.getY());
			IVector2 globalPos = fieldPanel.transformToGlobalCoordinates(guiPos);
			onFieldClicks.forEach(c -> c.onInteraction(globalPos, e));
		}


		@Override
		public void mouseMoved(final MouseEvent e)
		{
			IVector2 guiPos = fieldPanel.getFieldPos(e.getX(), e.getY());
			IVector2 lastMousePoint = fieldPanel.transformToGlobalCoordinates(guiPos);
			onMouseMoves.forEach(c -> c.onInteraction(lastMousePoint, e));
		}
	}

	@FunctionalInterface
	public interface FieldMouseInteraction
	{
		void onInteraction(IVector2 pos, MouseEvent e);
	}
}
