/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.events.data;

import edu.tigers.autoreferee.engine.events.EGameEvent;
import edu.tigers.sumatra.gamecontroller.SslGameEvent2019;
import edu.tigers.sumatra.ids.ETeamColor;


public class BotSubstitution extends AGameEvent
{
	
	private final ETeamColor team;
	
	
	/**
	 * @param team
	 */
	public BotSubstitution(ETeamColor team)
	{
		this.team = team;
	}
	
	
	@Override
	public EGameEvent getType()
	{
		return EGameEvent.BOT_SUBSTITUTION;
	}
	
	
	@Override
	public SslGameEvent2019.GameEvent toProtobuf()
	{
		SslGameEvent2019.GameEvent.Builder eventBuilder = SslGameEvent2019.GameEvent.newBuilder();
		eventBuilder.getBotSubstitutionBuilder().setByTeam(getTeam(team));
		
		return eventBuilder.build();
	}
	
	
	@Override
	public String toString()
	{
		return String.format("Team %s wants to substitute a bot", team);
	}
}
