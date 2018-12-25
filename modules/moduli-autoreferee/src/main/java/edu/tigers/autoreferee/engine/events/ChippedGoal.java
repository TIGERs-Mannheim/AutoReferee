/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.events;

import edu.tigers.sumatra.gamecontroller.SslGameEvent2019;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;


public class ChippedGoal extends AGameEvent
{
	private final ETeamColor team;
	private final int bot;
	private final IVector2 location;
	private final IVector2 kickLocation;
	private final double maxBallHeight;
	
	
	/**
	 * @param bot
	 * @param location
	 * @param kickLocation
	 * @param maxBallHeight [mm]
	 */
	public ChippedGoal(BotID bot, IVector2 location, IVector2 kickLocation,
			double maxBallHeight)
	{
		this.team = bot.getTeamColor();
		this.bot = bot.getNumber();
		this.location = location;
		this.kickLocation = kickLocation;
		this.maxBallHeight = maxBallHeight;
	}
	
	
	@Override
	public EGameEvent getType()
	{
		return EGameEvent.CHIP_ON_GOAL;
	}
	
	
	@Override
	public SslGameEvent2019.GameEvent toProtobuf()
	{
		SslGameEvent2019.GameEvent.Builder builder = SslGameEvent2019.GameEvent.newBuilder();
		builder.setType(SslGameEvent2019.GameEventType.CHIPPED_GOAL);
		builder.getChippedGoalBuilder().setByBot(bot).setByTeam(getTeam(team))
				.setMaxBallHeight((float) maxBallHeight / 1000.f)
				.setLocation(getLocationFromVector(location)).setKickLocation(getLocationFromVector(kickLocation));
		
		return builder.build();
	}
	
	
	@Override
	public String toString()
	{
		return String.format("Bot %d %s chipped onto the goal (%s -> %s) with hmax=%.2f mm", bot, team,
				formatVector(kickLocation), formatVector(location), maxBallHeight);
	}
}
