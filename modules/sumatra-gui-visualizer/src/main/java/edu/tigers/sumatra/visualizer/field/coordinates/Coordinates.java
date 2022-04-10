/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.visualizer.field.coordinates;

import edu.tigers.sumatra.drawable.EFontSize;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.drawable.IDrawableTool;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.util.ScalingUtil;
import lombok.RequiredArgsConstructor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.text.DecimalFormat;


@RequiredArgsConstructor
public class Coordinates implements IDrawableShape
{
	private static final int FONT_SIZE = ScalingUtil.getFontSize(EFontSize.LARGE);
	private static final DecimalFormat DF = new DecimalFormat("#####");
	private final IVector2 point;
	private final ETeamColor teamColor;
	private final Dimension canvasSize;


	@Override
	public void paintShape(Graphics2D g, IDrawableTool tool, boolean invert)
	{
		g.setStroke(new BasicStroke());
		g.setFont(new Font("", Font.PLAIN, FONT_SIZE));
		g.setColor(getColor());

		int y = canvasSize.height - (int) (FONT_SIZE * 1.5);
		int x = getX();

		g.drawString("x:" + DF.format(point.x()), x, y);
		g.drawString("y:" + DF.format(point.y()), x, y + FONT_SIZE + 1);
	}


	private Color getColor()
	{
		return switch (teamColor)
				{
					case YELLOW -> Color.YELLOW;
					case BLUE -> Color.BLUE;
					default -> Color.WHITE;
				};
	}


	private int getX()
	{
		if (teamColor == ETeamColor.YELLOW)
		{
			return 10;
		} else if (teamColor == ETeamColor.BLUE)
		{
			return canvasSize.width - (FONT_SIZE * 5);
		}
		return canvasSize.width / 2 - (FONT_SIZE * 5);
	}
}
