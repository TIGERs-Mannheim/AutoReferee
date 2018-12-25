/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.events;

import edu.tigers.sumatra.gamecontroller.SslGameEvent2019;
import edu.tigers.sumatra.ids.ETeamColor;


public class PlacementSucceeded extends AGameEvent
{
	
	private final ETeamColor team;
	private final double took;
	private final double precision;
	private final double distance;
	
	
	/**
	 * @param team
	 * @param took [s]
	 * @param precision [mm]
	 * @param distance [mm]
	 */
	public PlacementSucceeded(ETeamColor team, double took, double precision, double distance)
	{
		this.team = team;
		this.took = took;
		this.precision = precision;
		this.distance = distance;
	}
	
	
	@Override
	public EGameEvent getType()
	{
		return EGameEvent.PLACEMENT_SUCCEEDED;
	}
	
	
	@Override
	public SslGameEvent2019.GameEvent toProtobuf()
	{
		SslGameEvent2019.GameEvent.Builder builder = SslGameEvent2019.GameEvent.newBuilder();
		builder.setType(SslGameEvent2019.GameEventType.PLACEMENT_SUCCEEDED);
		builder.getPlacementSucceededBuilder().setByTeam(getTeam(team)).setDistance((float) distance / 1000.f)
				.setPrecision((float) precision / 1000.f).setTimeTaken((float) took);
		
		return builder.build();
	}
	
	
	@Override
	public String toString()
	{
		return String.format("Placement of team %s successful with a precision of %.2f mm over %.2f mm (took: %.2f s)",
				team, precision, distance, took);
	}
}
