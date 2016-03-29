/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 23, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.log;

import java.time.Instant;

import org.apache.commons.lang.NotImplementedException;

import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.RefCommand;
import edu.tigers.autoreferee.engine.violations.IRuleViolation;
import edu.tigers.sumatra.referee.RefereeMsg;
import edu.tigers.sumatra.wp.data.EGameStateNeutral;


/**
 * @author "Lukas Magel"
 */
public class GameLogEntry
{
	/**
	 * @author "Lukas Magel"
	 */
	public enum ELogEntryType
	{
		/**  */
		GAME_STATE,
		/**  */
		VIOLATION,
		/**  */
		REFEREE_MSG,
		/**  */
		FOLLOW_UP,
		/**  */
		COMMAND
	}
	
	private final ELogEntryType		type;
	/** frame timestamp in nanoseconds */
	private final long					timestamp;
	/** The time instant this entry was created in */
	private final Instant				instant;
	/** in nanoseconds */
	private final long					timeSinceStart;
	
	private final EGameStateNeutral	gamestate;
	private final IRuleViolation		violation;
	private final RefereeMsg			refereeMsg;
	private final FollowUpAction		followUpAction;
	private final RefCommand			command;
	
	
	/**
	 * @param timestamp
	 * @param timeSinceStart
	 * @param instant
	 * @param type
	 * @param gamestate
	 * @param violation
	 * @param refereeMsg
	 * @param followUpAction
	 * @param command
	 */
	protected GameLogEntry(final long timestamp, final long timeSinceStart, final Instant instant,
			final ELogEntryType type, final EGameStateNeutral gamestate, final IRuleViolation violation,
			final RefereeMsg refereeMsg, final FollowUpAction followUpAction, final RefCommand command)
	{
		this.type = type;
		this.timeSinceStart = timeSinceStart;
		this.timestamp = timestamp;
		this.instant = instant;
		
		this.gamestate = gamestate;
		this.violation = violation;
		this.refereeMsg = refereeMsg;
		this.followUpAction = followUpAction;
		this.command = command;
	}
	
	
	/**
	 * @param timestamp
	 * @param timeSinceStart
	 * @param instant
	 * @param gamestate
	 * @return
	 */
	public static GameLogEntry create(final long timestamp, final long timeSinceStart, final Instant instant,
			final EGameStateNeutral gamestate)
	{
		return new GameLogEntry(timestamp, timeSinceStart, instant, ELogEntryType.GAME_STATE, gamestate, null, null,
				null, null);
	}
	
	
	/**
	 * @param timestamp
	 * @param timeSinceStart
	 * @param instant
	 * @param violation
	 * @return
	 */
	public static GameLogEntry create(final long timestamp, final long timeSinceStart, final Instant instant,
			final IRuleViolation violation)
	{
		return new GameLogEntry(timestamp, timeSinceStart, instant, ELogEntryType.VIOLATION, null, violation, null, null,
				null);
	}
	
	
	/**
	 * @param timestamp
	 * @param timeSinceStart
	 * @param instant
	 * @param refereeMsg
	 * @return
	 */
	public static GameLogEntry create(final long timestamp, final long timeSinceStart, final Instant instant,
			final RefereeMsg refereeMsg)
	{
		return new GameLogEntry(timestamp, timeSinceStart, instant, ELogEntryType.REFEREE_MSG, null, null, refereeMsg,
				null, null);
	}
	
	
	/**
	 * @param timestamp
	 * @param timeSinceStart
	 * @param instant
	 * @param followUpAction
	 * @return
	 */
	public static GameLogEntry create(final long timestamp, final long timeSinceStart, final Instant instant,
			final FollowUpAction followUpAction)
	{
		return new GameLogEntry(timestamp, timeSinceStart, instant, ELogEntryType.FOLLOW_UP, null, null, null,
				followUpAction,
				null);
	}
	
	
	/**
	 * @param timestamp
	 * @param timeSinceStart
	 * @param instant
	 * @param command
	 * @return
	 */
	public static GameLogEntry create(final long timestamp, final long timeSinceStart, final Instant instant,
			final RefCommand command)
	{
		return new GameLogEntry(timestamp, timeSinceStart, instant, ELogEntryType.COMMAND, null, null, null, null,
				command);
	}
	
	
	/**
	 * @return
	 */
	public ELogEntryType getType()
	{
		return type;
	}
	
	
	/**
	 * The timestamp of the frame this entry was reported in
	 * 
	 * @return timestamp in nanoseconds
	 */
	public long getTimestamp()
	{
		return timestamp;
	}
	
	
	/**
	 * The number of nanoseconds since the autoreferee was started
	 * 
	 * @return time since the start in nanoseconds
	 */
	public long getTimeSinceStart()
	{
		return timeSinceStart;
	}
	
	
	/**
	 * @return
	 */
	public EGameStateNeutral getGamestate()
	{
		return gamestate;
	}
	
	
	/**
	 * @return
	 */
	public IRuleViolation getViolation()
	{
		return violation;
	}
	
	
	/**
	 * @return
	 */
	public RefereeMsg getRefereeMsg()
	{
		return refereeMsg;
	}
	
	
	/**
	 * @return
	 */
	public FollowUpAction getFollowUpAction()
	{
		return followUpAction;
	}
	
	
	/**
	 * @return the command
	 */
	public RefCommand getCommand()
	{
		return command;
	}
	
	
	/**
	 * @return the instant this instance was created in. The value is in UTC
	 */
	public Instant getInstant()
	{
		return instant;
	}
	
	
	/**
	 * Returns the object stored in this entry instance
	 * 
	 * @return
	 */
	public Object getObject()
	{
		switch (type)
		{
			case COMMAND:
				return command;
			case FOLLOW_UP:
				return followUpAction;
			case GAME_STATE:
				return gamestate;
			case REFEREE_MSG:
				return refereeMsg;
			case VIOLATION:
				return violation;
			default:
				throw new NotImplementedException("Please add the following enum value to this switch case: " + type);
		}
	}
	
}
