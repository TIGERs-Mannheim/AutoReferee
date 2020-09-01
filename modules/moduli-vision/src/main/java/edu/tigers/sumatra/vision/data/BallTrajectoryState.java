/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.vision.data;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.math.IMirrorable;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.math.vector.Vector3f;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;


/**
 * Data structure for the ball state at a certain time.
 * <br>
 * <b>WARNING: Units of this class are [mm], [mm/s], [mm/s^2] !!!</b>
 */
@Persistent
@Value
@Builder(setterPrefix = "with", toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BallTrajectoryState implements IMirrorable<BallTrajectoryState>
{
	/**
	 * Position in [mm]
	 */
	@NonNull
	IVector3 pos;

	/**
	 * Velocity in [mm/s]
	 */
	@NonNull
	IVector3 vel;

	/**
	 * Acceleration in [mm/s^2]
	 */
	@NonNull
	IVector3 acc;

	/**
	 * The velocity where the ball turns to roll
	 */
	double vSwitchToRoll;

	/**
	 * Was the ball chipped?
	 */
	boolean chipped;

	/**
	 * The spin of the ball
	 */
	double spin;


	/**
	 * Create an empty default state. Required for {@link Persistent}.
	 */
	public BallTrajectoryState()
	{
		pos = Vector3f.ZERO_VECTOR;
		vel = Vector3f.ZERO_VECTOR;
		acc = Vector3f.ZERO_VECTOR;
		vSwitchToRoll = 0;
		chipped = false;
		spin = 0;
	}


	@Override
	public BallTrajectoryState mirrored()
	{
		return toBuilder()
				.withPos(Vector3.from2d(pos.getXYVector().multiplyNew(-1), pos.getXYZVector().z()))
				.withVel(Vector3.from2d(vel.getXYVector().multiplyNew(-1), vel.getXYZVector().z()))
				.withAcc(Vector3.from2d(acc.getXYVector().multiplyNew(-1), acc.getXYZVector().z()))
				.build();
	}


	/**
	 * Get ball trajectory.
	 *
	 * @param timestampNow
	 * @return
	 */
	public ABallTrajectory getTrajectory(final long timestampNow)
	{
		if (isChipped())
		{
			return new ChipBallTrajectory(timestampNow, pos, vel, spin);
		}

		long switchTimestamp = timestampNow;
		if (getAcc().getLength2() > 1e-6)
		{
			switchTimestamp =
					timestampNow + (long) (((vel.getLength2() - vSwitchToRoll) / getAcc().getLength2()) * 1e9);
		}
		return new StraightBallTrajectory(timestampNow, getPos(), getVel(), switchTimestamp);
	}
}
