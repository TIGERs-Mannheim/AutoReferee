/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.intersections;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.IArc;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.circle.ICircular;
import edu.tigers.sumatra.math.ellipse.IEllipse;
import edu.tigers.sumatra.math.line.IHalfLine;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.ILineBase;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.math.vector.VectorDistinctStreamFilter;
import org.apache.commons.lang.NotImplementedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.Math.abs;


public class PathIntersectionMath
{
	private static final double ACCURACY = SumatraMath.getEqualTol();


	private PathIntersectionMath()
	{
	}


	public static ISingleIntersection intersectLineAndLine(ILine line1, ILine line2)
	{
		if (!line1.isValid() || !line2.isValid())
		{
			return SingleIntersection.empty();
		}
		if (line1.isParallelTo(line2))
		{
			return getSingleIntersectionPointsOfParallelLines(line1, line2);
		}
		final double lambda = getLineIntersectionLambda(line1, line2);
		return SingleIntersection.of(LineMath.getPointOnLineForLambda(line1, lambda));
	}


	public static ISingleIntersection intersectLineAndHalfLine(ILine line, IHalfLine halfLine)
	{
		if (!line.isValid() || !halfLine.isValid())
		{
			return SingleIntersection.empty();
		}
		if (line.isParallelTo(halfLine))
		{
			return getSingleIntersectionPointsOfParallelLines(line, halfLine);
		}
		double lambda = getLineIntersectionLambda(halfLine, line);

		if (isLineLambdaInRange(lambda, 0, Double.MAX_VALUE))
		{
			return SingleIntersection.of(LineMath.getPointOnLineForLambda(halfLine, lambda));
		}
		return SingleIntersection.empty();
	}


	public static ISingleIntersection intersectLineAndLineSegment(ILine line, ILineSegment segment)
	{
		if (!line.isValid())
		{
			return SingleIntersection.empty();
		}
		if (!segment.isValid())
		{
			return line.isPointOnPath(segment.supportVector()) ?
					SingleIntersection.of(segment.supportVector()) :
					SingleIntersection.empty();
		}
		if (line.isParallelTo(segment))
		{
			return getSingleIntersectionPointsOfParallelLines(line, segment);
		}

		double lambda = getLineIntersectionLambda(segment, line);
		if (isLineLambdaInRange(lambda, 0, 1))
		{
			return SingleIntersection.of(LineMath.getPointOnLineForLambda(segment, lambda));
		}
		return SingleIntersection.empty();
	}


	public static IIntersections intersectLineAndCircle(ILine line, ICircle circle)
	{
		if (!line.isValid() || !circle.isValid())
		{
			return Intersections.of();
		}
		return Intersections.of(circularLineIntersections(circle, line, true, true, 0));
	}


	public static IIntersections intersectLineAndArc(ILine line, IArc arc)
	{
		if (!line.isValid() || !arc.isValid())
		{
			return Intersections.of();
		}
		return Intersections.of(circularLineIntersections(arc, line, false, true, 0));
	}


	public static IIntersections intersectLineAndEllipse(ILine line, IEllipse ellipse)
	{
		if (!line.isValid() || !ellipse.isValid())
		{
			return Intersections.of();
		}
		return Intersections.of(ellipseLineIntersectionsInternal(ellipse, line, true, true));
	}


	public static ISingleIntersection intersectHalfLineAndHalfLine(IHalfLine halfLine1, IHalfLine halfLine2)
	{
		if (!halfLine1.isValid() || !halfLine2.isValid())
		{
			return SingleIntersection.empty();
		}
		if (halfLine1.isParallelTo(halfLine2))
		{
			return getSingleIntersectionPointsOfParallelLines(halfLine1, halfLine2);
		}

		double lambdaA = getLineIntersectionLambda(halfLine1, halfLine2);
		double lambdaB = getLineIntersectionLambda(halfLine2, halfLine1);

		if (isLineLambdaInRange(lambdaA, 0, Double.MAX_VALUE) && isLineLambdaInRange(lambdaB, 0, Double.MAX_VALUE))
		{
			return SingleIntersection.of(LineMath.getPointOnLineForLambda(halfLine1, lambdaA));
		}
		return SingleIntersection.empty();
	}


	public static ISingleIntersection intersectHalfLineAndLineSegment(IHalfLine halfLine, ILineSegment segment)
	{
		if (!halfLine.isValid())
		{
			return SingleIntersection.empty();
		}
		if (!segment.isValid())
		{
			return halfLine.isPointOnPath(segment.supportVector()) ?
					SingleIntersection.of(segment.supportVector()) :
					SingleIntersection.empty();
		}
		if (halfLine.isParallelTo(segment))
		{
			return getSingleIntersectionPointsOfParallelLines(halfLine, segment);
		}

		double halfLineLambda = getLineIntersectionLambda(halfLine, segment);
		double segmentLambda = getLineIntersectionLambda(segment, halfLine);

		if (isLineLambdaInRange(halfLineLambda, 0, Double.MAX_VALUE) && isLineLambdaInRange(segmentLambda, 0, 1))
		{
			return SingleIntersection.of(LineMath.getPointOnLineForLambda(halfLine, halfLineLambda));
		}
		return SingleIntersection.empty();
	}


	public static IIntersections intersectHalfLineAndCircle(IHalfLine halfLine, ICircle circle)
	{
		if (!halfLine.isValid() || !circle.isValid())
		{
			return Intersections.of();
		}
		return Intersections.of(circularLineIntersections(circle, halfLine, true, false, Double.MAX_VALUE));
	}


	public static IIntersections intersectHalfLineAndArc(IHalfLine halfLine, IArc arc)
	{
		if (!halfLine.isValid() || !arc.isValid())
		{
			return Intersections.of();
		}
		return Intersections.of(circularLineIntersections(arc, halfLine, false, false, Double.MAX_VALUE));
	}


	public static IIntersections intersectHalfLineAndEllipse(IHalfLine halfLine, IEllipse ellipse)
	{
		if (!halfLine.isValid() || !ellipse.isValid())
		{
			return Intersections.of();
		}
		return Intersections.of(ellipseLineIntersectionsInternal(ellipse, halfLine, true, false));
	}


	public static ISingleIntersection intersectLineSegmentAndLineSegment(ILineSegment segment1, ILineSegment segment2)
	{
		if (!segment1.isValid() && !segment2.isValid())
		{
			return segment1.supportVector().isCloseTo(segment2.supportVector()) ?
					SingleIntersection.of(segment1.supportVector()) :
					SingleIntersection.empty();
		}
		if (!segment1.isValid())
		{
			return segment2.isPointOnPath(segment1.supportVector()) ?
					SingleIntersection.of(segment1.supportVector()) :
					SingleIntersection.empty();
		}
		if (!segment2.isValid())
		{
			return segment1.isPointOnPath(segment2.supportVector()) ?
					SingleIntersection.of(segment2.supportVector()) :
					SingleIntersection.empty();
		}
		if (segment1.isParallelTo(segment2))
		{
			return getSingleIntersectionPointsOfParallelLines(segment1, segment2);
		}

		final double lambda = getLineIntersectionLambda(segment1, segment2);
		final double delta = getLineIntersectionLambda(segment2, segment1);

		if (isLineLambdaInRange(lambda, 0, 1) && isLineLambdaInRange(delta, 0, 1))
		{
			return SingleIntersection.of(LineMath.getPointOnLineForLambda(segment1, lambda));
		}
		return SingleIntersection.empty();
	}


	public static IIntersections intersectLineSegmentAndCircle(ILineSegment segment, ICircle circle)
	{
		if (!circle.isValid())
		{
			return Intersections.of();
		}
		if (!segment.isValid())
		{
			return circle.isPointOnPath(segment.supportVector()) ?
					Intersections.of(segment.supportVector()) :
					Intersections.of();
		}
		return Intersections.of(circularLineIntersections(circle, segment, true, false, 1));
	}


	public static IIntersections intersectLineSegmentAndArc(ILineSegment segment, IArc arc)
	{
		if (!arc.isValid())
		{
			return Intersections.of();
		}
		if (!segment.isValid())
		{
			return arc.isPointOnPath(segment.supportVector()) ?
					Intersections.of(segment.supportVector()) :
					Intersections.of();
		}
		return Intersections.of(circularLineIntersections(arc, segment, false, false, 1));
	}


	public static IIntersections intersectLineSegmentAndEllipse(ILineSegment segment, IEllipse ellipse)
	{
		if (!ellipse.isValid())
		{
			return Intersections.of();
		}
		if (!segment.isValid())
		{
			return ellipse.isPointOnPath(segment.supportVector()) ?
					Intersections.of(segment.supportVector()) :
					Intersections.of();
		}
		return Intersections.of(ellipseLineIntersectionsInternal(ellipse, segment, false, false));
	}


	public static IIntersections intersectCircleAndCircle(ICircle circle1, ICircle circle2)
	{
		if (!circle1.isValid() || !circle2.isValid())
		{
			return Intersections.of();
		}
		return Intersections.of(circleCircleIntersections(circle1, circle2));
	}


	public static IIntersections intersectCircleAndArc(ICircle circle, IArc arc)
	{
		if (!circle.isValid() || !arc.isValid())
		{
			return Intersections.of();
		}
		return Intersections.of(circularCircularIntersections(circle, arc, true, false));
	}


	public static IIntersections intersectCircleAndEllipse(ICircle circle, IEllipse ellipse)
	{
		if (!circle.isValid() || !ellipse.isValid())
		{
			return Intersections.of();
		}
		if (SumatraMath.isEqual(ellipse.getRadiusX(), ellipse.getRadiusY()))
		{
			return Intersections.of(
					circleCircleIntersections(Circle.createCircle(ellipse.center(), ellipse.getRadiusX()), circle));
		}
		return Intersections.of(circularEllipseIntersection(circle, true));
	}


	public static IIntersections intersectArcAndArc(IArc arc1, IArc arc2)
	{
		if (!arc1.isValid() || !arc2.isValid())
		{
			return Intersections.of();
		}
		return Intersections.of(circularCircularIntersections(arc1, arc2, false, false));
	}


	public static IIntersections intersectArcAndEllipse(IArc arc, IEllipse ellipse)
	{
		if (!arc.isValid() || !ellipse.isValid())
		{
			return Intersections.of();
		}
		if (SumatraMath.isEqual(ellipse.getRadiusX(), ellipse.getRadiusY()))
		{
			var circle = Circle.createCircle(ellipse.center(), ellipse.getRadiusX());
			return Intersections.of(circularCircularIntersections(circle, arc, true, false));
		}
		return Intersections.of(circularEllipseIntersection(arc, false));
	}


	public static IIntersections intersectEllipseAndEllipse(IEllipse ellipse1, IEllipse ellipse2)
	{
		if (!ellipse1.isValid() || !ellipse2.isValid())
		{
			return Intersections.of();
		}
		return Intersections.of(ellipseEllipseIntersections());
	}


	private static ISingleIntersection getSingleIntersectionPointsOfParallelLines(ILineBase line1, ILineBase line2)
	{
		var intersections = getIntersectionPointsOfParallelLines(line1, line2);
		if (intersections.isEmpty())
		{
			return SingleIntersection.empty();
		}
		Vector2 average = Vector2.zero();
		for (var intersect : intersections)
		{
			average.add(intersect);
		}
		return SingleIntersection.of(average.multiply(1.0 / intersections.size()));
	}


	private static List<IVector2> getIntersectionPointsOfParallelLines(ILineBase line1, ILineBase line2)
	{
		var supp1 = line1.supportVector();
		var supp2 = line2.supportVector();
		var dir1 = line1.directionVector();
		var dir2 = line2.directionVector();
		var end1 = supp1.addNew(dir1);
		var end2 = supp2.addNew(dir2);

		if (line1.equals(line2))
		{
			return Stream.of(supp1, supp2, end1, end2).filter(VectorDistinctStreamFilter.byIsCloseTo()).toList();
		}

		var results = new ArrayList<IVector2>();
		if (line2.isPointOnPath(supp1))
		{
			results.add(supp1);
		}
		if (line2.isPointOnPath(end1))
		{
			results.add(end1);
		}
		if (line1.isPointOnPath(supp2))
		{
			results.add(supp2);
		}
		if (line1.isPointOnPath(end2))
		{
			results.add(end2);
		}
		return results.stream().filter(VectorDistinctStreamFilter.byIsCloseTo()).toList();
	}


	private static double getLineIntersectionLambda(final ILineBase lineA, final ILineBase lineB)
	{
		return getLineIntersectionLambda(lineA.supportVector(), lineA.directionVector(),
				lineB.supportVector(), lineB.directionVector());
	}


	/**
	 * calculates the intersection-coefficient of the first line given as supportVector1 and directionVector1 and the
	 * second line build from supportVector2 and directionVector2.
	 * <pre>
	 * :: Let the following variables be defined as:
	 * s1 = supportVector1.x
	 * s2 = supportVector1.y
	 * d1 = directionVector1.x
	 * d2 = directionVector1.y
	 * x1 = supportVector2.x
	 * x2 = supportVector2.y
	 * r1 = directionVector2.x
	 * r2 = directionVector2.y
	 * ::
	 * Basic equations: s1 + lambda*d1 = x1 + gamma*r1
	 *                  s2 + lambda*d2 = x2 + gamma*r2
	 * ==============================================
	 * s1 + lambda*d1 = x1 + gamma*r1
	 * s1 - x1 + lambda*d1 = gamma*r1
	 * s1 - x1 + lambda*d1
	 * ------------------- = gamma
	 *          r1
	 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Insert into 2nd dim:
	 *                            s1 - x1 + lambda*d1
	 * s2 + lambda * d2 = x2 + (----------------------)*r2
	 *                                     r1
	 * (s2*r1) + (lambda*d2*r1) = (x2*r1) + (s1*r2) - (x1*r2) + (lambda*d1*r2)
	 * with a sharp eye one can notice some determinants of 2d-matrices...
	 *  ((r1*s2)-(r2*s1)) - ((r1*x2)-(r2*x1)) = lambda *((d1*r2)-(d2*r1))
	 *  ^^^^^^^^^^^^^^^^^    ^^^^^^^^^^^^^^^^           ^^^^^^^^^^^^^^^^^
	 *       detRS               detRX                        detDR
	 *  ==> if detDR==0 -> parallel
	 *                detRS - detRX
	 *  ==> lambda = ---------------
	 *                    detDR
	 * </pre>
	 *
	 * @param supportVector1   first support vector
	 * @param directionVector1 first direction vector
	 * @param supportVector2   second support vector
	 * @param directionVector2 second direction vector
	 * @return the lambda for the first line
	 */
	@SuppressWarnings("squid:S1244") // floating point comparison is ok here, cause it only protects against div by zero
	public static double getLineIntersectionLambda(
			final IVector2 supportVector1,
			final IVector2 directionVector1,
			final IVector2 supportVector2,
			final IVector2 directionVector2)
	{
		final double s1 = supportVector1.x();
		final double s2 = supportVector1.y();
		final double d1 = directionVector1.x();
		final double d2 = directionVector1.y();

		final double x1 = supportVector2.x();
		final double x2 = supportVector2.y();
		final double r1 = directionVector2.x();
		final double r2 = directionVector2.y();


		final double detRS = (r1 * s2) - (r2 * s1);
		final double detRX = (r1 * x2) - (r2 * x1);
		final double detDR = (d1 * r2) - (d2 * r1);

		if (Math.abs(detDR) == 0.0)
		{
			throw new IllegalStateException(
					"the two lines are parallel! Should not happen but when it does tell KaiE as this means there might be a bug");
		}
		return (detRS - detRX) / detDR;
	}


	/**
	 * checks if the given lambda is within the interval [min,max] with the predefined epsilon.
	 *
	 * @param lambda tha lambda value
	 * @param min    interval min
	 * @param max    interval max
	 * @return true, if lambda is in range
	 */
	public static boolean isLineLambdaInRange(final double lambda, final double min, final double max)
	{
		return ((min - (ACCURACY * ACCURACY)) < lambda) && (lambda < (max + (ACCURACY * ACCURACY)));
	}


	public static boolean isCirclePointOnCircular(final ICircular circular, IVector2 point)
	{
		if (circular instanceof IArc arc)
		{
			double angle = point.subtractNew(arc.center()).getAngle();
			double arcRotationHalf = arc.getRotation() / 2.0;
			double arcCenterAngle = AngleMath.normalizeAngle(arc.getStartAngle() + arcRotationHalf);
			return AngleMath.diffAbs(angle, arcCenterAngle) <= Math.abs(arcRotationHalf) + ACCURACY;
		}
		return true;
	}


	/**
	 * /**
	 * Get the intersection points of the circular shape and some kind of line
	 *
	 * @param circular     a circular
	 * @param line         some line
	 * @param ignoreAngle  if angle of e.g. an arc circular shall be considered
	 * @param ignoreLambda if lambda of e.g. a HalfLine shall be considered
	 * @param maxLambda    max positive lambda if not ignored, min lambda is always 0 or ignored
	 * @return all intersection points
	 * @see <a href="https://stackoverflow.com/questions/1073336/circle-line-segment-collision-detection-algorithm">Mathmatical Theory</a>
	 * <p>
	 */
	private static List<IVector2> circularLineIntersections(ICircular circular, ILineBase line, boolean ignoreAngle,
			boolean ignoreLambda, double maxLambda)
	{
		if (line.directionVector().isZeroVector())
		{
			if (abs(line.supportVector().distanceTo(circular.center()) - circular.radius()) < 1e-4)
			{
				return List.of(line.supportVector());
			}
			return List.of();
		}

		double dx = line.directionVector().x();
		double dy = line.directionVector().y();
		double fx = line.supportVector().x() - circular.center().x();
		double fy = line.supportVector().y() - circular.center().y();

		// Solve quadratic equation:
		// a * x^2 + b * x + c = 0
		// With the coefficients (<?, ?> is the dot product)
		// a = <d, d>
		// b = 2 * <f, d>
		// c = <f, f> - r^2

		double a = (dx * dx) + (dy * dy);
		double b = 2 * ((fx * dx) + (fy * dy));
		double c = (fx * fx) + (fy * fy) - (circular.radius() * circular.radius());

		return SumatraMath.quadraticFunctionRoots(a, b, c).stream()
				.map(lambda -> circularIntersectionPointFromLineLambda(line, circular, lambda, ignoreAngle, ignoreLambda,
						maxLambda))
				.flatMap(Optional::stream)
				.toList();

	}


	private static Optional<IVector2> circularIntersectionPointFromLineLambda(ILineBase line, ICircular circular,
			double lambda,
			boolean ignoreAngle,
			boolean ignoreLambda, double maxLambda)
	{
		if (ignoreLambda || isLineLambdaInRange(lambda, 0, maxLambda))
		{
			var point = LineMath.getPointOnLineForLambda(line, lambda);
			if (ignoreAngle || isCirclePointOnCircular(circular, point))
			{
				return Optional.of(point);
			}
		}
		return Optional.empty();
	}


	/**
	 * Get the intersection points of two circular shapes
	 *
	 * @param circular1    a circular
	 * @param circular2    another circular
	 * @param ignoreAngle1 if angle of e.g. an arc circular1 shall be considered
	 * @param ignoreAngle2 if angle of e.g. an arc circular2 shall be considered
	 * @return Intersection points or
	 */
	private static List<IVector2> circularCircularIntersections(ICircular circular1, ICircular circular2,
			boolean ignoreAngle1, boolean ignoreAngle2)
	{
		return circleCircleIntersections(circular1, circular2).stream()
				.filter(p -> ignoreAngle1 || isCirclePointOnCircular(circular1, p))
				.filter(p -> ignoreAngle2 || isCirclePointOnCircular(circular2, p))
				.toList();
	}


	/**
	 * Get the intersection points of two circle shapes
	 * <a href="https://stackoverflow.com/questions/3349125/circle-circle-intersection-points">Mathematical Theory</a>
	 *
	 * @param circular1 a circle
	 * @param circular2 another circle
	 * @return Intersection points or
	 */
	private static List<IVector2> circleCircleIntersections(ICircular circular1, ICircular circular2)
	{
		var c1 = circular1.center();
		var c2 = circular2.center();
		double d = circular1.center().distanceTo(circular2.center());
		double r1 = circular1.radius();
		double r2 = circular2.radius();

		if (d > r1 + r2 || d < ACCURACY)
		{
			return List.of();
		}
		if (Math.abs(d - (r1 + r2)) < ACCURACY)
		{
			// Single Intersection point radical with a = r1
			return List.of(c2.subtractNew(c1).multiply(r1 / d).add(c1));
		}

		double dSquare = d * d;
		double r1Square = r1 * r1;
		double r2Square = r2 * r2;

		double a = (r1Square - r2Square + dSquare) / (2 * d);
		double root = r1Square - (a * a);
		if (root < 0)
		{
			return List.of();
		}
		double h = SumatraMath.sqrt(root);

		var radical = c2.subtractNew(c1).multiply(a / d).add(c1);
		double rx = radical.x();
		double ry = radical.y();

		if (SumatraMath.isZero(h))
		{
			return List.of(Vector2.fromXY(rx, ry));
		}

		double dx = c2.x() - c1.x();
		double dy = c2.y() - c1.y();

		return List.of(
				Vector2.fromXY(rx + h * dy / d, ry - h * dx / d),
				Vector2.fromXY(rx - h * dy / d, ry + h * dx / d)
		);
	}


	private static List<IVector2> ellipseLineIntersectionsInternal(IEllipse ellipse, ILineBase line,
			boolean endlessPositive, boolean endlessNegative)
	{

		final List<IVector2> result = new ArrayList<>(2);

		final IVector2 p0 = ellipse.center();
		final IVector2 p1 = transformToNotTurned(ellipse, line.supportVector());
		final IVector2 p2 = transformToNotTurned(ellipse, line.supportVector().addNew(line.directionVector()));

		// using double to avoid inaccurate results. (its fast enough)
		final double rrx = ellipse.getRadiusX() * ellipse.getRadiusX();
		final double rry = ellipse.getRadiusY() * ellipse.getRadiusY();
		final double x21 = p2.x() - p1.x();
		final double y21 = p2.y() - p1.y();
		final double x10 = p1.x() - p0.x();
		final double y10 = p1.y() - p0.y();
		final double a = ((x21 * x21) / rrx) + ((y21 * y21) / rry);
		final double b = ((x21 * x10) / rrx) + ((y21 * y10) / rry);
		final double c = ((x10 * x10) / rrx) + ((y10 * y10) / rry);
		final double d = (b * b) - (a * (c - 1));

		if (d >= 0)
		{
			final double e = SumatraMath.sqrt(d);
			final double u1 = (-b - e) / a;
			final double u2 = (-b + e) / a;
			if (((0 <= u1 || endlessNegative) && (u1 <= 1 || endlessPositive)))
			{
				final IVector2 tmpP = Vector2f.fromXY(p1.x() + (x21 * u1), p1.y() + (y21 * u1));
				result.add(transformToTurned(ellipse, tmpP));
			}
			if (((0 <= u2 || endlessNegative) && (u2 <= 1 || endlessPositive)))
			{
				final IVector2 tmpP = Vector2f.fromXY(p1.x() + (x21 * u2), p1.y() + (y21 * u2));
				result.add(transformToTurned(ellipse, tmpP));
			}
		}

		return result;
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


	private static List<IVector2> circularEllipseIntersection(ICircular circular, boolean ignoreAngle)
	{
		return ellipseEllipseIntersections().stream()
				.filter(p -> ignoreAngle || isCirclePointOnCircular(circular, p))
				.toList();
	}


	/**
	 * <a href="https://gist.github.com/drawable/92792f59b6ff8869d8b1">Approach</a>
	 *
	 * @return
	 */
	private static List<IVector2> ellipseEllipseIntersections()
	{
		throw new NotImplementedException();
	}
}
