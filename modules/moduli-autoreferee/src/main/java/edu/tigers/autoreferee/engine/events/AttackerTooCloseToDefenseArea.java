/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.events;

import edu.tigers.sumatra.gamecontroller.SslGameEvent2019;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;


public class AttackerTooCloseToDefenseArea extends AGameEvent
{
	private final ETeamColor team;
	private final int bot;
	private final IVector2 location;
	private final double distance;
	
	
	/**
	 * @param bot
	 * @param location
	 * @param distance [mm]
	 */
	public AttackerTooCloseToDefenseArea(BotID bot, IVector2 location, double distance)
	{
		this.team = bot.getTeamColor();
		this.bot = bot.getNumber();
		this.location = location;
		this.distance = distance;
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
		builder.setType(SslGameEvent2019.GameEventType.ATTACKER_TOO_CLOSE_TO_DEFENSE_AREA);
		builder.getAttackerTooCloseToDefenseAreaBuilder().setByTeam(getTeam(team)).setByBot(bot)
				.setDistance((float) distance / 1000.f)
				.setLocation(getLocationFromVector(location));
		
		return builder.build();
	}
	
	
	@Override
	public String toString()
	{
		return String.format("Attacker %d %s was %.2f mm too close to the penalty area @ %s", bot, team, distance,
				formatVector(location));
	}
}
