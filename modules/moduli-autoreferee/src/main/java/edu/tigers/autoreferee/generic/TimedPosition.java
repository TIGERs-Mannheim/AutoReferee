/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.generic;


import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;


/**
 * Position which carries a timestamp corresponding to the time it was captured
 */
public class TimedPosition
{
	private final long timestamp;
	private final IVector2 position;
	
	
	/**
	 * default constructor
	 */
	public TimedPosition()
	{
		timestamp = 0;
		position = Vector2f.ZERO_VECTOR;
	}
	
	
	/**
	 * @param timestamp
	 * @param position
	 */
	public TimedPosition(final long timestamp, final IVector2 position)
	{
		this.position = position;
		this.timestamp = timestamp;
	}
	
	
	/**
	 * @param currentTimestamp
	 * @return the age in [s]
	 */
	public double getAge(final long currentTimestamp)
	{
		return (currentTimestamp - timestamp) / 1e9;
	}
	
	
	/**
	 * @return the ts
	 */
	public long getTimestamp()
	{
		return timestamp;
	}
	
	
	public IVector2 getPos()
	{
		return position;
	}
}
