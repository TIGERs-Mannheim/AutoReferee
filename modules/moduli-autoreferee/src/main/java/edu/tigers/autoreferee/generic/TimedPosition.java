/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Sep 28, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.generic;


import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * Vector subclass which carries a timestamp corresponding to the time the vector was captured
 * 
 * @author "Lukas Magel"
 */
public class TimedPosition
{
	private final long		timestamp;
	private final IVector2	position;
	
	
	/**
	 * 
	 */
	public TimedPosition()
	{
		timestamp = 0;
		position = Vector2.ZERO_VECTOR;
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
