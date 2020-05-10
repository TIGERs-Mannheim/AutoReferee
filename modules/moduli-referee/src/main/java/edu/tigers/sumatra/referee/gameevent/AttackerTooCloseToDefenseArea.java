/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.referee.gameevent;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.SslGcGameEvent;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;


@Persistent
public class AttackerTooCloseToDefenseArea extends AGameEvent
{
	private final ETeamColor team;
	private final int bot;
	private final IVector2 location;
	private final double distance;
	private final IVector2 ballLocation;


	@SuppressWarnings("unsued") // used by berkeley
	protected AttackerTooCloseToDefenseArea()
	{
		team = null;
		bot = 0;
		location = null;
		distance = 0;
		ballLocation = null;
	}


	/**
	 * Default conversion constructor. Note: Called by reflection!
	 *
	 * @param event a protobuf event
	 */
	public AttackerTooCloseToDefenseArea(SslGcGameEvent.GameEvent event)
	{
		super(event);
		this.team = toTeamColor(event.getAttackerTooCloseToDefenseArea().getByTeam());
		this.bot = event.getAttackerTooCloseToDefenseArea().getByBot();
		this.location = toVector(event.getAttackerTooCloseToDefenseArea().getLocation());
		this.distance = toDistance(event.getAttackerTooCloseToDefenseArea().getDistance());
		this.ballLocation = toVector(event.getAttackerTooCloseToDefenseArea().getBallLocation());
	}


	/**
	 * @param bot
	 * @param location
	 * @param distance [mm]
	 */
	public AttackerTooCloseToDefenseArea(BotID bot, IVector2 location, double distance, IVector2 ballLocation)
	{
		super(EGameEvent.ATTACKER_TOO_CLOSE_TO_DEFENSE_AREA);
		this.team = bot.getTeamColor();
		this.bot = bot.getNumber();
		this.location = location;
		this.distance = distance;
		this.ballLocation = ballLocation;
	}


	@Override
	public SslGcGameEvent.GameEvent toProtobuf()
	{
		SslGcGameEvent.GameEvent.Builder builder = SslGcGameEvent.GameEvent.newBuilder();
		builder.setType(SslGcGameEvent.GameEvent.Type.ATTACKER_TOO_CLOSE_TO_DEFENSE_AREA);
		builder.getAttackerTooCloseToDefenseAreaBuilder().setByTeam(getTeam(team)).setByBot(bot)
				.setDistance((float) distance / 1000.f)
				.setLocation(getLocationFromVector(location))
				.setBallLocation(getLocationFromVector(ballLocation));

		return builder.build();
	}


	@Override
	public String getDescription()
	{
		return String.format("Attacker %d %s was %.2f mm too close to the penalty area @ %s", bot, team, distance,
				formatVector(location));
	}


	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
			return true;

		if (o == null || getClass() != o.getClass())
			return false;

		final AttackerTooCloseToDefenseArea that = (AttackerTooCloseToDefenseArea) o;

		return new EqualsBuilder()
				.appendSuper(super.equals(o))
				.append(bot, that.bot)
				.append(distance, that.distance)
				.append(team, that.team)
				.append(location, that.location)
				.append(ballLocation, that.ballLocation)
				.isEquals();
	}


	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(17, 37)
				.appendSuper(super.hashCode())
				.append(team)
				.append(bot)
				.append(location)
				.append(distance)
				.append(ballLocation)
				.toHashCode();
	}
}
