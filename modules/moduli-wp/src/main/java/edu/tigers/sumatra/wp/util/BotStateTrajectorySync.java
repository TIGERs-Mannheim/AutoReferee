/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.bot.BotState;
import edu.tigers.sumatra.bot.State;
import edu.tigers.sumatra.filter.DataSync;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.trajectory.ITrajectory;
import edu.tigers.sumatra.wp.data.TrajTrackingQuality;


public class BotStateTrajectorySync
{
	@Configurable(defValue = "0.2", comment = "Time horizon [s] that is buffered (requires Moduli-reload)")
	private static double horizon = 0.2;

	@Configurable(defValue = "2.0", comment = "Time horizon [s] for statistics")
	private static double statisticsHorizon = 2.0;

	@Configurable(defValue = "0.1", comment = "Max time difference [s] between trajectory and measured state")
	private static double maxDt = 0.1;

	@Configurable(defValue = "1.0", comment = "Min velocity [m/s] to consider for calculating maxDiff on trajectory")
	private static double minVel = 1.0;

	@Configurable(defValue = "true", comment = "Prefer the state of the current trajectory that the bot executes")
	private static boolean enabled = true;

	static
	{
		ConfigRegistration.registerClass("wp", BotStateTrajectorySync.class);
	}

	private final DataSync<State> buffer = new DataSync<>(horizon);
	private final List<DistanceSample> distanceSamples = new ArrayList<>();

	public void add(final ITrajectory<IVector3> traj, final long timestamp)
	{
		if (!enabled)
		{
			buffer.reset();
			return;
		}
		State state = trajectoryToState(traj);
		buffer.add(timestamp, state);
	}


	public Optional<State> updateState(final long timestamp, final double feedbackDelay, final BotState state)
	{
		State currentState = getState(timestamp, feedbackDelay).orElse(null);
		DistanceSample sample = new DistanceSample();
		sample.timestamp = timestamp;
		addSample(sample);
		if (currentState != null)
		{
			sample.distance = distanceToTrajectory(currentState, state);
			sample.velocity = currentState.getVel2().getLength2();
			sample.maxDistance = Math.max(minVel, currentState.getVel2().getLength2()) * maxDt * 1000;
			if (sample.isOnTrajectory())
			{
				return Optional.of(currentState);
			}
		}
		return Optional.empty();
	}


	public TrajTrackingQuality getTrackingQuality()
	{
		return new TrajTrackingQuality(getDistance(), getMaxDistance(), getRelOnTrajectoryTime());
	}


	private double getDistance()
	{
		if (distanceSamples.isEmpty())
		{
			return 0;
		}
		return distanceSamples.get(distanceSamples.size() - 1).distance;
	}


	private double getMaxDistance()
	{
		return distanceSamples.stream().mapToDouble(s -> s.distance).max().orElse(0.0);
	}


	private double getRelOnTrajectoryTime()
	{
		long onTrajectory = distanceSamples.stream().filter(DistanceSample::isOnTrajectory).count();
		return (double) onTrajectory / distanceSamples.size();
	}


	private void addSample(DistanceSample sample)
	{
		distanceSamples.removeIf(s -> s.timestamp < sample.timestamp - statisticsHorizon * 1e9);
		distanceSamples.add(sample);
	}


	public Optional<State> getState(final long timestamp, final double feedbackDelay)
	{
		long pastTimestamp = timestamp - (long) (feedbackDelay * 1e9);
		return buffer.get(pastTimestamp).map(p -> p.interpolate(pastTimestamp));
	}


	private double distanceToTrajectory(final State bufferedState, final BotState currentState)
	{
		return bufferedState.getPos().distanceTo(currentState.getPos());
	}


	public void reset()
	{
		buffer.reset();
	}


	private State trajectoryToState(final ITrajectory<IVector3> traj)
	{
		return State.of(Pose.from(traj.getPositionMM(0.0)), traj.getVelocity(0.0));
	}

	private static class DistanceSample
	{
		long timestamp;
		double distance;
		double velocity;
		double maxDistance;

		boolean isOnTrajectory()
		{
			return distance <= maxDistance;
		}
	}
}
