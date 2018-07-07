/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.circle;

import java.util.List;

import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * An arc is a pizza peace of a circle.
 * 
 * @author nicolai.ommer
 */
public interface IArc extends ICircular
{
	/**
	 * @return the startAngle
	 */
	double getStartAngle();
	
	
	/**
	 * @return the angle
	 */
	double getRotation();
	
	
	/**
	 * Intersect this 2dShape instance with the specified line {@code segment} and return the intersection point.
	 *
	 * @param segment
	 *           The line segment for which to calculate an intersection with this instance
	 * @return
	 * 			The list of intersections. This can be empty, if there are no intersections.
	 */
	List<IVector2> intersectSegment(final ILineSegment segment);
	
	
	@Override
	IArc mirror();
}
