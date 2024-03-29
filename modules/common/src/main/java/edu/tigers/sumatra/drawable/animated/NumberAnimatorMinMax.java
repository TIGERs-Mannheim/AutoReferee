/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.drawable.animated;

import com.sleepycat.persist.model.Persistent;


/**
 * This animator returns a varying number between specified min and max.
 * Changing according to the used animation timer.
 * 
 * @author AndreR <andre@ryll.cc>
 */
@Persistent
public class NumberAnimatorMinMax implements INumberAnimator
{
	private final double min;
	private final double max;
	private final IAnimationTimer timer;
	
	
	@SuppressWarnings("unused")
	private NumberAnimatorMinMax()
	{
		min = 0;
		max = 1;
		timer = null;
	}
	
	
	/**
	 * @param min
	 * @param max
	 * @param timer
	 */
	public NumberAnimatorMinMax(final double min, final double max, final IAnimationTimer timer)
	{
		this.min = min;
		this.max = max;
		this.timer = timer;
	}
	
	
	@Override
	public double getNumber()
	{
		double counterValue = timer.getTimerValue();
		
		return ((1.0f - counterValue) * min) + (counterValue * max);
	}
	
}
