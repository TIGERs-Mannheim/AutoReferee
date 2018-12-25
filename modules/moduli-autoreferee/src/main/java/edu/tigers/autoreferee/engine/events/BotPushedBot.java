/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.events;

import edu.tigers.sumatra.gamecontroller.SslGameEvent2019;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;


public class BotPushedBot extends AGameEvent
{
	private final ETeamColor team;
	private final int violator;
	private final int victim;
	private final IVector2 location;
	private final double pushedDistance;
	
	
	/**
	 * @param violator
	 * @param victim
	 * @param location
	 * @param pushedDistance [mm]
	 */
	public BotPushedBot(BotID violator,
			BotID victim,
			IVector2 location,
			double pushedDistance)
	{
		this.team = violator.getTeamColor();
		this.violator = violator.getNumber();
		this.victim = victim.getNumber();
		this.location = location;
		this.pushedDistance = pushedDistance;
	}
	
	
	@Override
	public EGameEvent getType()
	{
		return EGameEvent.BOT_PUSHED_BOT;
	}
	
	
	@Override
	public SslGameEvent2019.GameEvent toProtobuf()
	{
		SslGameEvent2019.GameEvent.Builder builder = SslGameEvent2019.GameEvent.newBuilder();
		builder.setType(SslGameEvent2019.GameEventType.BOT_PUSHED_BOT);
		builder.getBotPushedBotBuilder().setByTeam(getTeam(team)).setViolator(violator)
				.setVictim(victim).setPushedDistance((float) pushedDistance / 1000.f)
				.setLocation(getLocationFromVector(location));
		
		return builder.build();
	}
	
	
	@Override
	public String toString()
	{
		return String.format("Bot %d %s pushed bot %d %s for %.2f mm @ %s", violator, team, victim, team.opposite(),
				pushedDistance, formatVector(location));
	}
}
