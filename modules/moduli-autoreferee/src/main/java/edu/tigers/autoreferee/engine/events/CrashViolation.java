/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.events;

import java.text.DecimalFormat;

import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.sumatra.ids.BotID;


public class CrashViolation extends GameEvent
{
	private final DecimalFormat format = new DecimalFormat("0.000");
	private BotID secondResponsibleBot;
	private double collisionSpeed;
	private double speedDifference;
	
	
	public CrashViolation(final EGameEvent eventType, final long timestamp, final BotID responsibleBot,
			final double collisionSpeed, final double speedDifference,
			final FollowUpAction followUp)
	{
		super(eventType, timestamp, responsibleBot, followUp);
		this.collisionSpeed = collisionSpeed;
		this.speedDifference = speedDifference;
	}
	
	
	public CrashViolation(final EGameEvent eventType, final long timestamp, final BotID responsibleBot,
			final BotID responsibleBot2, final double collisionSpeed, final double speedDifference,
			final FollowUpAction followUp)
	{
		super(eventType, timestamp, responsibleBot, followUp);
		secondResponsibleBot = responsibleBot2;
		this.collisionSpeed = collisionSpeed;
		this.speedDifference = speedDifference;
	}
	
	
	@Override
	protected String generateLogString()
	{
		if (secondResponsibleBot != null)
		{
			return generateBothTeamsViolationString();
		}
		return generateSingleBotViolationString();
	}
	
	
	private String generateSingleBotViolationString()
	{
		return super.generateLogString()
				+ " | CollisionSpeed: "
				+ format.format(collisionSpeed)
				+ "m/s"
				+ " | SpeedDifference: "
				+ format.format(speedDifference)
				+ "m/s";
	}
	
	
	private String generateBothTeamsViolationString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(getType());
		if (getResponsibleBot().isPresent())
		{
			builder.append(" | Bots: ");
			builder.append(getResponsibleBot().get().getNumber());
			builder.append(" ");
			builder.append(getResponsibleBot().get().getTeamColor());
			
			builder.append(" & ");
			builder.append(secondResponsibleBot.getNumber());
			builder.append(" ");
			builder.append(secondResponsibleBot.getTeamColor());
			
			builder.append(" | CollisionSpeed: ");
			builder.append(format.format(collisionSpeed));
			builder.append("m/s");
			
			builder.append(" | SpeedDifference: ");
			builder.append(format.format(speedDifference));
			builder.append("m/s");
		}
		return builder.toString();
	}
}
