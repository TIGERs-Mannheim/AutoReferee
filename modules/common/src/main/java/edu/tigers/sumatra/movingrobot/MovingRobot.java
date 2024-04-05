/*
 * Copyright (c) 2009 - 2024, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.movingrobot;

import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.tube.ITube;
import edu.tigers.sumatra.math.tube.Tube;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.trajectory.BangBangTrajectoryFactory;
import edu.tigers.sumatra.trajectory.ITrajectory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


/**
 * A moving robot with a moving horizon
 */
@Getter
@RequiredArgsConstructor
public class MovingRobot
{
	private static final double LARGE_DISTANCE = 20;
	private static final BangBangTrajectoryFactory TRAJECTORY_FACTORY = new BangBangTrajectoryFactory();

	private final IVector2 pos;
	private final IVector2 dir;
	private final double maxHorizon;
	private final double radius;
	private final double speed;
	private final double reactionTime;

	private final ITrajectory<Double> trajectoryForward;
	private final ITrajectory<Double> trajectoryBackward;


	public static MovingRobot create(
			IVector2 pos,
			IVector2 vel,
			double vMax,
			double acc,
			double maxHorizon,
			double radius,
			double reactionTime
	)
	{
		double speed = vel.getLength2();
		return new MovingRobot(
				pos,
				vel.normalizeNew(),
				maxHorizon,
				radius,
				speed,
				reactionTime,
				TRAJECTORY_FACTORY.single(0, LARGE_DISTANCE, speed, vMax, acc),
				TRAJECTORY_FACTORY.single(0, -LARGE_DISTANCE, speed, vMax, acc)
		);
	}


	/**
	 * Get the horizon for possible movement of the robot for a given time horizon
	 *
	 * @param tHorizon the time horizon
	 * @return a circle specifying the horizon
	 */
	public ICircle getMovingHorizon(final double tHorizon)
	{
		return getMovingHorizon(tHorizon, 0);
	}


	/**
	 * Get the horizon for possible movement of the robot for a given time horizon
	 *
	 * @param tHorizon            the time horizon
	 * @param tAdditionalReaction additional reaction time
	 * @return a circle specifying the horizon
	 */
	public ICircle getMovingHorizon(final double tHorizon, double tAdditionalReaction)
	{
		IVector2 p = forwardBackwardOffset(tHorizon, tAdditionalReaction);
		double pForward = p.x();
		double pBackward = p.y();

		double dynamicRadius = Math.abs(pForward - pBackward) / 2;
		IVector2 center = pos.addNew(dir.multiplyNew(pBackward + dynamicRadius));
		return Circle.createCircle(center, dynamicRadius + radius);
	}


	/**
	 * Get a tube with fixed width and length depending on the moving horizon.
	 *
	 * @param tHorizon            the time horizon
	 * @param tAdditionalReaction additional reaction time
	 * @return a tube specifying the movement horizon
	 */
	public ITube getMovingHorizonTube(final double tHorizon, double tAdditionalReaction)
	{
		IVector2 p = forwardBackwardOffset(tHorizon, tAdditionalReaction);
		double pForward = p.x();
		double pBackward = p.y();

		return Tube.create(
				dir.multiplyNew(pBackward).add(pos),
				dir.multiplyNew(pForward).add(pos),
				radius
		);
	}


	/**
	 * Get a tube with fixed width and length depending on the moving horizon.
	 *
	 * @param tHorizon the time horizon
	 * @return a tube specifying the movement horizon
	 */
	public ITube getMovingHorizonTube(final double tHorizon)
	{
		return getMovingHorizonTube(tHorizon, 0);
	}


	private IVector2 forwardBackwardOffset(double tHorizon, double tAdditionalReaction)
	{
		double tLimitedHorizon = Math.max(0, Math.min(maxHorizon, tHorizon));
		double tReaction = Math.min(reactionTime + tAdditionalReaction, tLimitedHorizon);

		double pReaction = speed * tReaction;
		return Vector2.fromXY(
				pReaction + trajectoryForward.getPositionMM(tLimitedHorizon - tReaction),
				pReaction + trajectoryBackward.getPositionMM(tLimitedHorizon - tReaction)
		);
	}


	public double getMaxSpeed()
	{
		return Math.max(trajectoryForward.getMaxSpeed(), trajectoryBackward.getMaxSpeed());
	}
}
