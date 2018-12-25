/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.events;

import edu.tigers.sumatra.gamecontroller.SslGameEvent2019;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;


public class Goal extends AGameEvent
{
	private final ETeamColor team;
	private final int bot;
	private final IVector2 location;
	private final IVector2 kickLocation;
	private final boolean unsure;
	
	
	/**
	 * @param unsure
	 * @param bot
	 * @param location
	 * @param kickLocation
	 */
	public Goal(boolean unsure, BotID bot, IVector2 location, IVector2 kickLocation)
	{
		this.unsure = unsure;
		this.team = bot.getTeamColor();
		this.bot = bot.getNumber();
		this.location = location;
		this.kickLocation = kickLocation;
	}
	
	
	@Override
	public EGameEvent getType()
	{
		return unsure ? EGameEvent.POSSIBLE_GOAL : EGameEvent.GOAL;
	}
	
	
	@Override
	public SslGameEvent2019.GameEvent toProtobuf()
	{
		SslGameEvent2019.GameEvent.Builder builder = SslGameEvent2019.GameEvent.newBuilder();
		
		if (unsure)
		{
			builder.setType(SslGameEvent2019.GameEventType.POSSIBLE_GOAL);
			builder.getPossibleGoalBuilder().setByBot(bot).setByTeam(getTeam(team))
					.setLocation(getLocationFromVector(location))
					.setKickLocation(getLocationFromVector(kickLocation));
		} else
		{
			builder.setType(SslGameEvent2019.GameEventType.GOAL);
			builder.getGoalBuilder().setByBot(bot).setByTeam(getTeam(team)).setLocation(getLocationFromVector(location))
					.setKickLocation(getLocationFromVector(kickLocation));
		}
		return builder.build();
	}
	
	
	@Override
	public String toString()
	{
		return String.format("Bot %d %s scored%sgoal (%s -> %s)", bot, team, unsure ? " possible " : " ",
				formatVector(kickLocation), formatVector(location));
	}
}
