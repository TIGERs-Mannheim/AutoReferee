/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.vision.data;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;


/**
 * Ball trajectory base class.
 */
public abstract class ABallTrajectory
{
	protected IVector3 kickPos;
	protected IVector3 kickVel;
	protected long kickTimestamp;


	/**
	 * Get state at specific timestamp.
	 *
	 * @param timestamp
	 * @return
	 */
	public abstract BallTrajectoryState getStateAtTimestamp(final long timestamp);


	/**
	 * @return Kick position in [mm]
	 */
	public IVector2 getKickPos()
	{
		return kickPos.getXYVector();
	}


	/**
	 * @return Kick velocity in [mm/s]
	 */
	public IVector3 getKickVel()
	{
		return kickVel;
	}


	/**
	 * @return the kickTimestamp
	 */
	public long getKickTimestamp()
	{
		return kickTimestamp;
	}
}
