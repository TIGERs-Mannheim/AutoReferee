/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.polygon;

import edu.tigers.sumatra.math.I2DShape;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.line.ILineBase;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import lombok.Value;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * @author KaiE
 */
@Value
public class Polygon implements I2DShape
{

	private static final double ACCURACY = 1e-3;
	private static final double SQRT2 = SumatraMath.sqrt(2);
	List<IVector2> points;
	IVector2 centroid;


	/**
	 * @param polyPoints
	 * @param centroid
	 */
	public Polygon(Collection<IVector2> polyPoints, IVector2 centroid)
	{
		this.points = List.copyOf(polyPoints);
		this.centroid = centroid;
	}


	private IVector2 getMarginPoint(final IVector2 point, final double margin)
	{
		return point.subtractNew(centroid).scaleTo(SQRT2 * margin).add(point);
	}


	/**
	 * @see <a href="http://alienryderflex.com/polygon">web-reference site for implementation</a>
	 */
	@Override
	public boolean isPointInShape(final IVector2 point, final double margin)
	{
		var j = points.size() - 1;
		var oddNodes = false;

		for (int i = 0; i < points.size(); i++)
		{
			var x = point.x();
			var y = point.y();
			var a = getMarginPoint(points.get(i), margin + ACCURACY);
			var b = getMarginPoint(points.get(j), margin + ACCURACY);

			var yCheckAB = (a.y() < y) && (b.y() >= y);
			var yCheckBA = (b.y() < y) && (a.y() >= y);
			var xCheck = (a.x() <= x) || (b.x() <= x);
			if (yCheckBA || (yCheckAB && xCheck))
			{
				oddNodes ^= ((a.x() + (((y - a.y()) / (b.y() - a.y())) * (b.x() - a.x()))) < x);
			}
			j = i;
		}
		return oddNodes;


	}


	@Override
	public IVector2 nearestPointOnCircumference(IVector2 point, double margin)
	{
		var best = point;
		var minDist = Double.MAX_VALUE;

		for (int i = 0; i < points.size(); ++i)
		{
			var edge = Lines.segmentFromPoints(
					getMarginPoint(points.get(i), margin),
					getMarginPoint(points.get((i + 1) % points.size()), margin)
			);
			var nPoint = edge.closestPointOnLine(point);
			var dist = nPoint.distanceToSqr(point);

			if (minDist > dist)
			{
				minDist = dist;
				best = nPoint;
			}
		}

		return best;
	}


	/**
	 * nearest point with line hint
	 *
	 * @param point
	 * @param p2bl
	 * @param margin
	 * @return
	 */
	public IVector2 nearestPointOutside(final IVector2 point, final IVector2 p2bl, final double margin)
	{
		if (!isPointInShape(point, margin))
		{
			return point;
		}

		if (point.isCloseTo(p2bl, ACCURACY))
		{
			return nearestPointOutside(point, margin);
		}

		List<IVector2> intersections = lineIntersections(Lines.lineFromPoints(point, p2bl), margin);
		if (intersections.isEmpty())
		{
			return point;
		}
		return point.nearestTo(intersections);

	}


	@Override
	public List<IVector2> lineIntersections(ILineBase line)
	{
		return lineIntersections(line, 0);
	}


	/**
	 * intersections with polygon deformed by margin
	 *
	 * @param line
	 * @param margin
	 * @return
	 */
	public List<IVector2> lineIntersections(final ILineBase line, final double margin)
	{
		List<IVector2> result = new ArrayList<>();
		for (int i = 0; i < points.size(); ++i)
		{
			var curr = getMarginPoint(points.get(i), margin);
			var next = getMarginPoint(points.get((i + 1) % points.size()), margin);
			var path = Lines.segmentFromPoints(curr, next);
			var intersection = line.intersect(path);

			if (intersection.isPresent() && !intersection.get().isCloseTo(curr, ACCURACY))
			{
				result.add(intersection.get());
			}

		}
		return result;
	}

}
