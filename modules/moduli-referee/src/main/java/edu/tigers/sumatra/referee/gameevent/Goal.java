/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.referee.gameevent;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.referee.proto.SslGcGameEvent;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


@Persistent
public class Goal extends AGameEvent
{
	private final ETeamColor team;
	private final ETeamColor kickingTeam;
	private final Integer kickingBot;
	private final IVector2 location;
	private final IVector2 kickLocation;
	private final Float maxBallHeight;
	private final Integer numRobotsByTeam;
	private final Long lastTouchedByTeam;
	private final String message;


	@SuppressWarnings("unsued") // used by berkeley
	protected Goal()
	{
		team = null;
		kickingTeam = null;
		kickingBot = null;
		location = null;
		kickLocation = null;
		maxBallHeight = null;
		numRobotsByTeam = null;
		lastTouchedByTeam = null;
		message = null;
	}


	/**
	 * Default conversion constructor. Note: Called by reflection!
	 *
	 * @param event a protobuf event
	 */
	public Goal(SslGcGameEvent.GameEvent event)
	{
		super(event);
		this.team = toTeamColor(event.getGoal().getByTeam());
		this.kickingTeam = toTeamColor(event.getGoal().getKickingTeam());
		this.kickingBot = event.getGoal().getKickingBot();
		this.location = toVector(event.getGoal().getLocation());
		this.kickLocation = toVector(event.getGoal().getKickLocation());
		this.maxBallHeight = event.getGoal().getMaxBallHeight() * 1000;
		this.numRobotsByTeam = event.getGoal().getNumRobotsByTeam();
		this.lastTouchedByTeam = event.getGoal().getLastTouchByTeam() * 1000;
		this.message = event.getGoal().getMessage();
	}


	public Goal(ETeamColor forTeam, BotID bot, IVector2 location, IVector2 kickLocation,
			double maxBallHeight, int numRobotsByTeam, long lastTouchedByTeam)
	{
		super(EGameEvent.GOAL);
		this.team = forTeam;
		this.kickingTeam = bot == null ? null : bot.getTeamColor();
		this.kickingBot = bot == null ? null : bot.getNumber();
		this.location = location;
		this.kickLocation = kickLocation;
		this.maxBallHeight = (float) maxBallHeight;
		this.numRobotsByTeam = numRobotsByTeam;
		this.lastTouchedByTeam = lastTouchedByTeam;
		this.message = "";
	}


	@Override
	public SslGcGameEvent.GameEvent toProtobuf()
	{
		SslGcGameEvent.GameEvent.Builder builder = SslGcGameEvent.GameEvent.newBuilder();
		builder.setType(SslGcGameEvent.GameEvent.Type.GOAL);
		builder.getGoalBuilder()
				.setByTeam(getTeam(team))
				.setMaxBallHeight(maxBallHeight / 1000.0f)
				.setNumRobotsByTeam(numRobotsByTeam)
				.setLastTouchByTeam(lastTouchedByTeam / 1000);

		if (kickingTeam != null)
		{
			builder.getGoalBuilder().setKickingTeam(getTeam(kickingTeam));
		}
		if (kickingBot != null)
		{
			builder.getGoalBuilder().setKickingBot(kickingBot);
		}
		if (location != null)
		{
			builder.getGoalBuilder().setLocation(getLocationFromVector(location));
		}
		if (kickLocation != null)
		{
			builder.getGoalBuilder().setKickLocation(getLocationFromVector(kickLocation));
		}
		if (message != null)
		{
			builder.getGoalBuilder().setMessage(message);
		}
		return builder.build();
	}


	@Override
	public String getDescription()
	{
		return String.format("Bot %d %s scored goal for %s (%s -> %s)",
				kickingBot, kickingTeam, team,
				formatVector(kickLocation), formatVector(location));
	}


	public ETeamColor getTeam()
	{
		return team;
	}


	@Override
	public String toString()
	{
		return new ToStringBuilder(this)
				.append("team", team)
				.append("kickingTeam", kickingTeam)
				.append("kickingBot", kickingBot)
				.append("location", location)
				.append("kickLocation", kickLocation)
				.append("maxBallHeight", maxBallHeight)
				.append("numRobotsByTeam", numRobotsByTeam)
				.append("lastTouchedByTeam", lastTouchedByTeam)
				.append("message", message)
				.toString();
	}


	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
			return true;

		if (o == null || getClass() != o.getClass())
			return false;

		final Goal goal = (Goal) o;

		return new EqualsBuilder()
				.appendSuper(super.equals(o))
				.append(team, goal.team)
				.append(kickingBot, goal.kickingBot)
				.append(kickingTeam, goal.kickingTeam)
				.append(location, goal.location)
				.append(kickLocation, goal.kickLocation)
				.append(maxBallHeight, goal.maxBallHeight)
				.append(numRobotsByTeam, goal.numRobotsByTeam)
				.append(lastTouchedByTeam, goal.lastTouchedByTeam)
				.append(message, goal.message)
				.isEquals();
	}


	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(17, 37)
				.appendSuper(super.hashCode())
				.append(team)
				.append(kickingTeam)
				.append(kickingBot)
				.append(location)
				.append(kickLocation)
				.append(maxBallHeight)
				.append(numRobotsByTeam)
				.append(lastTouchedByTeam)
				.append(message)
				.toHashCode();
	}
}
