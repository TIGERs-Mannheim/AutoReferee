/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.events;

import edu.tigers.sumatra.gamecontroller.SslGameEvent2019;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;


public class AttackerDoubleTouchedBall extends AGameEvent
{
	private final ETeamColor team;
	private final int bot;
	private final IVector2 location;
	
	
	/**
	 * @param bot
	 * @param location
	 */
	public AttackerDoubleTouchedBall(BotID bot, IVector2 location)
	{
		this.team = bot.getTeamColor();
		this.bot = bot.getNumber();
		this.location = location;
	}
	
	
	@Override
	public EGameEvent getType()
	{
		return EGameEvent.ATTACKER_DOUBLE_TOUCHED_BALL;
	}
	
	
	@Override
	public SslGameEvent2019.GameEvent toProtobuf()
	{
		SslGameEvent2019.GameEvent.Builder builder = SslGameEvent2019.GameEvent.newBuilder();
		builder.setType(SslGameEvent2019.GameEventType.ATTACKER_DOUBLE_TOUCHED_BALL);
		builder.getAttackerDoubleTouchedBallBuilder().setByBot(bot).setByTeam(getTeam(team))
				.setLocation(getLocationFromVector(location));
		
		return builder.build();
	}
	
	
	@Override
	public String toString()
	{
		return String.format("Attacked %d %s double touched ball @ %s", bot, team, formatVector(location));
	}
}
