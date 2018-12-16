/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.events.data;

import edu.tigers.autoreferee.engine.events.EGameEvent;
import edu.tigers.sumatra.gamecontroller.SslGameEvent2019;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;


public class BallLeftFieldEvent extends AGameEvent
{
	private final ETeamColor team;
	private final int bot;
	private final IVector2 location;
	private final boolean goalLine;
	
	
	/**
	 * @param bot
	 * @param location
	 * @param goalLine Ball left field at the goalline
	 */
	public BallLeftFieldEvent(BotID bot, IVector2 location, boolean goalLine)
	{
		this.team = bot.getTeamColor();
		this.bot = bot.getNumber();
		this.location = location;
		this.goalLine = goalLine;
	}
	
	
	@Override
	public EGameEvent getType()
	{
		return goalLine ? EGameEvent.BALL_LEFT_FIELD_GOAL_LINE : EGameEvent.BALL_LEFT_FIELD_TOUCH_LINE;
	}
	
	
	@Override
	public SslGameEvent2019.GameEvent toProtobuf()
	{
		SslGameEvent2019.GameEvent.Builder builder = SslGameEvent2019.GameEvent.newBuilder();
		
		if (goalLine)
		{
			builder.getBallLeftFieldGoalLineBuilder().setByBot(bot).setByTeam(getTeam(team))
					.setLocation(getLocationFromVector(location));
		} else
		{
			builder.getBallLeftFieldTouchLineBuilder().setByBot(bot).setByTeam(getTeam(team))
					.setLocation(getLocationFromVector(location));
		}
		
		return builder.build();
	}
	
	
	@Override
	public String toString()
	{
		return String.format("Ball left field @ %s by bot %d %s (%s line)", formatVector(location), bot, team,
				goalLine ? "goal" : "touch");
	}
}
