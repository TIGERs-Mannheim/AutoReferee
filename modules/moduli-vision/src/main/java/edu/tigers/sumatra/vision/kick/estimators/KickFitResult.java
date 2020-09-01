/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.vision.kick.estimators;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.vision.data.ABallTrajectory;
import edu.tigers.sumatra.vision.data.BallTrajectoryState;
import lombok.Value;

import java.util.List;


/**
 * Result of a fitted kick.
 */
@Value
public class KickFitResult
{
	List<IVector2> groundProjection;
	double avgDistance;
	ABallTrajectory trajectory;


	/**
	 * @return the kickPos
	 */
	public IVector2 getKickPos()
	{
		return trajectory.getKickPos();
	}


	/**
	 * @return the kickVel
	 */
	public IVector3 getKickVel()
	{
		return trajectory.getKickVel();
	}


	/**
	 * @return
	 */
	public long getKickTimestamp()
	{
		return trajectory.getKickTimestamp();
	}


	/**
	 * Get ball state at specific timestamp.
	 *
	 * @param timestamp
	 * @return
	 */
	public BallTrajectoryState getState(final long timestamp)
	{
		return trajectory.getStateAtTimestamp(timestamp);
	}
}
