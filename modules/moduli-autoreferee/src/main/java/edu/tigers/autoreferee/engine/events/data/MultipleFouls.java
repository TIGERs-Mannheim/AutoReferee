/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.events.data;

import edu.tigers.autoreferee.engine.events.EGameEvent;
import edu.tigers.sumatra.gamecontroller.SslGameEvent2019;
import edu.tigers.sumatra.ids.ETeamColor;


public class MultipleFouls extends AGameEvent
{
	
	private final ETeamColor team;
	
	
	/**
	 * @param team
	 */
	public MultipleFouls(ETeamColor team)
	{
		this.team = team;
	}
	
	
	@Override
	public EGameEvent getType()
	{
		return EGameEvent.MULTIPLE_FOULS;
	}
	
	
	@Override
	public SslGameEvent2019.GameEvent toProtobuf()
	{
		SslGameEvent2019.GameEvent.Builder builder = SslGameEvent2019.GameEvent.newBuilder();
		builder.getMultipleFoulsBuilder().setByTeam(getTeam(team));
		
		return builder.build();
	}
	
	
	@Override
	public String toString()
	{
		return String.format("Multiple fouls by Team %s", team);
	}
}
