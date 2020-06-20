/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.referee.gameevent;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.referee.proto.SslGcGameEvent;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;


@Persistent
public class NoProgressInGame extends AGameEvent
{
	private final double time;
	private final IVector2 location;


	@SuppressWarnings("unsued") // used by berkeley
	protected NoProgressInGame()
	{
		time = 0;
		location = null;
	}


	/**
	 * Default conversion constructor. Note: Called by reflection!
	 *
	 * @param event a protobuf event
	 */
	public NoProgressInGame(SslGcGameEvent.GameEvent event)
	{
		super(event);
		this.time = event.getNoProgressInGame().getTime();
		this.location = toVector(event.getNoProgressInGame().getLocation());
	}


	/**
	 * @param pos
	 * @param time [s]
	 */
	public NoProgressInGame(IVector2 pos, double time)
	{
		super(EGameEvent.NO_PROGRESS_IN_GAME);
		this.location = pos;
		this.time = time;
	}


	@Override
	public SslGcGameEvent.GameEvent toProtobuf()
	{
		SslGcGameEvent.GameEvent.Builder builder = SslGcGameEvent.GameEvent.newBuilder();
		builder.setType(SslGcGameEvent.GameEvent.Type.NO_PROGRESS_IN_GAME);
		builder.getNoProgressInGameBuilder().setTime((float) time).setLocation(getLocationFromVector(location));
		return builder.build();
	}


	@Override
	public String getDescription()
	{
		return String.format("No progress in Game for %.2f s @ %s", time, formatVector(location));
	}


	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
			return true;

		if (o == null || getClass() != o.getClass())
			return false;

		final NoProgressInGame that = (NoProgressInGame) o;

		return new EqualsBuilder()
				.appendSuper(super.equals(o))
				.append(time, that.time)
				.append(location, that.location)
				.isEquals();
	}


	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(17, 37)
				.appendSuper(super.hashCode())
				.append(time)
				.append(location)
				.toHashCode();
	}
}
