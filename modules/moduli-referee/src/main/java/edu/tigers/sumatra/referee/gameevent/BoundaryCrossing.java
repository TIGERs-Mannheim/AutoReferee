/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.referee.gameevent;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.SslGcGameEvent;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;


@Persistent
public class BoundaryCrossing extends AGameEvent
{
	private final ETeamColor team;
	private final IVector2 location;

	@SuppressWarnings("unsued") // used by berkeley
	protected BoundaryCrossing()
	{
		team = null;
		location = null;
	}


	/**
	 * Default conversion constructor. Note: Called by reflection!
	 *
	 * @param event a protobuf event
	 */
	public BoundaryCrossing(SslGcGameEvent.GameEvent event)
	{
		super(event);
		this.team = toTeamColor(event.getBoundaryCrossing().getByTeam());
		this.location = toVector(event.getBoundaryCrossing().getLocation());
	}


	public BoundaryCrossing(final ETeamColor team, final IVector2 location)
	{
		super(EGameEvent.BOUNDARY_CROSSING);
		this.team = team;
		this.location = location;
	}


	@Override
	public SslGcGameEvent.GameEvent toProtobuf()
	{
		SslGcGameEvent.GameEvent.Builder builder = SslGcGameEvent.GameEvent.newBuilder();
		builder.setType(SslGcGameEvent.GameEvent.Type.BOUNDARY_CROSSING);
		builder.getBoundaryCrossingBuilder()
				.setByTeam(getTeam(team));

		if (location != null)
		{
			builder.getBoundaryCrossingBuilder().setLocation(getLocationFromVector(location));
		}

		return builder.build();
	}


	@Override
	public String getDescription()
	{
		return String.format("Team %s chipped ball over the boundary at %s", team, formatVector(location));
	}


	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
			return true;

		if (o == null || getClass() != o.getClass())
			return false;

		final BoundaryCrossing that = (BoundaryCrossing) o;

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
