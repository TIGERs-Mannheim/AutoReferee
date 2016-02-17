/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 01.04.2011
 * Author(s): Malte
 * *********************************************************
 */
package edu.tigers.sumatra.shapes.circle;

import java.util.List;

import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.shapes.I2DShape;


/**
 * Circle interface.
 * 
 * @author Malte
 */
public interface ICircle extends I2DShape
{
	/**
	 * @return
	 */
	double radius();
	
	
	/**
	 * @return
	 */
	IVector2 center();
	
	
	/**
	 * @param point
	 * @param margin like the margin in css, the area around the shape with the thickness of this value
	 * @return
	 */
	boolean isPointInShape(IVector2 point, double margin);
	
	
	/**
	 * @param externalPoint
	 * @return
	 */
	List<IVector2> tangentialIntersections(final IVector2 externalPoint);
}
