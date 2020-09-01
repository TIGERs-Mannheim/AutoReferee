/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.ball.trajectory;

import edu.tigers.sumatra.math.line.v2.IHalfLine;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector;
import edu.tigers.sumatra.math.vector.IVector2;

import java.util.Collections;
import java.util.List;


/**
 * Common base implementation for ball trajectories.
 */
public abstract class ABallTrajectory implements IBallTrajectory
{
	/**
	 * Get the time when the ball comes to rest.
	 *
	 * @return
	 */
	public abstract double getTimeAtRest();


	/**
	 * Get the required time for the ball to travel the given distance.<br>
	 * If the distance can not be achieved, the result will be Infinity.
	 *
	 * @param distance Distance in [mm], must be positive.
	 * @return the time in [s] that is need to travel the distance, Inf if the ball stops before reaching the distance.
	 */
	protected abstract double getTimeByDistanceInMillimeters(final double distance);


	/**
	 * Get the time where the ball reaches a given velocity.<br>
	 * If <code>velocity</code> is larger than the current velocity, 0 will be returned
	 *
	 * @param velocity Velocity in [mm/s], must be positive.
	 * @return the time in [s] when the ball's velocity is smaller than or equal to the targetVelocity for the first
	 * time.
	 */
	protected abstract double getTimeByVelocityInMillimetersPerSec(final double velocity);


	@Override
	public IVector getPosByTime(final double time)
	{
		return getMilliStateAtTime(getTKickToNow() + time).getPos();
	}


	@Override
	public IVector getVelByTime(final double time)
	{
		return getMilliStateAtTime(getTKickToNow() + time).getVel().multiplyNew(0.001);
	}


	@Override
	public IVector getAccByTime(final double time)
	{
		return getMilliStateAtTime(getTKickToNow() + time).getAcc().multiplyNew(0.001);
	}


	@Override
	public double getSpinByTime(final double time)
	{
		return getMilliStateAtTime(getTKickToNow() + time).getSpin();
	}


	@Override
	public IVector getPosByVel(final double targetVelocity)
	{
		if (getAbsVelByTime(0) < targetVelocity)
		{
			return getPosByTime(0);
		}

		double time = getTimeByVel(targetVelocity);
		return getPosByTime(time);
	}


	@Override
	public double getTimeByDist(final double travelDistance)
	{
		double distToNow = getKickPos().getXYVector().distanceTo(getPosByTime(0).getXYVector());
		return getTimeByDistanceInMillimeters(travelDistance + distToNow) - getTKickToNow();
	}


	@Override
	public double getTimeByVel(final double targetVelocity)
	{
		return getTimeByVelocityInMillimetersPerSec(targetVelocity * 1000.0) - getTKickToNow();
	}


	@Override
	public double getAbsVelByDist(final double distance)
	{
		double time = getTimeByDist(distance);
		return getAbsVelByTime(time);
	}


	@Override
	public double getAbsVelByPos(final IVector2 targetPosition)
	{
		return getAbsVelByDist(getPosByTime(0).getXYVector().distanceTo(targetPosition));
	}


	@Override
	public double getTimeByPos(final IVector2 targetPosition)
	{
		return getTimeByDist(getPosByTime(0).getXYVector().distanceTo(targetPosition));
	}


	@Override
	public double getDistByTime(final double time)
	{
		return getPosByTime(0).getXYVector().distanceTo(getPosByTime(time).getXYVector());
	}


	@Override
	public double getAbsVelByTime(final double time)
	{
		return getMilliStateAtTime(getTKickToNow() + time).getVel().getLength2() * 0.001;
	}


	@Override
	public boolean isInterceptableByTime(final double time)
	{
		return getPosByTime(time).getXYZVector().z() < 150;
	}


	@Override
	public boolean isRollingByTime(final double time)
	{
		return getPosByTime(time).getXYZVector().z() < 10;
	}


	@Override
	public IHalfLine getTravelLine()
	{
		IVector2 finalPos = getPosByTime(getTimeAtRest() - getTKickToNow()).getXYVector();
		return Lines.halfLineFromPoints(getPosByTime(0).getXYVector(), finalPos);
	}


	@Override
	public ILineSegment getTravelLineSegment()
	{
		IVector2 finalPos = getPosByTime(getTimeAtRest() - getTKickToNow()).getXYVector();
		return Lines.segmentFromPoints(getPosByTime(0).getXYVector(), finalPos);
	}


	@Override
	public ILineSegment getTravelLineRolling()
	{
		return getTravelLineSegment();
	}


	@Override
	public List<ILineSegment> getTravelLinesInterceptable()
	{
		return Collections.singletonList(getTravelLineSegment());
	}


	@Override
	public List<IVector2> getTouchdownLocations()
	{
		return Collections.emptyList();
	}
}
