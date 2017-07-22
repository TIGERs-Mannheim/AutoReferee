/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.statistics;

import com.sleepycat.persist.model.Persistent;


/**
 * Simple helper class for percentage calculation and data holding
 * 
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
@Persistent(version = 1)
public class Percentage
{
	private int current = 0;
	private int all = 1;
	
	
	/**
	 * Returns current value of this Percentage
	 * 
	 * @return
	 */
	public int getCurrent()
	{
		return current;
	}
	
	
	/**
	 * Increments current value by one
	 * 
	 * @return this for chaining
	 */
	public Percentage inc()
	{
		current = current + 1;
		return this;
	}
	
	
	/**
	 * @param all , base value, do not update with all<=0
	 */
	public void setAll(final int all)
	{
		this.all = all;
	}
	
	
	/**
	 * @return the percent
	 */
	public double getPercent()
	{
		return (double) current / all;
	}
}
