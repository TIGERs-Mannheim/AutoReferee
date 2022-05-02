/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.trajectory;

import com.sleepycat.persist.model.Persistent;
import lombok.RequiredArgsConstructor;

import java.util.List;


/**
 * Wrap a trajectory around some start and end time
 *
 * @param <R> return type
 */
@Persistent
@RequiredArgsConstructor
public class TrajectoryWrapper<R> implements ITrajectory<R>
{
	private final ITrajectory<R> traj;
	private final double tStart;
	private final double tEnd;


	@SuppressWarnings("unused") // berkeley
	private TrajectoryWrapper()
	{
		traj = new StubTrajectory<>();
		tStart = 0;
		tEnd = 0;
	}


	private double getTime(final double t)
	{
		return Math.min(Math.max(0, t) + tStart, tEnd);
	}


	@Override
	public R getPositionMM(final double t)
	{
		return traj.getPositionMM(getTime(t));
	}


	@Override
	public R getPosition(final double t)
	{
		return traj.getPosition(getTime(t));
	}


	@Override
	public R getVelocity(final double t)
	{
		return traj.getVelocity(getTime(t));
	}


	@Override
	public R getAcceleration(final double t)
	{
		return traj.getAcceleration(getTime(t));
	}


	@Override
	public double getTotalTime()
	{
		return tEnd - tStart;
	}


	@Override
	public TrajectoryWrapper<R> mirrored()
	{
		return new TrajectoryWrapper<>(traj.mirrored(), tStart, tEnd);
	}


	@Override
	public PosVelAcc<R> getValuesAtTime(final double tt)
	{
		throw new IllegalStateException("Not implemented");
	}


	@Override
	public List<Double> getTimeSections()
	{
		throw new IllegalStateException("Not implemented");
	}
}
