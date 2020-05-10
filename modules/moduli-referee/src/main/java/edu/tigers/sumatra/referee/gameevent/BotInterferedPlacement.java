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
public class BotInterferedPlacement extends AGameEvent
{
	private final ETeamColor team;
	private final int bot;
	private final IVector2 location;


	@SuppressWarnings("unsued") // used by berkeley
	protected BotInterferedPlacement()
	{
		team = null;
		bot = 0;
		location = null;
	}


	/**
	 * Default conversion constructor. Note: Called by reflection!
	 *
	 * @param event a protobuf event
	 */
	public BotInterferedPlacement(SslGcGameEvent.GameEvent event)
	{
		super(event);
		this.team = toTeamColor(event.getBotInterferedPlacement().getByTeam());
		this.bot = event.getBotInterferedPlacement().getByBot();
		this.location = toVector(event.getBotInterferedPlacement().getLocation());
	}


	public BotInterferedPlacement(BotID bot, IVector2 location)
	{
		super(EGameEvent.BOT_INTERFERED_PLACEMENT);
		this.team = bot.getTeamColor();
		this.bot = bot.getNumber();
		this.location = location;
	}


	@Override
	public SslGcGameEvent.GameEvent toProtobuf()
	{
		SslGcGameEvent.GameEvent.Builder builder = SslGcGameEvent.GameEvent.newBuilder();
		builder.setType(SslGcGameEvent.GameEvent.Type.BOT_INTERFERED_PLACEMENT);
		builder.getBotInterferedPlacementBuilder().setByBot(bot).setByTeam(getTeam(team))
				.setLocation(getLocationFromVector(location));

		return builder.build();
	}


	@Override
	public String getDescription()
	{
		return String.format("Bot %d %s interfered ball placement @ %s", bot, team, formatVector(location));
	}


	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
			return true;

		if (o == null || getClass() != o.getClass())
			return false;

		final BotInterferedPlacement that = (BotInterferedPlacement) o;

		return new EqualsBuilder()
				.appendSuper(super.equals(o))
				.append(bot, that.bot)
				.append(team, that.team)
				.append(location, that.location)
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
				.toHashCode();
	}
}
