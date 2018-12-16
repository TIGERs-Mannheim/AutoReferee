/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.events.data;

import edu.tigers.autoreferee.engine.events.EGameEvent;
import edu.tigers.sumatra.gamecontroller.SslGameEvent2019;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;


public class BotHeldBallDeliberately extends AGameEvent
{
	
	private final ETeamColor team;
	private final int bot;
	private final IVector2 location;
	private final double duration;
	
	
	/**
	 * @param bot
	 * @param location
	 * @param duration [s]
	 */
	public BotHeldBallDeliberately(BotID bot, IVector2 location, double duration)
	{
		this.team = bot.getTeamColor();
		this.bot = bot.getNumber();
		this.location = location;
		this.duration = duration;
	}
	
	
	@Override
	public EGameEvent getType()
	{
		return EGameEvent.BOT_HELD_BALL_DELIBERATELY;
	}
	
	
	@Override
	public SslGameEvent2019.GameEvent toProtobuf()
	{
		SslGameEvent2019.GameEvent.Builder builder = SslGameEvent2019.GameEvent.newBuilder();
		builder.getBotHeldBallDeliberatelyBuilder().setByBot(bot).setByTeam(getTeam(team)).setDuration((float) duration)
				.setLocation(getLocationFromVector(location));
		
		return builder.build();
	}
	
	
	@Override
	public String toString()
	{
		return String.format("Bot %d %s hold the ball deliberately for %.2f s @ %s", bot, team, duration,
				formatVector(location));
	}
}
