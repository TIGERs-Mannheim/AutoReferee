/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 14.09.2011
 * Author(s): stei_ol
 * *********************************************************
 */
package edu.tigers.sumatra.math;

import java.util.List;

import org.apache.log4j.Logger;

import Jama.Matrix;


/**
 * Helper class for Geometry math problems.
 * 
 * @author osteinbrecher
 */
public final class GeoMath
{
	private static final Logger	log		= Logger.getLogger(GeoMath.class.getName());
														
	/** Matrix X index */
	private static final int		X			= 0;
	/** Matrix X index */
	private static final int		Y			= 1;
														
	private static final double	ACCURACY	= 0.001;
														
														
	/**
	 * not instantiable
	 */
	private GeoMath()
	{
	}
	
	
	/**
	 * Returns distance between two points
	 * 
	 * @param a
	 * @param b
	 * @return euclidean distance
	 * @author Oliver Steinbrecher <OST1988@aol.com>, Malte Mauelshagen <deineMutter@dlr.de>
	 */
	public static double distancePP(final IVector2 a, final IVector2 b)
	{
		return a.subtractNew(b).getLength2();
	}
	
	
	/**
	 * Squared distance between too points
	 * 
	 * @param a
	 * @param b
	 * @return The squared distance between two points
	 */
	public static double distancePPSqr(final IVector2 a, final IVector2 b)
	{
		final double abX = a.x() - b.x();
		final double abY = a.y() - b.y();
		return (abX * abX) + (abY * abY);
	}
	
	
	/**
	 * Calculates the distance between a point and a line.
	 * 
	 * @param point
	 * @param line1 , first point on the line
	 * @param line2 , second point on the line
	 * @return the distance between line and point
	 * @author Oliver Steinbrecher <OST1988@aol.com>
	 */
	public static double distancePL(final IVector2 point, final IVector2 line1, final IVector2 line2)
	{
		return distancePP(point, leadPointOnLine(point, line1, line2));
	}
	
	
	/**
	 * Create the lead point on a straight line (Lot f�llen).
	 * 
	 * @param point which should be used to create lead
	 * @param line1 , first point on the line
	 * @param line2 , second point on the line
	 * @return the lead point on the line
	 * @author Oliver Steinbrecher <OST1988@aol.com>
	 */
	public static Vector2 leadPointOnLine(final IVector2 point, final IVector2 line1, final IVector2 line2)
	{
		if (SumatraMath.isEqual(line1.x(), line2.x()))
		{
			// special case 1. line is orthogonal to x-axis
			return new Vector2(line1.x(), point.y());
			
		} else if (SumatraMath.isEqual(line1.y(), line2.y()))
		{
			// special case 2. line is orthogonal to y-axis
			return new Vector2(point.x(), line1.y());
			
		} else
		{
			// create straight line A from line1 to line2
			final double mA = (line2.y() - line1.y()) / (line2.x() - line1.x());
			final double nA = line2.y() - (mA * line2.x());
			
			// calculate straight line B
			final double mB = -1.0 / mA;
			final double nB = point.y() - (mB * point.x());
			
			// cut straight lines A and B
			final double xCut = (nB - nA) / (mA - mB);
			final double yCut = (mA * xCut) + nA;
			
			return new Vector2(xCut, yCut);
		}
	}
	
	
	/**
	 * Calculates the distance between a point and a line.
	 * 
	 * @param point
	 * @param line
	 * @return
	 */
	public static double distancePL(final IVector2 point, final ILine line)
	{
		return distancePP(point, leadPointOnLine(point, line));
	}
	
	
	/**
	 * Create the lead point on a straight line (Lot f�llen).
	 * 
	 * @param point
	 * @param line
	 * @return
	 */
	public static Vector2 leadPointOnLine(final IVector2 point, final ILine line)
	{
		return leadPointOnLine(point, line.supportVector(), line.supportVector().addNew(line.directionVector()));
	}
	
	
	/**
	 * Calculates the angle between x-Axis and a line, given by two points (p1, p2).<br>
	 * Further details {@link GeoMath#angleBetweenVectorAndVector(IVector2, IVector2) here}<br>
	 * 
	 * @param p1
	 * @param p2
	 * @author Malte
	 * @return
	 */
	public static double angleBetweenXAxisAndLine(final IVector2 p1, final IVector2 p2)
	{
		return angleBetweenXAxisAndLine(Line.newLine(p1, p2));
	}
	
	
	/**
	 * Calculates the angle between x-Axis and a line.<br>
	 * Further details here: {@link edu.tigers.sumatra.math.AVector2#getAngle()}
	 * 
	 * @author Malte
	 * @param l
	 * @return
	 */
	public static double angleBetweenXAxisAndLine(final ILine l)
	{
		return l.directionVector().getAngle();
	}
	
	
	/**
	 * Calculates the angle between two vectors (in rad).
	 * 
	 * @param v1
	 * @param v2
	 * @author AndreR
	 * @return angle in rad [0,PI]
	 */
	public static double angleBetweenVectorAndVector(final IVector2 v1, final IVector2 v2)
	{
		// The old version was numerically unstable, this one works better
		return Math.abs(angleBetweenVectorAndVectorWithNegative(v1, v2));
	}
	
	
	/**
	 * Calculates the angle between two vectors with respect to the rotation direction.
	 * 
	 * @see <a href=
	 *      "http://stackoverflow.com/questions/2663570/how-to-calculate-both-positive-and-negative-angle-between-two-lines"
	 *      >how-to-calculate-both-positive-and-negative-angle-between-two-lines</a>
	 * @see <a href= "http://en.wikipedia.org/wiki/Atan2" >Atan2 (wikipedia)</a>
	 * @param v1
	 * @param v2
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 * @return angle in rad [-PI,PI]
	 */
	public static double angleBetweenVectorAndVectorWithNegative(final IVector2 v1, final IVector2 v2)
	{
		// angle between positive x-axis and first vector
		final double angleA = Math.atan2(v1.x(), v1.y());
		// angle between positive x-axis and second vector
		final double angleB = Math.atan2(v2.x(), v2.y());
		// rotation
		double rotation = angleB - angleA;
		// fix overflows
		if (rotation < (-Math.PI - ACCURACY))
		{
			rotation += 2 * Math.PI;
		} else if (rotation > (Math.PI + ACCURACY))
		{
			rotation -= 2 * Math.PI;
		}
		return rotation;
	}
	
	
	/**
	 * A triangle is defined by three points(p1,p2,p3).
	 * This methods calculates the point(p4) where the bisector("Winkelhalbierende") of the angle(alpha) at p1 cuts the
	 * line p2-p3.
	 * 
	 * <pre>
	 *        p4
	 *  p2----x----p3
	 *    \   |   /
	 *     \  |  /
	 *      \^|^/
	 *       \|/<--alpha
	 *       p1
	 * </pre>
	 * 
	 * @param p1
	 * @param p2
	 * @param p3
	 * @return p4
	 * @author Malte
	 */
	public static Vector2 calculateBisector(final IVector2 p1, final IVector2 p2, final IVector2 p3)
	{
		if (p1.equals(p2) || p1.equals(p3))
		{
			return new Vector2(p1);
		}
		if (p2.equals(p3))
		{
			return new Vector2(p2);
		}
		final Vector2 p1p2 = p2.subtractNew(p1);
		final Vector2 p1p3 = p3.subtractNew(p1);
		final Vector2 p3p2 = p2.subtractNew(p3);
		
		p3p2.scaleTo(p3p2.getLength2() / ((p1p2.getLength2() / p1p3.getLength2()) + 1));
		p3p2.add(p3);
		
		return p3p2;
	}
	
	
	/**
	 * returns the dotted line (see image)
	 * 
	 * <pre>
	 * p2				  p3
	 *  \      p4   /
	 *   \----x----/
	 *    \   |   /
	 *     \  |  /
	 *      \^|^/
	 *       \|/<--alpha
	 *       p1
	 * </pre>
	 * 
	 * @param p1 triangle edge
	 * @param p2 triangle edge
	 * @param p3 triangle edge
	 * @param x position in triangle
	 * @return distance
	 * @author DirkK
	 */
	
	public static double triangleDistance(final IVector2 p1, final IVector2 p2, final IVector2 p3, final IVector2 x)
	{
		ILine ball2LeftPost = Line.newLine(p1, p2);
		ILine ball2RightPost = Line.newLine(p1, p3);
		ILine defenseLine = new Line(x, x.subtractNew(p1).turnNew(AngleMath.PI_HALF));
		
		IVector2 defenseLineLeft = x;
		IVector2 defenseLineRight = x;
		try
		{
			defenseLineLeft = GeoMath.intersectionPoint(ball2LeftPost, defenseLine);
			defenseLineRight = GeoMath.intersectionPoint(ball2RightPost, defenseLine);
		} catch (MathException err)
		{
			log.warn("This should not happen!", err);
		}
		
		return GeoMath.distancePP(defenseLineLeft, defenseLineRight);
	}
	
	
	/**
	 * Two line segments (Strecke) are given by two vectors each.
	 * This method calculates the distance between the line segments.
	 * If one or both of the lines are points (both vectors are the same) the distance form the line segment to the point
	 * is calculated
	 * THIS FUNCTION IS NOT CORRECT, IT IS JUST AN APPROXIMATION
	 * 
	 * @param l1p1
	 * @param l1p2
	 * @param l2p1
	 * @param l2p2
	 * @author Dirk
	 * @return
	 * @throws MathException if lines are parallel or equal or one of the vectors is zero
	 */
	public static double distanceBetweenLineSegments(final IVector2 l1p1, final IVector2 l1p2, final IVector2 l2p1,
			final IVector2 l2p2)
					throws MathException
	{
		// line crossing
		IVector2 lc = null;
		// special cases: one or both lines are points
		if (l1p1.equals(l1p2) && l2p1.equals(l2p2))
		{
			return distancePP(l1p1, l2p1);
		} else if (l1p1.equals(l1p2))
		{
			lc = leadPointOnLine(l1p1, new Line(l2p1, l2p2.subtractNew(l2p1)));
		} else if (l2p1.equals(l2p2))
		{
			lc = leadPointOnLine(l2p1, new Line(l1p1, l1p2.subtractNew(l1p1)));
		} else
		{
			// the normal case: both lines are real lines
			lc = GeoMath.intersectionPoint(l1p1, l1p2.subtractNew(l1p1), l2p1,
					l2p2.subtractNew(l2p1));
		}
		
		// limit to line segments
		IVector2 nearestPointToCrossingForLineSegement1 = new Vector2(lc);
		if (ratio(l1p1, lc, l1p2) > 1)
		{
			nearestPointToCrossingForLineSegement1 = new Vector2(l1p2);
		}
		if ((ratio(l1p2, lc, l1p1) > 1)
				&& ((ratio(l1p1, lc, l1p2) < 1) || (ratio(l1p2, lc, l1p1) < ratio(l1p1, lc, l1p2))))
		{
			nearestPointToCrossingForLineSegement1 = new Vector2(l1p1);
		}
		
		IVector2 nearestPointToCrossingForLineSegement2 = new Vector2(lc);
		if (ratio(l2p1, lc, l2p2) > 1)
		{
			nearestPointToCrossingForLineSegement2 = new Vector2(l2p2);
		}
		if ((ratio(l2p2, lc, l2p1) > 1)
				&& ((ratio(l2p1, lc, l2p2) < 1) || (ratio(l2p2, lc, l2p1) < ratio(l2p1, lc, l2p2))))
		{
			nearestPointToCrossingForLineSegement2 = new Vector2(l2p1);
		}
		return nearestPointToCrossingForLineSegement2.subtractNew(nearestPointToCrossingForLineSegement1).getLength2();
	}
	
	
	/**
	 * Two line segments (Strecke) are given by two vectors each.
	 * This method calculates the nearest point to line segment one to on line segment two.
	 * If one or both of the lines are points (both vectors are the same) the distance form the line segment to the point
	 * is calculated
	 * THIS FUNCTION IS NOT CORRECT, IT IS JUST AN APPROXIMATION
	 * 
	 * @param l1p1
	 * @param l1p2
	 * @param p2
	 * @author Dirk, Felix
	 * @return
	 * @throws MathException if lines are parallel or equal or one of the vectors is zero
	 */
	public static IVector2 nearestPointOnLineSegment(final IVector2 l1p1, final IVector2 l1p2, final IVector2 p2)
			throws MathException
	{
		// line crossing
		IVector2 lc = null;
		// special cases: one or both lines are points
		if (l1p1.equals(l1p2))
		{
			return l1p1;
		}
		lc = leadPointOnLine(p2, new Line(l1p1, l1p2.subtractNew(l1p1)));
		// limit to line segments
		IVector2 nearestPointToCrossingForLineSegement1 = new Vector2(lc);
		if (ratio(l1p1, lc, l1p2) > 1)
		{
			nearestPointToCrossingForLineSegement1 = new Vector2(l1p2);
		}
		if ((ratio(l1p2, lc, l1p1) > 1)
				&& ((ratio(l1p1, lc, l1p2) < 1) || (ratio(l1p2, lc, l1p1) < ratio(l1p1, lc, l1p2))))
		{
			nearestPointToCrossingForLineSegement1 = new Vector2(l1p1);
		}
		return nearestPointToCrossingForLineSegement1;
	}
	
	
	/**
	 * returns the factor the distance between root and point 1 is longer than the distance between root and point 2
	 * e.g. root = (0,0), point1 = (100,0), point2 = (200,0) -> ratio = 1/2
	 * 
	 * @param root
	 * @param point1
	 * @param point2
	 * @return
	 */
	public static double ratio(final IVector2 root, final IVector2 point1, final IVector2 point2)
	{
		if (point2.equals(root))
		{
			// ratio is inifinite
			return Double.MAX_VALUE;
		}
		return (point1.subtractNew(root).getLength2() / point2.subtractNew(root).getLength2());
	}
	
	
	/**
	 * Two lines are given by a support vector <b>p</b> ("Stuetzvektor") and a direction vector <b>v</b>
	 * ("Richtungsvektor").
	 * This methods calculate the point where these lines intersect.
	 * If lines are parallel or equal or one of the vectors is zero Exeption is thrown!!
	 * 
	 * @param p1
	 * @param v1
	 * @param p2
	 * @param v2
	 * @author Malte
	 * @return
	 * @throws MathException if lines are parallel or equal or one of the vectors is zero
	 */
	public static Vector2 intersectionPoint(final IVector2 p1, final IVector2 v1, final IVector2 p2, final IVector2 v2)
			throws MathException
	{
		if (v1.equals(AVector2.ZERO_VECTOR))
		{
			throw new MathException("v1 is the zero vector!");
		}
		if (v2.equals(AVector2.ZERO_VECTOR))
		{
			throw new MathException("v2 is the zero vector!");
		}
		assert !Double.isNaN(v1.x());
		assert !Double.isNaN(v1.x());
		assert !Double.isNaN(v2.y());
		assert !Double.isNaN(v2.y());
		// Create a matrix
		final Matrix m = new Matrix(2, 2);
		m.set(0, 0, v1.x());
		m.set(0, 1, -v2.x());
		m.set(1, 0, v1.y());
		m.set(1, 1, -v2.y());
		
		final double[] b = { p2.x() - p1.x(), p2.y() - p1.y() };
		if (m.rank() == 1)
		{
			throw new MathException("Given lines are parallel or equal!");
		}
		
		final Matrix bM = new Matrix(2, 1);
		bM.set(0, 0, b[X]);
		bM.set(1, 0, b[Y]);
		final Matrix solved = m.solve(bM);
		
		final double x = (solved.get(0, 0) * v1.x()) + p1.x();
		final double y = (solved.get(0, 0) * v1.y()) + p1.y();
		
		return new Vector2(x, y);
		
	}
	
	
	/**
	 * TODO FelixB <bayer.fel@gmail.com>, add comment!
	 * 
	 * @param p1p1 first point of the first line
	 * @param p1p2 second point of the first line
	 * @param p2p1 first point of the second line
	 * @param p2p2 second point of the second line
	 * @return intersection of the two paths if exists, null else
	 */
	public static IVector2 intersectionBetweenPaths(final IVector2 p1p1, final IVector2 p1p2, final IVector2 p2p1,
			final IVector2 p2p2)
	{
		IVector2 intersectionPoint = intersectionPointPath(p1p1, p1p2.subtractNew(p1p1), p2p1, p2p2.subtractNew(p2p1));
		return intersectionPoint;
	}
	
	
	/**
	 * Calculates the intersection point of two paths. Returns null if there is no intersection point.
	 * 
	 * @param p1 vector to the first point of the first path
	 * @param v1 vector from the first point of the first path to the second point of the first path
	 * @param p2 vector to the first point of the second path
	 * @param v2 vector from the first point of the second path to the second point of the second path
	 * @return the intersection point of the two paths if possible, else null
	 */
	public static Vector2 intersectionPointPath(final IVector2 p1, final IVector2 v1, final IVector2 p2,
			final IVector2 v2)
	{
		IVector2 intersectionPoint = null;
		try
		{
			intersectionPoint = intersectionPoint(p1, v1, p2, v2);
		} catch (MathException err)
		{
			// There is no intersection point at all
			return null;
		}
		
		if (betweenValues(p1, p1.addNew(v1), intersectionPoint) &&
				betweenValues(p2, p2.addNew(v2), intersectionPoint))
		{
			return (Vector2) intersectionPoint;
		}
		return null;
	}
	
	
	/*
	 * Tests if the vector pm lies in the rectangle spanned by p1 and p2.
	 */
	private static boolean betweenValues(final IVector2 p1, final IVector2 p2, final IVector2 pm)
	{
		return (((p1.x() <= pm.x()) && (pm.x() <= p2.x())) || ((p2.x() <= pm.x()) && (pm.x() <= p1.x())))
				&& (((p1.y() <= pm.y()) && (pm.y() <= p2.y())) || ((p2.y() <= pm.y()) && (pm.y() <= p1.y())));
	}
	
	
	/**
	 * This methods calculate the point where two lines (l1, l2) intersect.
	 * If lines are parallel or equal, Exception is thrown.
	 * 
	 * @param l1
	 * @param l2
	 * @throws MathException if lines are parallel or equal
	 * @author Malte
	 * @return
	 */
	public static Vector2 intersectionPoint(final ILine l1, final ILine l2) throws MathException
	{
		return intersectionPoint(l1.supportVector(), l1.directionVector(), l2.supportVector(), l2.directionVector());
	}
	
	
	/**
	 * Calculates the intersection point of two lines.
	 * Throws MathException if lines are parallel, equal or intersection is off line boundaries.
	 * Will not work with horizontal or vertical lines.
	 * 
	 * @param l1
	 * @param l2
	 * @return Intersection point
	 * @throws MathException
	 * @author JulianT
	 */
	public static Vector2 intersectionPointOnLine(final ILine l1, final ILine l2) throws MathException
	{
		IVector2 intersect = intersectionPoint(l1, l2);
		
		if (isPointOnLine(l1, intersect) && isPointOnLine(l2, intersect))
		{
			return (Vector2) intersect;
		}
		
		throw new MathException("No intersection on line");
		
	}
	
	
	/**
	 * Calculates if a Point is on a Line.
	 * 
	 * @param line
	 * @param point
	 * @return True, if Point on Line
	 * @author SimonS
	 */
	public static boolean isPointOnLine(final ILine line, final IVector2 point)
	{
		IVector2 lp = GeoMath.leadPointOnLine(point, line);
		if (GeoMath.distancePP(point, lp) < 1e-4f)
		{
			return isVectorBetween(point, line.supportVector(), line.supportVector().addNew(line.directionVector()));
		}
		return false;
	}
	
	
	/**
	 * Check if is a Vector between min and max.
	 * Only look at x and y
	 * 
	 * @param point
	 * @param min
	 * @param max
	 * @return True
	 * @author SimonS
	 */
	public static boolean isVectorBetween(final IVector2 point, final IVector2 min, final IVector2 max)
	{
		return (SumatraMath.isBetween(point.x(), min.x(), max.x()) && SumatraMath.isBetween(point.y(), min.y(), max.y()));
	}
	
	
	/**
	 * A line is given by its slope and a point on it.
	 * This method calculates the y-Intercept.
	 * 
	 * @param point
	 * @param slope
	 * @return yIntercept
	 * @author ChristianK
	 */
	public static double yInterceptOfLine(final IVector2 point, final double slope)
	{
		return (point.y() - (slope * point.x()));
	}
	
	
	/**
	 * Indicates if line intercepts/touches circle
	 * 
	 * @param center of circle
	 * @param radius of circle
	 * @param slope of line
	 * @param yIntercept
	 * @return true if line intercepts circle
	 * @author ChristianK
	 */
	public static boolean isLineInterceptingCircle(final IVector2 center, final double radius, final double slope,
			final double yIntercept)
	{
		// based on equation of cirle and line
		// trying to intercept leads to a quadratic-equation
		// p-q-equation is used
		// point of interception doesn't matter --> checks only if value in sqrt is >= 0 (i.e. equation is solvable, i.e.
		// is intercepting
		
		final double p = (((-2 * center.x()) + (2 * slope * yIntercept)) - (2 * center.y() * slope))
				/ (1.0 + (slope * slope));
		final double q = (((((center.x() * center.x()) + (yIntercept * yIntercept)) - (2 * center.y() * yIntercept))
				+ (center
						.y() * center.y()))
				- (radius * radius))
				/ (1.0 + (slope * slope));
				
		if ((((p * p) / 4.0) - q) >= 0)
		{
			// yepp, is intercepting
			return true;
		}
		// nope, not intercepting
		return false;
	}
	
	
	/**
	 * calculates a point on a circle defined by center and current vectors
	 * performs a projection (rotation) of {@link IVector2}
	 * 
	 * @param current point on circle
	 * @param center of circle
	 * @param angle of rotation
	 * @return projected point
	 * @author DanielW
	 */
	public static Vector2 stepAlongCircle(final IVector2 current, final IVector2 center, final double angle)
	{
		/*
		 * x' = (x-u) cos(beta) - (y-v) sin(beta) + u
		 * y' = (x-u) sin(beta) + (y-v) cos(beta) + v
		 */
		final double x = (((current.x() - center.x()) * Math.cos(angle)) - ((current.y() - center.y()) * Math
				.sin(angle))) + center.x();
		final double y = ((current.x() - center.x()) * Math.sin(angle))
				+ ((current.y() - center.y()) * Math.cos(angle)) + center.y();
				
		return new Vector2(x, y);
	}
	
	
	/**
	 * Distance between two points on a circle
	 * 
	 * @param center
	 * @param p1
	 * @param p2
	 * @return
	 */
	public static double distancePPCircle(final IVector2 center, final IVector2 p1, final IVector2 p2)
	{
		IVector2 c2p1 = p1.subtractNew(center);
		IVector2 c2p2 = p2.subtractNew(center);
		double angle = angleBetweenVectorAndVector(c2p1, c2p2);
		double radius = GeoMath.distancePP(p1, center);
		double u = 2 * radius * AngleMath.PI;
		return (angle / AngleMath.PI_TWO) * u;
	}
	
	
	/**
	 * calculates a point on a line between start and end, that is stepSize away from start
	 * calculation is based on Intercept theorem (Strahlensatz)
	 * 
	 * @param start
	 * @param end
	 * @param stepSize
	 * @author ChristianK
	 * @return
	 */
	public static Vector2 stepAlongLine(final IVector2 start, final IVector2 end, final double stepSize)
	{
		final Vector2 result = new Vector2();
		
		final double distance = distancePP(start, end);
		if (distance == 0)
		{
			result.setX(end.x());
			result.setY(end.y());
			return result;
		}
		
		final double coefficient = stepSize / distance;
		
		final double xDistance = end.x() - start.x();
		final double yDistance = end.y() - start.y();
		
		
		result.setX((xDistance * coefficient) + start.x());
		result.setY((yDistance * coefficient) + start.y());
		if (Double.isNaN(result.x()) || Double.isNaN(result.y()))
		{
			log.fatal("stepAlongLine: result contains NaNs. Very dangerous!!");
			final String seperator = " / ";
			log.fatal(start.toString() + seperator + end.toString() + seperator + distance + seperator + coefficient
					+ seperator + xDistance + seperator + yDistance + seperator + result.toString());
		}
		return result;
	}
	
	
	/**
	 * Check if the position in the First, Second, Third, or Fourth Quadrant.
	 * Note: <strong> We are every time in quadrant 2,3 and the foe in 1,4</strong>
	 * 
	 * @param position to check
	 * @return 1,2,3,4 for the number of the quadrant
	 * @author PhilippP (Ph.Posovszky@gmail.com)
	 */
	public static int checkQuadrant(final IVector2 position)
	{
		if ((position.x() >= 0) && (position.y() >= 0))
		{
			return 1;
		} else if ((position.x() < 0) && (position.y() > 0))
		{
			return 2;
		} else if ((position.x() <= 0) && (position.y() <= 0))
		{
			return 3;
		} else
		{
			return 4;
		}
		
	}
	
	
	/**
	 * Get the nearest point to p from the list
	 * 
	 * @param list
	 * @param p
	 * @return
	 */
	public static IVector2 nearestPointInList(final List<IVector2> list, final IVector2 p)
	{
		if (list.isEmpty())
		{
			return p;
		}
		IVector2 closest = null;
		double closestDist = Double.MAX_VALUE;
		for (IVector2 vec : list)
		{
			double dist = GeoMath.distancePP(vec, p);
			if (closestDist > dist)
			{
				closestDist = dist;
				closest = vec;
			}
		}
		return closest;
	}
	
	
	/**
	 * Convert a bot-local vector to the equivalent global one.
	 * 
	 * @param local Bot-local vector
	 * @param wpAngle Orientation of the bot
	 * @return Properly turned global vector
	 * @author AndreR
	 */
	public static Vector2 convertLocalBotVector2Global(final IVector2 local, final double wpAngle)
	{
		return local.turnNew(-AngleMath.PI_HALF + wpAngle);
	}
	
	
	/**
	 * Convert a global vector to a bot-local one
	 * 
	 * @param global Global vector
	 * @param wpAngle Orientation of the bot
	 * @return Properly turned local vector
	 * @author AndreR
	 */
	public static Vector2 convertGlobalBotVector2Local(final IVector2 global, final double wpAngle)
	{
		return global.turnNew(AngleMath.PI_HALF - wpAngle);
	}
	
	
	/**
	 * Convert a global bot angle to a bot-local one
	 * 
	 * @param angle global angle
	 * @return local angle
	 * @author AndreR
	 */
	public static double convertGlobalBotAngle2Local(final double angle)
	{
		return AngleMath.PI_HALF - angle;
	}
	
	
	/**
	 * Convert a local bot angle to a global one
	 * 
	 * @param angle local angle
	 * @return global angle
	 * @author AndreR
	 */
	public static double convertLocalBotAngle2Global(final double angle)
	{
		return -AngleMath.PI_HALF + angle;
	}
	
	
	/**
	 * Calculates the position of the dribbler/kicker depending on bot position and orientation (angle)
	 * 
	 * @param botPos
	 * @param orientation
	 * @param center2Dribbler
	 * @return
	 */
	public static IVector2 getBotKickerPos(final IVector2 botPos, final double orientation, final double center2Dribbler)
	{
		
		return botPos.addNew(new Vector2(orientation).scaleTo(center2Dribbler));
	}
}
