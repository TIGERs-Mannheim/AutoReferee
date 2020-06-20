/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.referee.gameevent;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.referee.proto.SslGcGameEvent;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;


@Persistent
public class PenaltyKickFailed extends AGameEvent
{
	private final ETeamColor team;
	private final IVector2 location;

	@SuppressWarnings("unsued") // used by berkeley
	protected PenaltyKickFailed()
	{
		team = null;
		location = null;
	}


	/**
	 * Default conversion constructor. Note: Called by reflection!
	 *
	 * @param event a protobuf event
	 */
	public PenaltyKickFailed(SslGcGameEvent.GameEvent event)
	{
		super(event);
		this.team = toTeamColor(event.getPenaltyKickFailed().getByTeam());
		this.location = toVector(event.getPenaltyKickFailed().getLocation());
	}


	public PenaltyKickFailed(final ETeamColor team, final IVector2 location)
	{
		super(EGameEvent.PENALTY_KICK_FAILED);
		this.team = team;
		this.location = location;
	}


	@Override
	public SslGcGameEvent.GameEvent toProtobuf()
	{
		SslGcGameEvent.GameEvent.Builder builder = SslGcGameEvent.GameEvent.newBuilder();
		builder.setType(SslGcGameEvent.GameEvent.Type.PENALTY_KICK_FAILED);
		builder.getPenaltyKickFailedBuilder()
				.setByTeam(getTeam(team));

		if (location != null)
		{
			builder.getPenaltyKickFailedBuilder().setLocation(getLocationFromVector(location));
		}

		return builder.build();
	}


	@Override
	public String getDescription()
	{
		return String.format("Penalty kick failed by %s at %s", team, formatVector(location));
	}


	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
			return true;

		if (o == null || getClass() != o.getClass())
			return false;

		final PenaltyKickFailed that = (PenaltyKickFailed) o;

		return new EqualsBuilder()
				.appendSuper(super.equals(o))
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
				.append(location)
				.toHashCode();
	}
}
