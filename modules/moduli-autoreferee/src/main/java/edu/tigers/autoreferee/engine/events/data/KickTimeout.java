/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.events.data;

import edu.tigers.autoreferee.engine.events.EGameEvent;
import edu.tigers.sumatra.gamecontroller.SslGameEvent2019;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;


public class KickTimeout extends AGameEvent
{
	private final ETeamColor team;
	private final IVector2 location;
	private final double time;
	
	
	/**
	 * @param team
	 * @param location
	 * @param time [s]
	 */
	public KickTimeout(ETeamColor team, final IVector2 location, double time)
	{
		this.team = team;
		this.location = location;
		this.time = time;
	}
	
	
	@Override
	public EGameEvent getType()
	{
		return EGameEvent.KICK_TIMEOUT;
	}
	
	
	@Override
	public SslGameEvent2019.GameEvent toProtobuf()
	{
		SslGameEvent2019.GameEvent.Builder builder = SslGameEvent2019.GameEvent.newBuilder();
		builder.getKickTimeoutBuilder()
				.setByTeam(getTeam(team))
				.setLocation(getLocationFromVector(location))
				.setTime((float) time);
		
		return builder.build();
	}
	
	
	@Override
	public String toString()
	{
		return String.format("Timeout for kick of team %s after %.2f s", team, time);
	}
}
