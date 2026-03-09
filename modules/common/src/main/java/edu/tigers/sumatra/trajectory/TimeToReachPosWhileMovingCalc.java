/*
 * Copyright (c) 2009 - 2026, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.trajectory;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import net.jafama.DoubleWrapper;
import net.jafama.FastMath;
import org.apache.commons.lang.Validate;

import java.util.function.UnaryOperator;


/**
 * Calculate how long a robot will take to reach a certain position while allowing for a none-zero velocity at the
 * target position
 */
public class TimeToReachPosWhileMovingCalc
{
	public double timeForBangBang2dSync(
			IVector2 s0,
			IVector2 s1,
			IVector2 v0,
			double vMax,
			double aMax,
			double vMaxAtTarget
	)
	{
		return timeForBangBang2D(
				s0,
				s1,
				v0,
				(float) vMax,
				(float) aMax,
				(float) vMaxAtTarget,
				alpha -> alpha
		);
	}


	public double timeForBangBang2dAsync(
			IVector2 s0,
			IVector2 s1,
			IVector2 v0,
			double vMax,
			double aMax,
			double vMaxAtTarget,
			IVector2 primaryDirection
	)
	{
		var rotation = primaryDirection.getAngle();
		var startToTarget = s1.subtractNew(s0).turn(-rotation);
		var v0Rotated = v0.turnNew(-rotation);

		return timeForBangBang2D(
				Vector2f.ZERO_VECTOR,
				startToTarget,
				v0Rotated,
				(float) vMax,
				(float) aMax,
				(float) vMaxAtTarget,
				BangBangTrajectoryFactory.ALPHA_FN_ASYNC
		);
	}


	private double timeForBangBang2D(
			IVector2 s0,
			IVector2 s1,
			IVector2 v0,
			float vMax,
			float aMax,
			float vMaxAtTarget,
			UnaryOperator<Float> alphaFn
	)
	{
		float v0x = (float) v0.x();
		float v0y = (float) v0.y();

		var distance = s1.subtractNew(s0);
		float distanceX = (float) distance.x();
		float distanceY = (float) distance.y();

		float inc = (float) AngleMath.PI / 8.0f;
		float alpha = (float) AngleMath.PI / 4.0f;

		double x = 0;
		double y = 0;

		// binary search, some iterations (fixed)
		while (inc > 1e-7)
		{
			DoubleWrapper cos = new DoubleWrapper();
			final float sA = (float) FastMath.sinAndCos(alphaFn.apply(alpha), cos);
			final float cA = (float) cos.value;

			x = getTime1D(distanceX, v0x, vMax * cA, aMax * cA, vMaxAtTarget * cA);
			y = getTime1D(distanceY, v0y, vMax * sA, aMax * sA, vMaxAtTarget * sA);

			double diff = Math.abs(x - y);
			if (diff < BangBangTrajectoryFactory.SYNC_ACCURACY)
			{
				break;
			}
			if (x > y)
			{
				alpha -= inc;
			} else
			{
				alpha += inc;
			}

			inc *= 0.5f;
		}
		return (x + y) / 2;
	}


	private float getTime1D(float s, float v0, float vMax, float aMax, float vMaxAtTarget)
	{
		if (s < 0)
		{
			// Assure s >= 0
			return getTime1D(-s, -v0, vMax, aMax, vMaxAtTarget);
		}

		if (v0 > vMax)
		{
			// Assure v0 <= vMax
			float tBreak = (v0 - vMax) / aMax;
			float sBreak = 0.5f * (vMax + v0) * tBreak;
			return tBreak + getTime1D(s - sBreak, vMax, vMax, aMax, vMaxAtTarget);
		}

		if (v0 < 0)
		{
			// Assure v0 >= 0
			float tBreak = -v0 / aMax;
			float sBreak = -0.5f * v0 * tBreak;
			return tBreak + getTime1DSimplified(s + sBreak, 0, vMax, aMax, vMaxAtTarget);
		}

		if (v0 <= vMaxAtTarget)
		{
			return getTime1DSimplified(s, v0, vMax, aMax, vMaxAtTarget);
		}


		float tSlowDown = (v0 - vMaxAtTarget) / aMax;
		float sSlowDown = 0.5f * (vMaxAtTarget + v0) * tSlowDown;
		if (sSlowDown > s)
		{
			// Not enough space to slow down to vMaxAtTarget -> overshoot and recover
			float tBreak = v0 / aMax;
			float sBreak = 0.5f * v0 * tBreak;
			return tBreak + getTime1DSimplified(sBreak - s, 0, vMax, aMax, vMaxAtTarget);
		}

		return minimizeTime(v0, s - sSlowDown, vMax, aMax) + tSlowDown;
	}


	private float getTime1DSimplified(
			float s,
			float v0,
			float vMax,
			float aMax,
			float vMaxAtTarget
	)
	{

		var tAcc = vMaxAtTarget > 0 ? (vMaxAtTarget - v0) / aMax : 0;
		var sAcc = 0.5f * (v0 + vMaxAtTarget) * tAcc;

		if (sAcc <= s)
		{
			return tAcc + minimizeTime(vMaxAtTarget, s - sAcc, vMax, aMax);
		}

		return triangleTime(s, v0, aMax);
	}


	private float minimizeTime(
			float vStartEnd,
			float s,
			float vMax,
			float aMax
	)
	{
		Validate.isTrue(vMax >= vStartEnd);

		float sAccAvailable = s / 2;
		float tAcc = (vMax - vStartEnd) / aMax;
		float sAcc = 0.5f * (vMax + vStartEnd) * tAcc;

		if (sAccAvailable >= sAcc)
		{
			// Sufficient space to reach vMax -> trapezoidal form
			return 2 * (tAcc + (sAccAvailable - sAcc) / vMax);
		}

		return 2 * triangleTime(sAccAvailable, vStartEnd, aMax);
	}


	private float triangleTime(float s, float v, float aMax)
	{
		// Solving s = 0.5 * aMax * t^2 + v * t for t:
		float discriminant = (float) SumatraMath.sqrt(v * v + 2 * aMax * s);
		float t = (discriminant - v) / aMax;
		Validate.isTrue(t >= 0);
		return t;
	}
}
