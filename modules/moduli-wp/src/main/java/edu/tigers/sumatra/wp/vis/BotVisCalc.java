/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.vis;

import java.awt.Color;
import java.util.List;

import edu.tigers.sumatra.drawable.DrawableBotShape;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.EAiType;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BotVisCalc implements IWpCalc
{
	@Override
	public void process(final WorldFrameWrapper wfw, final ShapeMap shapeMap)
	{
		List<IDrawableShape> shapes = shapeMap.get(EWpShapesLayer.BOTS);
		for (ITrackedBot bot : wfw.getSimpleWorldFrame().getBots().values())
		{
			DrawableBotShape shape = new DrawableBotShape(bot.getPos(), bot.getOrientation(), Geometry.getBotRadius(),
					bot.getCenter2DribblerDist());
			Color color = bot.getTeamColor().getColor();
			if (!bot.isVisible())
			{
				color = new Color(color.getRed(), color.getGreen(), color.getBlue(), 150);
			}
			if (bot.getRobotInfo().getAiType() == EAiType.PRIMARY)
			{
				shape.setBorderColor(Color.black);
			} else
			{
				shape.setBorderColor(null);
			}
			shape.setFillColor(color);
			shape.setFontColor(bot.getTeamColor() == ETeamColor.YELLOW ? Color.black : Color.white);
			shape.setId(String.valueOf(bot.getBotId().getNumber()));
			shapes.add(shape);
		}
	}
	
}
