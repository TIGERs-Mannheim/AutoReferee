/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.events.data;

import edu.tigers.autoreferee.engine.events.EGameEvent;
import edu.tigers.sumatra.gamecontroller.SslGameEvent2019;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;


public class BotCrashDrawn extends AGameEvent
{
	private final int botY;
	private final int botB;
	private final IVector2 location;
	private final double crashSpeed;
	private final double speedDiff;
	private final double crashAngle;
	
	
	/**
	 * @param yellow
	 * @param blue
	 * @param location
	 * @param crashSpeed [m/s]
	 * @param speedDiff [m/s]
	 * @param crashAngle [rad]
	 */
	public BotCrashDrawn(BotID yellow, BotID blue, IVector2 location, double crashSpeed, double speedDiff,
			double crashAngle)
	{
		if ((yellow.getTeamColor() != ETeamColor.YELLOW))
		{
			throw new AssertionError();
		}
		if ((blue.getTeamColor() != ETeamColor.BLUE))
		{
			throw new AssertionError();
		}
		
		this.botY = yellow.getNumber();
		this.botB = blue.getNumber();
		this.location = location;
		this.crashSpeed = crashSpeed;
		this.speedDiff = speedDiff;
		this.crashAngle = crashAngle;
	}
	
	
	@Override
	public EGameEvent getType()
	{
		return EGameEvent.BOT_CRASH_DRAWN;
	}
	
	
	@Override
	public SslGameEvent2019.GameEvent toProtobuf()
	{
		SslGameEvent2019.GameEvent.Builder builder = SslGameEvent2019.GameEvent.newBuilder();
		builder.getBotCrashDrawnBuilder().setBotBlue(botB).setBotYellow(botY)
				.setCrashSpeed((float) crashSpeed).setSpeedDiff((float) speedDiff)
				.setCrashAngle((float) crashAngle).setLocation(getLocationFromVector(location));
		
		return builder.build();
	}
	
	
	@Override
	public String toString()
	{
		return String.format(
				"Bots %d YELLOW and %d BLUE crashed into each other at %.2f m/s @ %s (Δv: %.2f m/s, angle. %.2f rad)",
				botY, botB, crashSpeed, formatVector(location), speedDiff, crashAngle);
	}
}
