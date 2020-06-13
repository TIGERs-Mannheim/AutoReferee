/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.data;

import com.sleepycat.persist.model.Persistent;


@Persistent
public class TrajTrackingQuality
{
	private final double curDistance;
	private final double maxDistance;
	private final double relOnTrajectory;

	@SuppressWarnings("unused")
	public TrajTrackingQuality()
	{
		curDistance = 0;
		maxDistance = 0;
		relOnTrajectory = 0;
	}


	public TrajTrackingQuality(final double curDistance, final double maxDistance, final double relOnTrajectory)
	{
		this.curDistance = curDistance;
		this.maxDistance = maxDistance;
		this.relOnTrajectory = relOnTrajectory;
	}


	public double getCurDistance()
	{
		return curDistance;
	}


	public double getMaxDistance()
	{
		return maxDistance;
	}


	public double getRelOnTrajectory()
	{
		return relOnTrajectory;
	}
}
