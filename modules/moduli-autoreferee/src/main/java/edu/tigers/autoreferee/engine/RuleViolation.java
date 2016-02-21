/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 8, 2015
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine;

import edu.tigers.sumatra.ids.ETeamColor;


/**
 * @author "Lukas Magel"
 */
public class RuleViolation
{
	/**
	 * @author "Lukas Magel"
	 */
	public enum ERuleViolation
	{
		
		/**  */
		BALL_LEFT_FIELD,
		/**  */
		BALL_SPEEDING,
		/**  */
		DOUBLE_TOUCH,
		/**  */
		DISTANCE_TO_BALL,
		/**  */
		ATTACKER_TO_DEFENCE_AREA,
		/**  */
		BALL_HOLDING,
		/**  */
		BOT_COLLISION,
		/**  */
		INDIRECT_GOAL,
		/**  */
		NO_TOUCH_GOAL_LINE,
		/**  */
		BALL_DRIBBLING,
		/**  */
		BOT_COUNT,
		/**  */
		BOT_STOP_SPEED,
		/**  */
		ATTACKER_IN_DEFENSE_AREA,
		/** The defending team comes too close to the ball during a freekick */
		DEFENDER_KICK_DISTANCE,
		/** If the kick was not taken after a certain amount of time */
		KICK_TIMEOUT,
		/**  */
		MULTIPLE_DEFENDER,
		/**  */
		ATTACKER_TOUCH_KEEPER
		
	}
	
	private final ERuleViolation	violationType;
	private final long				timestamp;		// ns
	private final ETeamColor		teamAtFault;
											
											
	/**
	 * @param violationType
	 * @param timestamp in ns
	 * @param teamAtFault
	 */
	public RuleViolation(final ERuleViolation violationType, final long timestamp,
			final ETeamColor teamAtFault)
	{
		this.violationType = violationType;
		this.timestamp = timestamp;
		this.teamAtFault = teamAtFault;
	}
	
	
	/**
	 * @return
	 */
	public ERuleViolation getViolationType()
	{
		return violationType;
	}
	
	
	/**
	 * @return timestamp in ns
	 */
	public long getTimestamp()
	{
		return timestamp;
	}
	
	
	/**
	 * @return
	 */
	public ETeamColor getTeamAtFault()
	{
		return teamAtFault;
	}
}
