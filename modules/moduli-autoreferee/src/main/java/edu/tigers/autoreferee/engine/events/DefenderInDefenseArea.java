/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.events;

import edu.tigers.sumatra.gamecontroller.SslGameEvent2019;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;


public class DefenderInDefenseArea extends AGameEvent
{
	private final boolean partially;
	private final ETeamColor team;
	private final int bot;
	private final IVector2 location;
	private final double distance;
	
	
	/**
	 * @param partially
	 * @param bot
	 * @param location
	 * @param distance [mm]
	 */
	public DefenderInDefenseArea(boolean partially, BotID bot, IVector2 location,
			double distance)
	{
		this.partially = partially;
		this.team = bot.getTeamColor();
		this.bot = bot.getNumber();
		this.location = location;
		this.distance = distance;
	}
	
	
	@Override
	public EGameEvent getType()
	{
		return partially ? EGameEvent.DEFENDER_IN_DEFENSE_AREA_PARTIALLY : EGameEvent.DEFENDER_IN_DEFENSE_AREA;
	}
	
	
	@Override
	public SslGameEvent2019.GameEvent toProtobuf()
	{
		SslGameEvent2019.GameEvent.Builder builder = SslGameEvent2019.GameEvent.newBuilder();
		
		if (partially)
		{
			builder.setType(SslGameEvent2019.GameEventType.DEFENDER_IN_DEFENSE_AREA_PARTIALLY);
			builder.getDefenderInDefenseAreaPartiallyBuilder().setByTeam(getTeam(team)).setByBot(bot)
					.setDistance((float) distance / 1000.f)
					.setLocation(getLocationFromVector(location));
		} else
		{
			builder.setType(SslGameEvent2019.GameEventType.DEFENDER_IN_DEFENSE_AREA);
			builder.getDefenderInDefenseAreaBuilder().setByTeam(getTeam(team)).setByBot(bot)
					.setDistance((float) distance / 1000.f)
					.setLocation(getLocationFromVector(location));
		}
		return builder.build();
	}
	
	
	@Override
	public String toString()
	{
		return String.format("Defender %d %s was%sin the penalty area for %.2f mm @ %s", bot, team,
				partially ? " partially" : " ", distance, formatVector(location));
	}
}
