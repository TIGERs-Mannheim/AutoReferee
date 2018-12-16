/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.events.data;

import edu.tigers.autoreferee.engine.events.EGameEvent;
import edu.tigers.sumatra.gamecontroller.SslGameEvent2019;
import edu.tigers.sumatra.ids.ETeamColor;


public class UnsportingBehavior extends AGameEvent
{
	
	private final boolean major;
	private final ETeamColor team;
	private final String reason;
	
	
	/**
	 * @param major
	 * @param team
	 * @param reason
	 */
	public UnsportingBehavior(boolean major, ETeamColor team, String reason)
	{
		this.team = team;
		this.major = major;
		this.reason = reason;
	}
	
	
	@Override
	public EGameEvent getType()
	{
		return major ? EGameEvent.UNSPORTING_BEHAVIOR_MAJOR : EGameEvent.UNSPORTING_BEHAVIOR_MINOR;
	}
	
	
	@Override
	public SslGameEvent2019.GameEvent toProtobuf()
	{
		SslGameEvent2019.GameEvent.Builder eventBuilder = SslGameEvent2019.GameEvent.newBuilder();
		if (major)
		{
			eventBuilder.getUnsportingBehaviorMajorBuilder().setByTeam(getTeam(team)).setReason(reason);
		} else
		{
			eventBuilder.getUnsportingBehaviorMinorBuilder().setByTeam(getTeam(team)).setReason(reason);
		}
		
		return eventBuilder.build();
	}
	
	
	@Override
	public String toString()
	{
		return String.format("Unsporting behavior of team %s: %s (%s)", team, reason, major ? "MAJOR" : "minor");
	}
}
