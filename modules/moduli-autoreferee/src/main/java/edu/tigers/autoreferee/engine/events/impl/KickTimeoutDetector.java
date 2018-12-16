/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine.events.impl;

import java.util.EnumSet;
import java.util.Optional;

import com.github.g3force.configurable.Configurable;

import edu.tigers.autoreferee.engine.events.EGameEventDetectorType;
import edu.tigers.autoreferee.engine.events.IGameEvent;
import edu.tigers.autoreferee.engine.events.data.KickTimeout;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.referee.data.EGameState;


/**
 * The kick timeout will stop the game if the ball is not kicked
 */
public class KickTimeoutDetector extends AGameEventDetector
{
	@Configurable(defValue = "10.0")
	private static double freeKickTimeout = 10.0;
	
	private long entryTime;
	private boolean kickTimedOut;
	
	
	public KickTimeoutDetector()
	{
		super(EGameEventDetectorType.KICK_TIMEOUT, EnumSet.of(
				EGameState.DIRECT_FREE, EGameState.INDIRECT_FREE, EGameState.KICKOFF));
	}
	
	
	@Override
	protected void doPrepare()
	{
		entryTime = frame.getTimestamp();
		kickTimedOut = false;
	}
	
	
	@Override
	protected Optional<IGameEvent> doUpdate()
	{
		ETeamColor attackingColor = frame.getGameState().getForTeam();
		
		long curTime = frame.getTimestamp();
		if (((curTime - entryTime) / 1e9 > freeKickTimeout) && !kickTimedOut)
		{
			kickTimedOut = true;
			IGameEvent violation = new KickTimeout(attackingColor,
					getBall().getPos(), (curTime - entryTime) / 1e9);
			return Optional.of(violation);
		}
		return Optional.empty();
	}
}
