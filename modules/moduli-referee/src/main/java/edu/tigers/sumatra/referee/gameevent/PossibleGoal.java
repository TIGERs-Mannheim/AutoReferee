/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.referee.gameevent;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.SslGameEvent;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;


@Persistent
public class PossibleGoal extends AGameEvent
{
	private final ETeamColor team;
	private final int bot;
	private final IVector2 location;
	private final IVector2 kickLocation;
	
	
	@SuppressWarnings("unsued") // used by berkeley
	protected PossibleGoal()
	{
		team = null;
		bot = 0;
		location = null;
		kickLocation = null;
	}
	
	
	/**
	 * Default conversion constructor. Note: Called by reflection!
	 *
	 * @param event a protobuf event
	 */
	public PossibleGoal(SslGameEvent.GameEvent event)
	{
		super(event);
		this.team = toTeamColor(event.getPossibleGoal().getByTeam());
		this.bot = event.getPossibleGoal().getByBot();
		this.location = toVector(event.getPossibleGoal().getLocation());
		this.kickLocation = toVector(event.getPossibleGoal().getKickLocation());
	}
	
	
	public PossibleGoal(BotID bot, IVector2 location, IVector2 kickLocation)
	{
		super(EGameEvent.POSSIBLE_GOAL);
		this.team = bot.getTeamColor();
		this.bot = bot.getNumber();
		this.location = location;
		this.kickLocation = kickLocation;
	}
	
	
	@Override
	public SslGameEvent.GameEvent toProtobuf()
	{
		SslGameEvent.GameEvent.Builder builder = SslGameEvent.GameEvent.newBuilder();
		builder.setType(SslGameEvent.GameEventType.POSSIBLE_GOAL);
		builder.getPossibleGoalBuilder()
				.setByBot(bot)
				.setByTeam(getTeam(team))
				.setLocation(getLocationFromVector(location))
				.setKickLocation(getLocationFromVector(kickLocation));
		
		return builder.build();
	}
	
	
	@Override
	public String toString()
	{
		return String.format("Bot %d %s scored possible goal (%s -> %s)", bot, team,
				formatVector(kickLocation), formatVector(location));
	}
	
	
	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
			return true;
		
		if (o == null || getClass() != o.getClass())
			return false;
		
		final PossibleGoal goal = (PossibleGoal) o;
		
		return new EqualsBuilder()
				.appendSuper(super.equals(o))
				.append(bot, goal.bot)
				.append(team, goal.team)
				.append(location, goal.location)
				.append(kickLocation, goal.kickLocation)
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
				.append(kickLocation)
				.toHashCode();
	}
}
