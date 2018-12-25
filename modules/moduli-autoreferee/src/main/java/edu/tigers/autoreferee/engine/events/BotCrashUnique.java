/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.events;

import edu.tigers.sumatra.gamecontroller.SslGameEvent2019;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;


public class BotCrashUnique extends AGameEvent
{
	private final ETeamColor team;
	private final int violator;
	private final int victim;
	private final IVector2 location;
	private final double crashSpeed;
	private final double speedDiff;
	private final double crashAngle;
	
	
	/**
	 * @param violator
	 * @param victim
	 * @param location
	 * @param crashSpeed [m/s]
	 * @param speedDiff [m/s]
	 * @param crashAngle [rad]
	 */
	public BotCrashUnique(BotID violator, BotID victim,
			IVector2 location,
			double crashSpeed,
			double speedDiff,
			double crashAngle)
	{
		this.team = violator.getTeamColor();
		this.violator = violator.getNumber();
		this.victim = victim.getNumber();
		this.location = location;
		this.crashSpeed = crashSpeed;
		this.speedDiff = speedDiff;
		this.crashAngle = crashAngle;
	}
	
	
	@Override
	public EGameEvent getType()
	{
		return EGameEvent.BOT_CRASH_UNIQUE;
	}
	
	
	@Override
	public SslGameEvent2019.GameEvent toProtobuf()
	{
		SslGameEvent2019.GameEvent.Builder builder = SslGameEvent2019.GameEvent.newBuilder();
		builder.setType(SslGameEvent2019.GameEventType.BOT_CRASH_UNIQUE);
		builder.getBotCrashUniqueBuilder().setByTeam(getTeam(team)).setViolator(violator)
				.setVictim(victim).setCrashSpeed((float) crashSpeed).setSpeedDiff((float) speedDiff)
				.setCrashAngle((float) crashAngle).setLocation(getLocationFromVector(location));
		
		return builder.build();
	}
	
	
	@Override
	public String toString()
	{
		return String.format("Bot %d %s crashed into bot %d %s at %.2f m/s @ %s (Δv: %.2f m/s, angle. %.2f rad)",
				violator, team, victim, team.opposite(), crashSpeed, formatVector(location), speedDiff, crashAngle);
	}
}
