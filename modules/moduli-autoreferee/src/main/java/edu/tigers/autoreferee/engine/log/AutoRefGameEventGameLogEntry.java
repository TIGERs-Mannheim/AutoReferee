/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.log;

import java.awt.Color;

import edu.tigers.sumatra.referee.gameevent.IGameEvent;


public class AutoRefGameEventGameLogEntry extends GameLogEntry
{
	private final IGameEvent gameEvent;


	public AutoRefGameEventGameLogEntry(
			final long timestamp,
			final GameTime gameTime,
			final IGameEvent gameEvent)
	{
		super(ELogEntryType.DETECTED_GAME_EVENT, timestamp, gameTime);
		this.gameEvent = gameEvent;
	}


	@Override
	public String workGameLogEntry()
	{
		return gameEvent.getType().name() + " - " + gameEvent.getDescription();
	}


	@Override
	public String getToolTipText()
	{
		return "The AutoReferee has registered the following game event " + gameEvent.getType();
	}


	@Override
	public String toString()
	{
		return String.format("%d | %s | %s | %s", getTimestamp(), getGameTime(), getType(), gameEvent.getDescription());
	}


	@Override
	public Color getForegroundColor()
	{
		switch (gameEvent.getType().getType())
		{
			case FOUL:
				return new Color(250, 14, 10);
			case BALL_LEFT_FIELD:
				return new Color(250, 150, 31);
			case OTHER:
			default:
				return super.getForegroundColor();
		}
	}
}
