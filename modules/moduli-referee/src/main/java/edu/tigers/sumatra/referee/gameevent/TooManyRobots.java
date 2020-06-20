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
public class TooManyRobots extends AGameEvent
{
	private final ETeamColor team;
	private final int numRobotsAllowed;
	private final int numRobotsOnField;
	private final IVector2 ballLocation;


	@SuppressWarnings("unsued") // used by berkeley
	protected TooManyRobots()
	{
		team = null;
		numRobotsAllowed = 0;
		numRobotsOnField = 0;
		ballLocation = null;
	}


	/**
	 * Default conversion constructor. Note: Called by reflection!
	 *
	 * @param event a protobuf event
	 */
	public TooManyRobots(SslGcGameEvent.GameEvent event)
	{
		super(event);
		this.team = toTeamColor(event.getTooManyRobots().getByTeam());
		this.numRobotsAllowed = event.getTooManyRobots().getNumRobotsAllowed();
		this.numRobotsOnField = event.getTooManyRobots().getNumRobotsOnField();
		this.ballLocation = toVector(event.getTooManyRobots().getBallLocation());
	}


	public TooManyRobots(ETeamColor team, int numRobotsAllowed, int numRobotsOnField, IVector2 ballLocation)
	{
		super(EGameEvent.TOO_MANY_ROBOTS);
		this.team = team;
		this.numRobotsAllowed = numRobotsAllowed;
		this.numRobotsOnField = numRobotsOnField;
		this.ballLocation = ballLocation;
	}


	@Override
	public SslGcGameEvent.GameEvent toProtobuf()
	{
		SslGcGameEvent.GameEvent.Builder builder = SslGcGameEvent.GameEvent.newBuilder();
		builder.setType(SslGcGameEvent.GameEvent.Type.TOO_MANY_ROBOTS);
		builder.getTooManyRobotsBuilder()
				.setByTeam(getTeam(team))
				.setNumRobotsAllowed(numRobotsAllowed)
				.setNumRobotsOnField(numRobotsOnField)
				.setBallLocation(getLocationFromVector(ballLocation));

		return builder.build();
	}


	@Override
	public String getDescription()
	{
		return String.format("Team %s has %d robots on the field, but only %d are allowed", team, numRobotsOnField,
				numRobotsAllowed);
	}


	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
			return true;

		if (o == null || getClass() != o.getClass())
			return false;

		final TooManyRobots that = (TooManyRobots) o;

		return new EqualsBuilder()
				.appendSuper(super.equals(o))
				.append(team, that.team)
				.append(numRobotsAllowed, that.numRobotsAllowed)
				.append(numRobotsOnField, that.numRobotsOnField)
				.append(ballLocation, that.ballLocation)
				.isEquals();
	}


	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(17, 37)
				.appendSuper(super.hashCode())
				.append(team)
				.append(numRobotsAllowed)
				.append(numRobotsOnField)
				.append(ballLocation)
				.toHashCode();
	}
}
