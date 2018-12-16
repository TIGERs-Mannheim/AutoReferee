/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine.log;

import java.time.Instant;

import org.apache.commons.lang.NotImplementedException;

import edu.tigers.autoreferee.engine.RefboxRemoteCommand;
import edu.tigers.autoreferee.engine.events.GameEventResponse;
import edu.tigers.autoreferee.engine.events.IGameEvent;
import edu.tigers.sumatra.referee.data.GameEvent;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.referee.data.RefereeMsg;


/**
 * @author "Lukas Magel"
 */
public class GameLogEntry
{
	
	private final GameEventResponse gameEventResponse;
	
	private final ELogEntryType type;
	/** frame timestamp in nanoseconds */
	private final long timestamp;
	/** time of the game this entry was created at */
	private final GameTime gameTime;
	/** The time instant this entry was created in */
	private final Instant instant;
	
	private final GameState gamestate;
	private final RefereeMsg refereeMsg;
	private final RefboxRemoteCommand command;
	private final GameEvent refGameEvent;
	
	private final IGameEvent gameEvent;
	private final boolean acceptedByEngine;
	
	/**
	 * @param timestamp
	 * @param instant
	 * @param type
	 * @param gamestate
	 * @param gameEvent
	 * @param refereeMsg
	 * @param command
	 */
	protected GameLogEntry(final long timestamp, final GameTime gameTime, final Instant instant,
			final ELogEntryType type, final GameState gamestate, final IGameEvent gameEvent,
			final boolean acceptedByEngine, final RefereeMsg refereeMsg,
			final RefboxRemoteCommand command, GameEvent refGameEvent, GameEventResponse gameEventResponse)
	{
		this.type = type;
		this.gameTime = gameTime;
		this.timestamp = timestamp;
		this.instant = instant;
		
		this.gameEvent = gameEvent;
		this.gameEventResponse = gameEventResponse;
		this.acceptedByEngine = acceptedByEngine;
		
		this.gamestate = gamestate;
		this.refereeMsg = refereeMsg;
		this.command = command;
		this.refGameEvent = refGameEvent;
	}
	
	
	public GameEventResponse getGameEventResponse()
	{
		return gameEventResponse;
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
	 * @return the gameTime until the end of the stage
	 */
	public GameTime getGameTime()
	{
		return gameTime;
	}
	
	
	/**
	 * @return
	 */
	public GameState getGamestate()
	{
		return gamestate;
	}
	
	
	/**
	 * @return
	 */
	public IGameEvent getGameEvent()
	{
		return gameEvent;
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
			case GAME_STATE:
				return gamestate;
			case REFEREE_MSG:
				return refereeMsg;
			case GAME_EVENT:
				return gameEvent;
			case REFEREE_GAME_EVENT:
				return refGameEvent;
			case GAME_EVENT_REPLY:
				return gameEventResponse;
			default:
				throw new NotImplementedException("Please add the following enum value to this switch case: " + type);
		}
	}
	
	
	/**
	 * Returns true if the corresponding game event accessible over {@link #getGameEvent()} was accepted by the autoref
	 * engine and initiated a referee command and/or change of game state. This flag is only set when the autoref runs in
	 * active mode.
	 * 
	 * @return
	 */
	public boolean isAcceptedByEngine()
	{
		return acceptedByEngine;
	}
	
	
	/**
	 * @return
	 */
	public RefereeMsg getRefereeMsg()
	{
		return refereeMsg;
	}
	
	
	/**
	 * @return the command
	 */
	public RefboxRemoteCommand getCommand()
	{
		return command;
	}
	
	
	/**
	 * @return game event from auto ref
	 */
	public GameEvent getRefGameEvent()
	{
		return refGameEvent;
	}
	
	
	/**
	 * @return the instant this instance was created in. The value is in UTC
	 */
	public Instant getInstant()
	{
		return instant;
	}
	
	
	/**
	 * @author "Lukas Magel"
	 */
	public enum ELogEntryType
	{
		/** Describes a change in the game state e.g. RUNNING or STOP */
		GAME_STATE,
		
		/** Describes a game event e.g. BallLeftField */
		GAME_EVENT,
		
		/** Describes a ssl-game-controller-response to a reported GameEvent e.g. OK or REJECT */
		GAME_EVENT_REPLY,
		
		/**  */
		REFEREE_MSG,
		
		/** */
		REFEREE_GAME_EVENT,
		
		/**  */
		COMMAND
	}
	
	
	@Override
	public String toString()
	{
		return "GameLogEntry{" + type +
				" -> " + getObject() +
				'}';
	}
}
