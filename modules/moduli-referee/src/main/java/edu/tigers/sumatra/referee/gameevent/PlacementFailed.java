/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.referee.gameevent;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.SslGcGameEvent;
import edu.tigers.sumatra.ids.ETeamColor;


@Persistent
public class PlacementFailed extends AGameEvent
{
	private final ETeamColor team;
	private final double remainingDistance;


	@SuppressWarnings("unsued") // used by berkeley
	protected PlacementFailed()
	{
		team = null;
		remainingDistance = 0;
	}


	/**
	 * Default conversion constructor. Note: Called by reflection!
	 *
	 * @param event a protobuf event
	 */
	public PlacementFailed(SslGcGameEvent.GameEvent event)
	{
		super(event);
		this.team = toTeamColor(event.getPlacementFailed().getByTeam());
		this.remainingDistance = toDistance(event.getPlacementFailed().getRemainingDistance());
	}


	public PlacementFailed(final ETeamColor team, final double remainingDistance)
	{
		super(EGameEvent.PLACEMENT_FAILED);
		this.team = team;
		this.remainingDistance = remainingDistance;
	}


	@Override
	public SslGcGameEvent.GameEvent toProtobuf()
	{
		SslGcGameEvent.GameEvent.Builder builder = SslGcGameEvent.GameEvent.newBuilder();
		builder.setType(SslGcGameEvent.GameEvent.Type.PLACEMENT_FAILED);
		builder.getPlacementFailedBuilder()
				.setByTeam(getTeam(team))
				.setRemainingDistance((float) remainingDistance / 1000f)
				.build();

		return builder.build();
	}


	@Override
	public String getDescription()
	{
		return String.format("Placement failed by %s: %.2f mm remaining", team, remainingDistance);
	}


	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
			return true;

		if (o == null || getClass() != o.getClass())
			return false;

		final PlacementFailed that = (PlacementFailed) o;

		return new EqualsBuilder()
				.appendSuper(super.equals(o))
				.append(remainingDistance, that.remainingDistance)
				.append(team, that.team)
				.isEquals();
	}


	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(17, 37)
				.appendSuper(super.hashCode())
				.append(team)
				.append(remainingDistance)
				.toHashCode();
	}
}
