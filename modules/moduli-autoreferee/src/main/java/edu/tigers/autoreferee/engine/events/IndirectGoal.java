/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.events;

import edu.tigers.sumatra.gamecontroller.SslGameEvent2019;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;


public class IndirectGoal extends AGameEvent
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
	public IndirectGoal(BotID bot, IVector2 location, IVector2 kickLocation)
	{
		this.team = bot.getTeamColor();
		this.bot = bot.getNumber();
		this.location = location;
		this.kickLocation = kickLocation;
	}
	
	
	@Override
	public EGameEvent getType()
	{
		return EGameEvent.INDIRECT_GOAL;
	}
	
	
	@Override
	public SslGameEvent2019.GameEvent toProtobuf()
	{
		SslGameEvent2019.GameEvent.Builder builder = SslGameEvent2019.GameEvent.newBuilder();
		builder.setType(SslGameEvent2019.GameEventType.INDIRECT_GOAL);
		builder.getIndirectGoalBuilder().setByBot(bot).setByTeam(getTeam(team))
				.setLocation(getLocationFromVector(location))
				.setKickLocation(getLocationFromVector(kickLocation));
		
		return builder.build();
	}
	
	
	@Override
	public String toString()
	{
		return String.format("Bot %d %s kicked an indirect goal (%s -> %s)", bot, team, formatVector(kickLocation),
				formatVector(location));
	}
}
