/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.tube;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.stream.Collectors;


/**
 * Implementation of a {@link ITube}
 */
@Persistent(version = 1)
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(staticName = "create")
public class Tube implements ITube
{
	@NonNull
	private final IVector2 startCenter;
	@NonNull
	private final IVector2 endCenter;
	private final double radius;


	/**
	 * for DB only
	 */
	@SuppressWarnings("unused")
	private Tube()
	{
		startCenter = Vector2f.ZERO_VECTOR;
		endCenter = Vector2f.ZERO_VECTOR;
		radius = 1;
	}


	/**
	 * tube around LineSegment with given radius [mm]
	 * adds radius to start and endpoint of lineSegment
	 *
	 * @param lineSegment
	 * @param radius [mm]
	 * @return
	 */
	public static Tube fromLineSegment(final ILineSegment lineSegment, final double radius)
	{
		return new Tube(lineSegment.getStart(), lineSegment.getEnd(), radius);
	}


	@Override
	public double radius()
	{
		return radius;
	}


	@Override
	public IVector2 startCenter()
	{
		return startCenter;
	}


	@Override
	public IVector2 endCenter()
	{
		return endCenter;
	}


	@Override
	public IVector2 center()
	{
		return Lines.segmentFromPoints(startCenter, endCenter).getCenter();
	}


	@Override
	public boolean isPointInShape(final IVector2 point)
	{
		ILineSegment line = Lines.segmentFromPoints(startCenter, endCenter);
		return line.distanceTo(point) < radius;
	}


	@Override
	public boolean isPointInShape(final IVector2 point, final double margin)
	{
		return withMargin(margin).isPointInShape(point);
	}


	@Override
	public IVector2 nearestPointOutside(final IVector2 point)
	{
		if (startCenter.equals(endCenter))
		{
			return Circle.createCircle(startCenter, radius).nearestPointOutside(point);
		}
		var nearestPointOutside = point;
		var tubeLineSeg = Lines.segmentFromPoints(startCenter, endCenter);
		if (tubeLineSeg.distanceTo(point) < radius)
		{
			var leadPoint = tubeLineSeg.closestPointOnLine(point);
			if (leadPoint.equals(point))
			{
				var dir = tubeLineSeg.directionVector().getNormalVector().scaleTo(radius);
				nearestPointOutside = point.addNew(dir);
			} else
			{
				nearestPointOutside = LineMath.stepAlongLine(leadPoint, point, radius);
			}
		}
		return nearestPointOutside;
	}


	@Override
	public IVector2 nearestPointInside(final IVector2 point)
	{
		ILineSegment tubeLineSeg = Lines.segmentFromPoints(startCenter, endCenter);
		if (tubeLineSeg.distanceTo(point) < radius)
		{
			return point;
		}
		IVector2 leadPoint = tubeLineSeg.closestPointOnLine(point);
		return LineMath.stepAlongLine(leadPoint, point, radius - 1);
	}


	@Override
	public List<IVector2> lineIntersections(final ILine line)
	{
		if (startCenter.equals(endCenter))
		{
			return Circle.createCircle(startCenter, radius).lineIntersections(line);
		}
		ICircle startCircle = Circle.createCircle(startCenter, radius);
		ICircle endCircle = Circle.createCircle(endCenter, radius);
		ILineSegment tubeLineSeg = Lines.segmentFromPoints(startCenter, endCenter);

		List<IVector2> intersections = startCircle.lineIntersections(line).stream()
				.filter(v -> tubeLineSeg.closestPointOnLine(v).equals(startCenter))
				.collect(Collectors.toList());

		List<IVector2> intersectsAtEnd = endCircle.lineIntersections(line);
		for (IVector2 v : intersectsAtEnd)
		{
			if (tubeLineSeg.closestPointOnLine(v).equals(endCenter))
			{
				intersections.add(v);
			}
		}

		IVector2 start1 = startCenter.addNew(tubeLineSeg.directionVector().getNormalVector().scaleTo(radius));
		IVector2 end1 = endCenter.addNew(tubeLineSeg.directionVector().getNormalVector().scaleTo(radius));
		Lines.segmentFromPoints(start1, end1).intersectLine(line.v2()).ifPresent(intersections::add);
		IVector2 start2 = startCenter.addNew(tubeLineSeg.directionVector().getNormalVector().scaleTo(-radius));
		IVector2 end2 = endCenter.addNew(tubeLineSeg.directionVector().getNormalVector().scaleTo(-radius));
		Lines.segmentFromPoints(start2, end2).intersectLine(line.v2()).ifPresent(intersections::add);

		return intersections.stream().distinct().collect(Collectors.toList());
	}


	@Override
	public ITube withMargin(final double margin)
	{
		return new Tube(startCenter, endCenter, radius + margin);
	}
}
