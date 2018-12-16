/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.events.data;

import edu.tigers.autoreferee.engine.events.EGameEvent;
import edu.tigers.sumatra.gamecontroller.SslGameEvent2019;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;


public class BotDribbledBallTooFar extends AGameEvent
{
	
	private final ETeamColor team;
	private final int bot;
	private final IVector2 start;
	private final IVector2 end;
	
	
	/**
	 * @param bot
	 * @param start
	 * @param end
	 */
	public BotDribbledBallTooFar(BotID bot, IVector2 start, IVector2 end)
	{
		this.team = bot.getTeamColor();
		this.bot = bot.getNumber();
		this.start = start;
		this.end = end;
	}
	
	
	@Override
	public EGameEvent getType()
	{
		return EGameEvent.BOT_DRIBBLED_BALL_TOO_FAR;
	}
	
	
	@Override
	public SslGameEvent2019.GameEvent toProtobuf()
	{
		SslGameEvent2019.GameEvent.Builder builder = SslGameEvent2019.GameEvent.newBuilder();
		builder.getBotDribbledBallTooFarBuilder().setByBot(bot).setByTeam(getTeam(team))
				.setStart(getLocationFromVector(start))
				.setEnd(getLocationFromVector(end));
		
		return builder.build();
	}
	
	
	@Override
	public String toString()
	{
		return String.format("Bot %d %s dribbled the ball %.2f mm (%s -> %s)", bot, team, start.distanceTo(end),
				formatVector(start),
				formatVector(end));
	}
}
