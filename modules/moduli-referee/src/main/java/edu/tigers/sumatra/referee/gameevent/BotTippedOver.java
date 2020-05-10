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
public class BotTippedOver extends AGameEvent
{
	private final ETeamColor team;
	private final int bot;
	private final IVector2 location;
	private final IVector2 ballLocation;


	@SuppressWarnings("unsued") // used by berkeley
	protected BotTippedOver()
	{
		team = null;
		bot = 0;
		location = null;
		ballLocation = null;
	}


	/**
	 * Default conversion constructor. Note: Called by reflection!
	 *
	 * @param event a protobuf event
	 */
	public BotTippedOver(SslGcGameEvent.GameEvent event)
	{
		super(event);
		this.team = toTeamColor(event.getBotTippedOver().getByTeam());
		this.bot = event.getBotTippedOver().getByBot();
		this.location = toVector(event.getBotTippedOver().getLocation());
		this.ballLocation = toVector(event.getBotTippedOver().getBallLocation());
	}


	/**
	 * @param bot
	 * @param location
	 */
	public BotTippedOver(BotID bot, IVector2 location, IVector2 ballLocation)
	{
		super(EGameEvent.BOT_TIPPED_OVER);
		this.team = bot.getTeamColor();
		this.bot = bot.getNumber();
		this.location = location;
		this.ballLocation = ballLocation;
	}


	@Override
	public SslGcGameEvent.GameEvent toProtobuf()
	{
		SslGcGameEvent.GameEvent.Builder builder = SslGcGameEvent.GameEvent.newBuilder();
		builder.setType(SslGcGameEvent.GameEvent.Type.BOT_TIPPED_OVER);
		builder.getBotTippedOverBuilder().setByBot(bot).setByTeam(getTeam(team))
				.setLocation(getLocationFromVector(location))
				.setBallLocation(getLocationFromVector(ballLocation));

		return builder.build();
	}


	@Override
	public String getDescription()
	{
		return String.format("Bot %d of team %s tipped over @ %s", bot, team, location);
	}


	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
			return true;

		if (o == null || getClass() != o.getClass())
			return false;

		final BotTippedOver that = (BotTippedOver) o;

		return new EqualsBuilder()
				.appendSuper(super.equals(o))
				.append(bot, that.bot)
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
				.append(ballLocation)
				.toHashCode();
	}
}
