/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.events;

import edu.tigers.sumatra.gamecontroller.SslGameEvent2019;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;


public class KeeperHeldBall extends AGameEvent
{
	private final ETeamColor team;
	private final IVector2 location;
	private final double duration;
	
	
	/**
	 * @param team
	 * @param location
	 * @param duration [s]
	 */
	public KeeperHeldBall(ETeamColor team, IVector2 location, double duration)
	{
		this.team = team;
		this.location = location;
		this.duration = duration;
	}
	
	
	@Override
	public EGameEvent getType()
	{
		return EGameEvent.KEEPER_HELD_BALL;
	}
	
	
	@Override
	public SslGameEvent2019.GameEvent toProtobuf()
	{
		SslGameEvent2019.GameEvent.Builder builder = SslGameEvent2019.GameEvent.newBuilder();
		builder.setType(SslGameEvent2019.GameEventType.KEEPER_HELD_BALL);
		builder.getKeeperHeldBallBuilder().setByTeam(getTeam(team)).setDuration((float) duration)
				.setLocation(getLocationFromVector(location));
		return builder.build();
	}
	
	
	@Override
	public String toString()
	{
		return String.format("Keeper of team %s held ball for %.2f s @ %s", team, duration, formatVector(location));
	}
}
