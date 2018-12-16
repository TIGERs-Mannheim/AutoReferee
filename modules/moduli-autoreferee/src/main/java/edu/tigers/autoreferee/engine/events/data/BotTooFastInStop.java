/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.events.data;

import edu.tigers.autoreferee.engine.events.EGameEvent;
import edu.tigers.sumatra.gamecontroller.SslGameEvent2019;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;


public class BotTooFastInStop extends AGameEvent
{
	private final ETeamColor team;
	private final int bot;
	private final IVector2 location;
	private final double speed;
	
	
	/**
	 * @param bot
	 * @param location
	 * @param speed [m/s]
	 */
	public BotTooFastInStop(BotID bot, IVector2 location, double speed)
	{
		this.team = bot.getTeamColor();
		this.bot = bot.getNumber();
		this.location = location;
		this.speed = speed;
	}
	
	
	@Override
	public EGameEvent getType()
	{
		return EGameEvent.ATTACKER_TOO_CLOSE_TO_DEFENSE_AREA;
	}
	
	
	@Override
	public SslGameEvent2019.GameEvent toProtobuf()
	{
		SslGameEvent2019.GameEvent.Builder builder = SslGameEvent2019.GameEvent.newBuilder();
		builder.getBotTooFastInStopBuilder().setByTeam(getTeam(team)).setByBot(bot).setSpeed((float) speed)
				.setLocation(getLocationFromVector(location));
		
		return builder.build();
	}
	
	
	@Override
	public String toString()
	{
		return String.format("Bot %d %s moved to fast during stop: vmax=%.2f @ %s", bot, team, speed,
				formatVector(location));
	}
}
