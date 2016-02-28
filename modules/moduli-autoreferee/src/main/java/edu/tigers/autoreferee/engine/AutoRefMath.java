/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 14, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine;

import java.util.Collection;
import java.util.stream.Stream;

import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.shapes.rectangle.Rectangle;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.PenaltyArea;


/**
 * Encapsulates static math operations for the autoref rules
 * 
 * @author "Lukas Magel"
 */
public class AutoRefMath
{
	/**  */
	public static double	THROW_IN_DISTANCE						= 100;
	/**  */
	public static double	GOAL_KICK_DISTANCE					= 500;
	/** Minimum distance between the goal line and the kick position during a freekick */
	public static double	DEFENSE_AREA_GOALLINE_DISTANCE	= 600;
	
	/** mm */
	public static double	OFFENSE_FREEKICK_DISTANCE			= 700;
	
	
	/**
	 * Determines the corner kick position that is located closest to the specified position
	 * 
	 * @param pos
	 * @return
	 */
	public static IVector2 getClosestCornerKickPos(final IVector2 pos)
	{
		IVector2 corner = NGeometry.getClosestCorner(pos);
		int ySide = (corner.y() > 0 ? -1 : 1);
		int xSide = (corner.x() > 0 ? -1 : 1);
		return corner.addNew(new Vector2(xSide * THROW_IN_DISTANCE, ySide * THROW_IN_DISTANCE));
	}
	
	
	/**
	 * Determines the corner kick position that is located closest to the specified position
	 * 
	 * @param pos
	 * @return
	 */
	public static IVector2 getClosestGoalKickPos(final IVector2 pos)
	{
		IVector2 corner = NGeometry.getClosestCorner(pos);
		int xSide = (corner.x() > 0 ? -1 : 1);
		int ySide = (corner.y() > 0 ? -1 : 1);
		return corner.addNew(new Vector2(xSide * GOAL_KICK_DISTANCE, ySide * THROW_IN_DISTANCE));
	}
	
	
	/**
	 * This method checks if the specified position is located closer than {@value AutoRefMath#OFFENSE_FREEKICK_DISTANCE}
	 * to the defense area.
	 * If the freekick is to be executed by the attacking team then the ball is positioned at the
	 * closest point that is located {@value AutoRefMath#OFFENSE_FREEKICK_DISTANCE} from the defense area.
	 * If the freekick is to be executed by the defending team then the ball is positioned at one of the two corner
	 * points of the field which are located 600 mm from the goal line and 100 mm from the side line.
	 * 
	 * @param pos
	 * @param kickerColor
	 * @return
	 */
	public static IVector2 getClosestFreekickPos(final IVector2 pos, final ETeamColor kickerColor)
	{
		Rectangle field = NGeometry.getField();
		ETeamColor goalColor = NGeometry.getTeamOfClosestGoalLine(pos);
		IVector2 newKickPos;
		if (goalColor == kickerColor)
		{
			newKickPos = getDefenseKickPos(pos);
		}
		newKickPos = getOffenseKickPos(pos);
		
		/*
		 * Check if the ball is located too close to the touch lines or goal lines
		 */
		int xSide = newKickPos.x() > 0 ? 1 : -1;
		int ySide = newKickPos.y() > 0 ? 1 : -1;
		
		if (Math.abs(newKickPos.x()) > ((field.getxExtend() / 2) - THROW_IN_DISTANCE))
		{
			double newXPos = ((field.getxExtend() / 2) - THROW_IN_DISTANCE) * xSide;
			newKickPos = new Vector2(newXPos, newKickPos.y());
		}
		
		if (Math.abs(newKickPos.y()) > ((field.getyExtend() / 2) - THROW_IN_DISTANCE))
		{
			double newYPos = ((field.getyExtend() / 2) - THROW_IN_DISTANCE) * ySide;
			newKickPos = new Vector2(newKickPos.x(), newYPos);
		}
		
		return newKickPos;
	}
	
	
	private static IVector2 getOffenseKickPos(final IVector2 pos)
	{
		PenaltyArea penArea = NGeometry.getPenaltyArea(NGeometry.getTeamOfClosestGoalLine(pos));
		
		if (penArea.isPointInShape(pos, OFFENSE_FREEKICK_DISTANCE))
		{
			return penArea.nearestPointOutside(pos, OFFENSE_FREEKICK_DISTANCE);
		}
		return pos;
	}
	
	
	private static IVector2 getDefenseKickPos(final IVector2 pos)
	{
		int xSide = pos.x() > 0 ? -1 : 1;
		PenaltyArea penArea = NGeometry.getPenaltyArea(NGeometry.getTeamOfClosestGoalLine(pos));
		if (penArea.isPointInShape(pos, OFFENSE_FREEKICK_DISTANCE))
		{
			return getClosestGoalKickPos(pos)
					.addNew(new Vector2((DEFENSE_AREA_GOALLINE_DISTANCE - GOAL_KICK_DISTANCE) * xSide, 0));
		}
		return pos;
	}
	
	
	/**
	 * Checks if all bots that are located entirely inside the field area are on their own side of the field
	 * 
	 * @param bots
	 * @return true if all bots are in their half of the field
	 */
	public static boolean botsAreOnCorrectSide(final Collection<ITrackedBot> bots)
	{
		Rectangle field = Geometry.getField();
		Stream<ITrackedBot> onFieldBots = bots.stream().filter(
				bot -> {
					return field.isPointInShape(bot.getPos(), Geometry.getBotRadius());
				});
		
		return onFieldBots.allMatch(bot -> {
			Rectangle side = NGeometry.getFieldSide(bot.getTeamColor());
			return side.isPointInShape(bot.getPos());
		});
	}
}
