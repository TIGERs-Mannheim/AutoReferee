/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 27, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.log;

import java.time.Instant;

import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.RefCommand;
import edu.tigers.autoreferee.engine.log.GameLogEntry.ELogEntryType;
import edu.tigers.autoreferee.engine.violations.IRuleViolation;
import edu.tigers.sumatra.referee.RefereeMsg;
import edu.tigers.sumatra.wp.data.EGameStateNeutral;


/**
 * @author "Lukas Magel"
 */
public class GameLogEntryBuilder
{
	private ELogEntryType		type;
	/** frame timestamp in nanoseconds */
	private Long					timestamp;
	/** The time instant this entry was created in */
	private Instant				instant;
	/** in nanoseconds */
	private Long					timeSinceStart;
	
	private EGameStateNeutral	gamestate;
	private IRuleViolation		violation;
	private RefereeMsg			refereeMsg;
	private FollowUpAction		followUpAction;
	private RefCommand			command;
	
	
	private void setType(final ELogEntryType type)
	{
		this.type = type;
	}
	
	
	/**
	 * @param timestamp
	 */
	public void setTimestamp(final long timestamp)
	{
		this.timestamp = timestamp;
	}
	
	
	/**
	 * @param instant
	 */
	public void setInstant(final Instant instant)
	{
		this.instant = instant;
	}
	
	
	/**
	 * @param timeSinceStart
	 */
	public void setTimeSinceStart(final long timeSinceStart)
	{
		this.timeSinceStart = timeSinceStart;
	}
	
	
	/**
	 * @param gamestate
	 */
	public void setGamestate(final EGameStateNeutral gamestate)
	{
		this.gamestate = gamestate;
		setType(ELogEntryType.GAME_STATE);
	}
	
	
	/**
	 * @param violation
	 */
	public void setViolation(final IRuleViolation violation)
	{
		this.violation = violation;
		setType(ELogEntryType.VIOLATION);
	}
	
	
	/**
	 * @param refereeMsg
	 */
	public void setRefereeMsg(final RefereeMsg refereeMsg)
	{
		this.refereeMsg = refereeMsg;
		setType(ELogEntryType.REFEREE_MSG);
	}
	
	
	/**
	 * @param followUpAction
	 */
	public void setFollowUpAction(final FollowUpAction followUpAction)
	{
		this.followUpAction = followUpAction;
		setType(ELogEntryType.FOLLOW_UP);
	}
	
	
	/**
	 * @param command
	 */
	public void setCommand(final RefCommand command)
	{
		this.command = command;
		setType(ELogEntryType.COMMAND);
	}
	
	
	/**
	 * @return
	 */
	public GameLogEntry toEntry()
	{
		if ((type == null) || (timestamp == null) || (timeSinceStart == null) || (instant == null))
		{
			throw new NullPointerException("Not all required fields have been set");
		}
		
		return new GameLogEntry(timestamp, timeSinceStart, instant, type, gamestate, violation, refereeMsg,
				followUpAction, command);
	}
}
