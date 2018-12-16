/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.events.data;

import edu.tigers.autoreferee.engine.events.EGameEvent;
import edu.tigers.sumatra.gamecontroller.SslGameEvent2019;


public class Prepared extends AGameEvent
{
	
	private final double timeTaken;
	
	
	/**
	 * @param timeTaken [s]
	 */
	public Prepared(double timeTaken)
	{
		this.timeTaken = timeTaken;
	}
	
	
	@Override
	public EGameEvent getType()
	{
		return EGameEvent.PREPARED;
	}
	
	
	@Override
	public SslGameEvent2019.GameEvent toProtobuf()
	{
		SslGameEvent2019.GameEvent.Builder eventBuilder = SslGameEvent2019.GameEvent.newBuilder();
		eventBuilder.getPreparedBuilder().setTimeTaken((float) timeTaken);
		
		return eventBuilder.build();
	}
	
	
	@Override
	public String toString()
	{
		return String.format("Both teams are prepared after %.2f s", timeTaken);
	}
}
