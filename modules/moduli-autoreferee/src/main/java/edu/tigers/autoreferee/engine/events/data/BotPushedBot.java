/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.events.data;

import edu.tigers.autoreferee.engine.events.EGameEvent;
import edu.tigers.sumatra.gamecontroller.SslGameEvent2019;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;


public class BotPushedBot extends AGameEvent
{
	private final boolean skipped;
	private final ETeamColor team;
	private final int violator;
	private final int victim;
	private final IVector2 location;
	private final double pushedDistance;
	
	
	/**
	 * @param skipped Not violating team wants to skipp game event
	 * @param violator
	 * @param victim
	 * @param location
	 * @param pushedDistance [mm]
	 */
	public BotPushedBot(boolean skipped, BotID violator, BotID victim, IVector2 location,
			double pushedDistance)
	{
		this.skipped = skipped;
		this.team = violator.getTeamColor();
		this.violator = violator.getNumber();
		this.victim = victim.getNumber();
		this.location = location;
		this.pushedDistance = pushedDistance;
	}
	
	
	@Override
	public EGameEvent getType()
	{
		return skipped ? EGameEvent.BOT_PUSHED_BOT_SKIPPED : EGameEvent.BOT_PUSHED_BOT;
	}
	
	
	@Override
	public SslGameEvent2019.GameEvent toProtobuf()
	{
		SslGameEvent2019.GameEvent.Builder builder = SslGameEvent2019.GameEvent.newBuilder();
		if (skipped)
		{
			builder.getBotPushedBotSkippedBuilder().setByTeam(getTeam(team)).setViolator(violator)
					.setVictim(victim).setPushedDistance((float) pushedDistance / 1000.f)
					.setLocation(getLocationFromVector(location));
		} else
		{
			builder.getBotPushedBotBuilder().setByTeam(getTeam(team)).setViolator(violator)
					.setVictim(victim).setPushedDistance((float) pushedDistance / 1000.f)
					.setLocation(getLocationFromVector(location));
		}
		
		return builder.build();
	}
	
	
	@Override
	public String toString()
	{
		return String.format("Bot %d %s pushed bot %d %s for %.2f mm @ %s", violator, team, victim, team.opposite(),
				pushedDistance, formatVector(location));
	}
}
