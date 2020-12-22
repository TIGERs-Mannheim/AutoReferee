/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.ball.trajectory;

import edu.tigers.sumatra.math.vector.IVector;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.vision.data.BallTrajectoryState;
import edu.tigers.sumatra.wp.ball.trajectory.chipped.FixedLossPlusRollingBallTrajectory;
import edu.tigers.sumatra.wp.ball.trajectory.chipped.FixedLossPlusRollingConsultant;
import edu.tigers.sumatra.wp.ball.trajectory.chipped.FixedLossPlusRollingParameters;
import edu.tigers.sumatra.wp.ball.trajectory.flat.TwoPhaseDynamicVelBallTrajectory;
import edu.tigers.sumatra.wp.ball.trajectory.flat.TwoPhaseDynamicVelConsultant;
import edu.tigers.sumatra.wp.ball.trajectory.flat.TwoPhaseDynamicVelParameters;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;


/**
 * Factory class for creating classes for the configured ball models.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BallFactory
{
	/**
	 * Create a ball trajectory with the default configured implementation
	 *
	 * @param state the ball state on which the trajectory is based
	 * @return a new ball trajectory
	 */
	public static IBallTrajectory createTrajectory(final BallTrajectoryState state)
	{
		if (state.isChipped())
		{
			return FixedLossPlusRollingBallTrajectory.fromState(
					state.getPos().getXYZVector(),
					state.getVel().getXYZVector(),
					state.getSpin(),
					new FixedLossPlusRollingParameters());
		}
		return TwoPhaseDynamicVelBallTrajectory.fromState(
				state.getPos().getXYVector(),
				state.getVel().getXYVector(),
				state.getVSwitchToRoll(),
				new TwoPhaseDynamicVelParameters());
	}


	/**
	 * Create a ball trajectory based on a kick
	 * <br>
	 * Consider using {@link #createTrajectoryFromChipKick(IVector2, IVector3)} or
	 * {@link #createTrajectoryFromStraightKick(IVector2, IVector)}
	 *
	 * @param kickPos [mm]
	 * @param kickVel [mm/s]
	 * @param chip
	 * @return
	 */
	public static IBallTrajectory createTrajectoryFromKick(final IVector2 kickPos, final IVector kickVel,
			final boolean chip)
	{
		if (chip)
		{
			return createTrajectoryFromChipKick(kickPos, kickVel.getXYZVector());
		}
		return createTrajectoryFromStraightKick(kickPos, kickVel);
	}


	/**
	 * Create a ball trajectory based on a chip kick
	 *
	 * @param kickPos [mm]
	 * @param kickVel [mm/s]
	 * @return
	 */
	public static IBallTrajectory createTrajectoryFromStraightKick(final IVector2 kickPos, final IVector kickVel)
	{
		return TwoPhaseDynamicVelBallTrajectory.fromKick(kickPos, kickVel.getXYVector(),
				new TwoPhaseDynamicVelParameters());
	}


	/**
	 * Create a ball trajectory based on a straight kick
	 *
	 * @param kickPos [mm]
	 * @param kickVel [mm/s]
	 * @return
	 */
	public static IBallTrajectory createTrajectoryFromChipKick(final IVector2 kickPos, final IVector3 kickVel)
	{
		return FixedLossPlusRollingBallTrajectory.fromKick(kickPos, kickVel,
				0, new FixedLossPlusRollingParameters());
	}


	/**
	 * Create a consultant for straight kicks with the default configured implementation
	 *
	 * @return a new ball consultant for straight kicks
	 */
	public static IStraightBallConsultant createStraightConsultant()
	{
		return new TwoPhaseDynamicVelConsultant(new TwoPhaseDynamicVelParameters());
	}


	/**
	 * Create a consultant for chip kicks with the default configured implementation
	 *
	 * @return a new ball consultant for chip kicks
	 */
	public static IChipBallConsultant createChipConsultant()
	{
		return new FixedLossPlusRollingConsultant(new FixedLossPlusRollingParameters());
	}
}
