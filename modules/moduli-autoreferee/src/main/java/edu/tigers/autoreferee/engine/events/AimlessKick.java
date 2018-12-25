/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.events;

import edu.tigers.sumatra.gamecontroller.SslGameEvent2019;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;


public class AimlessKick extends AGameEvent
{
	private final ETeamColor team;
	private final int bot;
	private final IVector2 location;
	private final IVector2 kickLocation;
	
	
	/**
	 * @param bot
	 * @param location
	 * @param kickLocation
	 */
	public AimlessKick(BotID bot, IVector2 location, IVector2 kickLocation)
	{
		this.team = bot.getTeamColor();
		this.bot = bot.getNumber();
		this.location = location;
		this.kickLocation = kickLocation;
	}
	
	
	@Override
	public EGameEvent getType()
	{
		return EGameEvent.AIMLESS_KICK;
	}
	
	
	@Override
	public SslGameEvent2019.GameEvent toProtobuf()
	{
		SslGameEvent2019.GameEvent.Builder builder = SslGameEvent2019.GameEvent.newBuilder();
		builder.setType(SslGameEvent2019.GameEventType.AIMLESS_KICK);
		builder.getAimlessKickBuilder().setByBot(bot).setByTeam(getTeam(team))
				.setLocation(getLocationFromVector(location))
				.setKickLocation(getLocationFromVector(kickLocation));
		
		return builder.build();
	}
	
	
	@Override
	public String toString()
	{
		return String.format("Aimless kick by Bot %d %s @ %s", bot, team, formatVector(location));
	}
}
