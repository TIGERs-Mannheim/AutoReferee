/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.ellipse;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.IBoundedPath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.circle.IArc;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.intersections.IIntersections;
import edu.tigers.sumatra.math.intersections.PathIntersectionMath;
import edu.tigers.sumatra.math.line.IHalfLine;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;

import java.util.List;


/**
 * Default implementation of an ellipse
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class Ellipse implements IEllipse
{

	private final IVector2 center;
	private final double radiusX;
	private final double radiusY;
	private final double turnAngle;


	@SuppressWarnings("unused")
	protected Ellipse()
	{
		this(Vector2f.ZERO_VECTOR, 1, 1, 0);
	}


	/**
	 * @param center
	 * @param radiusX
	 * @param radiusY
	 * @param turnAngle
	 */
	protected Ellipse(final IVector2 center, final double radiusX, final double radiusY, final double turnAngle)
	{
		if ((radiusX <= 0) || (radiusY <= 0))
		{
			throw new IllegalArgumentException("radius may not be equal or smaller than zero");
		}
		if (center == null)
		{
			throw new IllegalArgumentException("center may not be null");
		}
		this.center = center;
		this.radiusX = radiusX;
		this.radiusY = radiusY;
		this.turnAngle = AngleMath.normalizeAngle(turnAngle);
	}


	/**
	 * @param center
	 * @param radiusX
	 * @param radiusY
	 */
	protected Ellipse(final IVector2 center, final double radiusX, final double radiusY)
	{
		this(center, radiusX, radiusY, 0);
	}


	/**
	 * Copy constructor
	 *
	 * @param ellipse
	 */
	protected Ellipse(final IEllipse ellipse)
	{
		this(ellipse.center(), ellipse.getRadiusX(), ellipse.getRadiusY(), ellipse.getTurnAngle());
	}


	/**
	 * @param center
	 * @param radiusX
	 * @param radiusY
	 * @param turnAngle
	 * @return new ellipse
	 */
	public static Ellipse createTurned(final IVector2 center, final double radiusX, final double radiusY,
			final double turnAngle)
	{
		return new Ellipse(center, radiusX, radiusY, turnAngle);
	}


	/**
	 * @param center
	 * @param radiusX
	 * @param radiusY
	 * @return new ellipse
	 */
	public static Ellipse createEllipse(final IVector2 center, final double radiusX, final double radiusY)
	{
		return new Ellipse(center, radiusX, radiusY);
	}


	@Override
	public double getTurnAngle()
	{
		return turnAngle;
	}


	@Override
	public IVector2 center()
	{
		return center;
	}


	@Override
	public double getRadiusX()
	{
		return radiusX;
	}


	@Override
	public double getRadiusY()
	{
		return radiusY;
	}


	@Override
	public final boolean equals(final Object o)
	{
		if (this == o)
			return true;
		if (!(o instanceof IEllipse ellipse))
			return false;

		return SumatraMath.isEqual(getRadiusX(), ellipse.getRadiusX())
				&& SumatraMath.isEqual(getRadiusY(), ellipse.getRadiusY())
				&& SumatraMath.isEqual(getTurnAngle(), ellipse.getTurnAngle())
				&& center().equals(ellipse.center());
	}


	@Override
	public final int hashCode()
	{
		int result;
		long temp;
		result = center.hashCode();
		temp = Double.doubleToLongBits(getRadiusX());
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(getRadiusY());
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(getTurnAngle());
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		return result;
	}


	@Override
	public IEllipse withMargin(double margin)
	{
		return new Ellipse(center, radiusX + margin, radiusY + margin, turnAngle);
	}


	@Override
	public IVector2 nearestPointOnPerimeterPath(IVector2 point)
	{
		return closestPointOnPath(point);
	}


	@Override
	public List<IBoundedPath> getPerimeterPath()
	{
		return List.of(this);
	}


	@Override
	public IIntersections intersect(ILine line)
	{
		return PathIntersectionMath.intersectLineAndEllipse(line, this);
	}


	@Override
	public IIntersections intersect(IHalfLine halfLine)
	{
		return PathIntersectionMath.intersectHalfLineAndEllipse(halfLine, this);
	}


	@Override
	public IIntersections intersect(ILineSegment segment)
	{
		return PathIntersectionMath.intersectLineSegmentAndEllipse(segment, this);
	}


	@Override
	public IIntersections intersect(ICircle circle)
	{
		return PathIntersectionMath.intersectCircleAndEllipse(circle, this);
	}


	@Override
	public IIntersections intersect(IArc arc)
	{
		return PathIntersectionMath.intersectArcAndEllipse(arc, this);
	}


	@Override
	public IIntersections intersect(IEllipse ellipse)
	{
		return PathIntersectionMath.intersectEllipseAndEllipse(ellipse, this);
	}


	@Override
	public boolean isValid()
	{
		return getRadiusX() > SumatraMath.getEqualTol()
				&& getRadiusY() > SumatraMath.getEqualTol()
				&& center().isFinite();
	}


	@Override
	public IVector2 closestPointOnPath(IVector2 point)
	{
		return EllipseMath.nearestPointOnEllipseLine(this, point);
	}


	@Override
	public String toString()
	{
		return "Ellipse{" +
				"center=" + center +
				", radiusX=" + radiusX +
				", radiusY=" + radiusY +
				", turnAngle=" + turnAngle +
				'}';
	}


	@Override
	public IVector2 getFocusPositive()
	{
		return center().addNew(getFocusFromCenter());
	}


	@Override
	public IVector2 getFocusNegative()
	{
		return center().addNew(getFocusFromCenter().multiplyNew(-1));
	}


	@Override
	public IVector2 getFocusFromCenter()
	{
		final double x;
		final double y;
		if (getRadiusX() > getRadiusY())
		{
			x = SumatraMath.sqrt((getRadiusX() * getRadiusX()) - (getRadiusY() * getRadiusY()));
			y = 0;
		} else
		{
			x = 0;
			y = SumatraMath.sqrt((getRadiusY() * getRadiusY()) - (getRadiusX() * getRadiusX()));
		}
		return Vector2.fromXY(x, y).turn(getTurnAngle());
	}


	@Override
	public boolean isPointInShape(final IVector2 point)
	{
		final double lenPos = getFocusPositive().subtractNew(point).getLength2();
		final double lenNeg = getFocusNegative().subtractNew(point).getLength2();
		return (lenPos + lenNeg) <= getDiameterMax();
	}


	@Override
	public IVector2 getPathStart()
	{
		return center().addNew(Vector2.fromX(getRadiusX()).turn(getTurnAngle()));
	}


	@Override
	public IVector2 getPathEnd()
	{
		return getPathStart();
	}


	@Override
	public IVector2 getPathCenter()
	{
		return center().addNew(Vector2.fromX(-getRadiusX()).turn(getTurnAngle()));
	}


	@Override
	public double getLength()
	{
		double l = (getRadiusX() - getRadiusY()) / (getRadiusX() + getRadiusY());
		double e = 1 + ((3 * l * l) / (10.0 + SumatraMath.sqrt(4 - (3 * l * l))));
		return Math.PI * (getRadiusX() + getRadiusY()) * e;
	}


	@Override
	public IVector2 stepAlongPath(double stepSize)
	{
		return EllipseMath.stepOnCurve(this, getPathStart(), stepSize);
	}


	@Override
	public double getDiameterMax()
	{
		return Math.max(getRadiusX(), getRadiusY()) * 2;
	}
}
