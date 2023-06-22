/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.ellipse;

import edu.tigers.sumatra.math.intersections.PathIntersectionMath;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.math.vector.VectorMath;

import java.util.List;


/**
 * Ellipse related calculations.
 * Please consider using the methods from {@link IEllipse} instead of these static methods!
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public final class EllipseMath
{
	private static final double MAX_STEP_CURVE = 5;
	private static final double POINT_ON_CURVE_TOL = 5;
	private static final double STEP_TOLERANCE = 0.01;


	@SuppressWarnings("unused")
	private EllipseMath()
	{
	}


	/**
	 * @param ellipse
	 * @param start
	 * @param step
	 * @return point after step
	 */
	public static IVector2 stepOnCurve(final IEllipse ellipse, final IVector2 start, final double step)
	{
		if (VectorMath.distancePP(start, ellipse.nearestPointOutside(start)) > POINT_ON_CURVE_TOL)
		{
			throw new IllegalArgumentException(
					"The start point is not on the ellipse border. Use nearestPointOutsideCircle to get an appropriate point");
		}

		IVector2 curPt = transformToNotTurned(ellipse, start);
		double nextStep = step;
		final double maxStep = MAX_STEP_CURVE;
		do
		{
			double curStep;
			if (nextStep > 0)
			{
				curStep = Math.min(nextStep, maxStep);
			} else
			{
				curStep = Math.max(nextStep, -maxStep);
			}

			final IVector2 relStart = curPt.subtractNew(ellipse.center());
			// tangent formula, see Wikipedia, not sure if it is correct for a turned ellipse
			final IVector2 dir = Vector2f.fromXY((-ellipse.getRadiusX() * relStart.y()) / ellipse.getRadiusY(),
					(ellipse.getRadiusY() * relStart.x())
							/ ellipse.getRadiusX());
			final IVector2 tmpP1 = curPt.addNew(dir.scaleToNew(curStep)).add(relStart);
			// ensure that we cross the ellipse border
			final IVector2 tmpP2 = LineMath.stepAlongLine(tmpP1, ellipse.center(), -ellipse.getDiameterMax());

			final List<IVector2> intsPts = PathIntersectionMath.intersectLineSegmentAndEllipse(
					Lines.segmentFromPoints(ellipse.center(), transformToTurned(ellipse, tmpP2)), ellipse).asList();
			if (intsPts.size() != 1)
			{
				throw new IllegalStateException("Only one intersection point expected, but " + intsPts.size() + " found");
			}
			final IVector2 newP = transformToNotTurned(ellipse, intsPts.get(0));

			// actual step
			double actStep = VectorMath.distancePP(curPt, newP);
			nextStep = nextStep - ((curStep > 0) ? actStep : -actStep);
			// if we passed zero
			if (isNegPos(nextStep, curStep) || (actStep < 0.001))
			{
				// exit
				nextStep = 0;
			}

			curPt = newP;

		} while (Math.abs(nextStep) > STEP_TOLERANCE);
		return transformToTurned(ellipse, curPt);
	}


	/**
	 * Checks if a is neg and b is pos or wise versa
	 *
	 * @param a
	 * @param b
	 * @return
	 */
	private static boolean isNegPos(final double a, final double b)
	{
		return ((a > 0) && (b < 0)) || ((a < 0) && (b > 0));
	}


	public static IVector2 nearestPointOnEllipseLine(Ellipse ellipse, IVector2 point)
	{
		if (point.isCloseTo(ellipse.center()))
		{
			if (ellipse.getRadiusX() <= ellipse.getRadiusY())
			{
				ellipse.center().addNew(Vector2.fromX(ellipse.getRadiusX()).turn(ellipse.getTurnAngle()));
			}
			return ellipse.center().addNew(Vector2.fromY(ellipse.getRadiusY()).turn(ellipse.getTurnAngle()));
		}
		var halfLine = Lines.halfLineFromPoints(ellipse.center(), point);
		return ellipse.intersectPerimeterPath(halfLine).stream().findAny()
				.orElseThrow(() -> new IllegalArgumentException(
						"Not exactly one intersection between half line starting from within a circle, this is impossible"));
	}


	/**
	 * Transform a point that is not turned with the turnAngle of the ellipse
	 * to a turned point
	 *
	 * @param point
	 * @return
	 */
	private static IVector2 transformToTurned(final IEllipse ellipse, final IVector2 point)
	{
		return point.subtractNew(ellipse.center()).turn(ellipse.getTurnAngle()).add(ellipse.center());
	}


	/**
	 * Transform a turned point (normal incoming point actually) to a non turned
	 * point (needed by some calculations, that do not consider turnAngle
	 *
	 * @param point
	 * @return
	 */
	private static IVector2 transformToNotTurned(final IEllipse ellipse, final IVector2 point)
	{
		return point.subtractNew(ellipse.center()).turn(-ellipse.getTurnAngle()).add(ellipse.center());
	}
}
