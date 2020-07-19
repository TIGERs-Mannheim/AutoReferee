/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.trajectory;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import org.apache.commons.lang.Validate;

import java.util.function.Function;


public final class BangBangTrajectoryFactory
{
	private static final float SYNC_ACCURACY = 1e-3f;


	public BangBangTrajectory2DAsync async(
			final IVector2 s0,
			final IVector2 s1,
			final IVector2 v0,
			final double vmax,
			final double acc,
			final IVector2 primaryDirection
	)
	{
		Validate.notNull(primaryDirection);
		Validate.isTrue(!primaryDirection.isZeroVector(), "zero primary direction vector");

		final var rotation = primaryDirection.getAngle();
		final var startToTarget = s1.subtractNew(s0).turn(-rotation);
		final var v0Rotated = v0.turnNew(-rotation);

		final Function<Float, Float> alphaFn = alpha -> alpha + (((float) AngleMath.PI_HALF - alpha) * 0.5f);
		BangBangTrajectory2D child = new BangBangTrajectory2D().generate(
				Vector2f.ZERO_VECTOR,
				startToTarget,
				v0Rotated,
				(float) vmax,
				(float) acc,
				SYNC_ACCURACY,
				alphaFn);
		return new BangBangTrajectory2DAsync(child, s0, rotation);
	}


	public BangBangTrajectory2D sync(
			final IVector2 s0,
			final IVector2 s1,
			final IVector2 v0,
			final double vmax,
			final double acc
	)
	{
		return new BangBangTrajectory2D()
				.generate(s0, s1, v0, (float) vmax, (float) acc, SYNC_ACCURACY, Function.identity());
	}


	public BangBangTrajectory1D singleDim(
			final double initialPos,
			final double finalPos,
			final double initialVel,
			final double maxVel,
			final double maxAcc
	)
	{
		return new BangBangTrajectory1D()
				.generate((float) initialPos, (float) finalPos, (float) initialVel, (float) maxVel, (float) maxAcc);
	}


	public BangBangTrajectory1DOrient orientation(
			final double initialPos,
			final double finalPos,
			final double initialVel,
			final double maxVel,
			final double maxAcc
	)
	{
		var adaptedFinalPos = initialPos + AngleMath.normalizeAngle(finalPos - AngleMath.normalizeAngle(initialPos));
		return new BangBangTrajectory1DOrient(singleDim(initialPos, adaptedFinalPos, initialVel, maxVel, maxAcc));
	}
}
