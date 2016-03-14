/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 26, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.violations;

import java.util.Optional;

import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;


/**
 * @author "Lukas Magel"
 */
public interface IRuleViolation
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
		ICING,
		/**  */
		BALL_DRIBBLING,
		/**  */
		BOT_COUNT,
		/**  */
		BOT_STOP_SPEED,
		/**  */
		ATTACKER_IN_DEFENSE_AREA,
		/** The defending team comes too close to the ball during a freekick */
		DEFENDER_TO_KICK_POINT_DISTANCE,
		/** If the kick was not taken after a certain amount of time */
		KICK_TIMEOUT,
		/**  */
		MULTIPLE_DEFENDER,
		/**  */
		ATTACKER_TOUCH_KEEPER
	}
	
	
	/**
	 * @return
	 */
	public ERuleViolation getType();
	
	
	/**
	 * @return
	 */
	public long getTimestamp();
	
	
	/**
	 * @return
	 */
	public ETeamColor getTeamAtFault();
	
	
	/**
	 * @return
	 */
	public Optional<BotID> getBotAtFault();
	
	
	/**
	 * @return
	 */
	public String buildLogString();
	
	
	/**
	 * @return
	 */
	public FollowUpAction getFollowUpAction();
}
