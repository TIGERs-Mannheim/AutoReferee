/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.events.data;

import edu.tigers.autoreferee.engine.events.EGameEvent;
import edu.tigers.sumatra.gamecontroller.SslGameEvent2019;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;


public class BotKickedBallToFast extends AGameEvent
{
	
	private final ETeamColor team;
	private final int bot;
	private final IVector2 location;
	private final double initialBallSpeed;
	private final double maxBallHeight;
	
	
	/**
	 * @param bot
	 * @param location
	 * @param initialBallSpeed [m/s]
	 * @param maxBallHeight [mm]
	 */
	public BotKickedBallToFast(BotID bot, IVector2 location, double initialBallSpeed,
			double maxBallHeight)
	{
		this.team = bot.getTeamColor();
		this.bot = bot.getNumber();
		this.location = location;
		this.initialBallSpeed = initialBallSpeed;
		this.maxBallHeight = maxBallHeight;
	}
	
	
	@Override
	public EGameEvent getType()
	{
		return EGameEvent.BOT_KICKED_BALL_TOO_FAST;
	}
	
	
	@Override
	public SslGameEvent2019.GameEvent toProtobuf()
	{
		SslGameEvent2019.GameEvent.Builder builder = SslGameEvent2019.GameEvent.newBuilder();
		builder.getBotKickedBallTooFastBuilder().setByTeam(getTeam(team)).setByBot(bot)
				.setInitialBallSpeed((float) initialBallSpeed)
				.setMaxBallHeight((float) maxBallHeight / 1000.f).setLocation(getLocationFromVector(location));
		
		return builder.build();
	}
	
	
	@Override
	public String toString()
	{
		return String.format("Bot %d %s kicked ball too fast: vmax=%.2f m/s hmax=%.2f @ %s", bot, team, initialBallSpeed,
				maxBallHeight, formatVector(location));
	}
}
