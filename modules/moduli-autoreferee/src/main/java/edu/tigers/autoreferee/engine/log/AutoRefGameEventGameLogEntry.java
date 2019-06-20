package edu.tigers.autoreferee.engine.log;

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
		return gameEvent.getType().name() + " - " + gameEvent.toString();
	}


	@Override
	public String getToolTipText()
	{
		return "The AutoReferee has registered the following game event " + gameEvent;
	}


	@Override
	public String toString()
	{
		return String.format("%d | %s | %s | %s", getTimestamp(), getGameTime(), getType(), gameEvent);
	}
}
