/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.events.data;

import edu.tigers.autoreferee.engine.events.EGameEvent;
import edu.tigers.sumatra.gamecontroller.SslGameEvent2019;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;


public class BotInterferedPlacement extends AGameEvent
{
	
	private final ETeamColor team;
	private final int bot;
	private final IVector2 location;
	
	
	/**
	 * @param bot
	 * @param location
	 */
	public BotInterferedPlacement(BotID bot, IVector2 location)
	{
		this.team = bot.getTeamColor();
		this.bot = bot.getNumber();
		this.location = location;
	}
	
	
	@Override
	public EGameEvent getType()
	{
		return EGameEvent.BOT_INTERFERED_PLACEMENT;
	}
	
	
	@Override
	public SslGameEvent2019.GameEvent toProtobuf()
	{
		SslGameEvent2019.GameEvent.Builder builder = SslGameEvent2019.GameEvent.newBuilder();
		builder.getBotInterferedPlacementBuilder().setByBot(bot).setByTeam(getTeam(team))
				.setLocation(getLocationFromVector(location));
		
		return builder.build();
	}
	
	
	@Override
	public String toString()
	{
		return String.format("Bot %d %s interfered ball placement @ %s", bot, team, formatVector(location));
	}
}
