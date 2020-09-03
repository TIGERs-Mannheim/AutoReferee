/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.util;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.bot.BotState;
import edu.tigers.sumatra.bot.State;
import edu.tigers.sumatra.filter.DataSync;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.trajectory.ITrajectory;
import edu.tigers.sumatra.wp.data.TrajTrackingQuality;
import lombok.Getter;

import java.util.Optional;


public class BotStateTrajectorySync
{
	@Configurable(defValue = "0.2", comment = "Time horizon [s] that is buffered (requires Moduli-reload)")
	private static double horizon = 0.2;

	@Configurable(defValue = "0.5", comment = "Max time [s] that robot can be off the trajectory before a reset")
	private static double maxTimeOffTrajectory = 0.5;

	@Configurable(defValue = "0.03", comment = "Max time difference [s] between trajectory and measured state")
	private static double maxDt = 0.03;

	@Configurable(defValue = "1.0", comment = "Min velocity [m/s] to consider for calculating maxDiff on trajectory")
	private static double minVel = 1.0;

	@Configurable(defValue = "true", comment = "Prefer the state of the current trajectory that the bot executes")
	private static boolean enabled = true;

	static
	{
		ConfigRegistration.registerClass("wp", BotStateTrajectorySync.class);
	}

	private final DataSync<State> buffer = new DataSync<>(horizon);
	private long lastTimestamp = 0;

	@Getter
	private TrajTrackingQuality trajTrackingQuality = new TrajTrackingQuality();


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


	public void updateState(final long timestamp, final double feedbackDelay, final BotState state)
	{
		State currentState = getState(timestamp, feedbackDelay).orElse(null);
		if (currentState == null || lastTimestamp == 0)
		{
			trajTrackingQuality = new TrajTrackingQuality();
		} else
		{
			double dt = (timestamp - lastTimestamp) / 1e9;
			double curDistance = distanceToTrajectory(currentState, state);
			double maxDistance = Math.max(minVel, currentState.getVel2().getLength2()) * maxDt * 1000;
			double timeOffTrajectory = trajTrackingQuality.getTimeOffTrajectory();
			if (curDistance <= maxDistance)
			{
				timeOffTrajectory -= dt;
				if (timeOffTrajectory < 0)
				{
					timeOffTrajectory = 0;
				}
			} else
			{
				timeOffTrajectory += dt;
				if (timeOffTrajectory > maxTimeOffTrajectory)
				{
					buffer.reset();
				}
			}
			trajTrackingQuality = new TrajTrackingQuality(curDistance, maxDistance, timeOffTrajectory);
		}
		lastTimestamp = timestamp;
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


	private State trajectoryToState(final ITrajectory<IVector3> traj)
	{
		return State.of(Pose.from(traj.getPositionMM(0.0)), traj.getVelocity(0.0));
	}
}
