/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.circle;

import edu.tigers.sumatra.math.IBoundedPath;


/**
 * An arc is a pizza peace of a circle.
 */
public interface IArc extends ICircular, IBoundedPath
{
	/**
	 * @return the startAngle
	 */
	double getStartAngle();


	/**
	 * @return the angle
	 */
	double getRotation();


	@Override
	IArc mirror();

	/**
	 * Create a new arc with a given margin in each direction
	 *
	 * @param margin a positive or negative margin
	 * @return a new arc
	 */
	@Override
	IArc withMargin(double margin);
}
