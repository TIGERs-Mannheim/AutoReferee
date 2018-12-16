/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.events.data;

import edu.tigers.autoreferee.engine.events.EGameEvent;
import edu.tigers.sumatra.gamecontroller.SslGameEvent2019;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;


public class AttackerTouchedKeeper extends AGameEvent
{
	private final ETeamColor team;
	private final int bot;
	private final IVector2 location;
	
	
	/**
	 * @param bot
	 * @param location
	 */
	public AttackerTouchedKeeper(BotID bot, IVector2 location)
	{
		this.team = bot.getTeamColor();
		this.bot = bot.getNumber();
		this.location = location;
	}
	
	
	@Override
	public EGameEvent getType()
	{
		return EGameEvent.ATTACKER_TOUCH_KEEPER;
	}
	
	
	@Override
	public SslGameEvent2019.GameEvent toProtobuf()
	{
		SslGameEvent2019.GameEvent.Builder builder = SslGameEvent2019.GameEvent.newBuilder();
		builder.getAttackerTouchedKeeperBuilder().setByBot(bot).setByTeam(getTeam(team))
				.setLocation(getLocationFromVector(location));
		
		return builder.build();
	}
	
	
	@Override
	public String toString()
	{
		return String.format("Attacker %d %s touched keeper @ %s", bot, team, formatVector(location));
	}
}
