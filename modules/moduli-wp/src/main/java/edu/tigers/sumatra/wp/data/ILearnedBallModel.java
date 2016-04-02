/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 31, 2016
 * Author(s): ArneS <arne.sachtler@dlr.de>
 * *********************************************************
 */
package edu.tigers.sumatra.wp.data;

import edu.tigers.sumatra.math.IVector2;


/**
 * @author ArneS <arne.sachtler@dlr.de>
 */
public interface ILearnedBallModel
{
	/**
	 * @param currentPos
	 * @param currentVel
	 * @param initialVel
	 * @param time
	 * @return
	 */
	public IVector2 getPosByTime(final IVector2 currentPos, final IVector2 currentVel, final IVector2 initialVel,
			double time);
	
	
	/**
	 * @param currentVel
	 * @param initialVel
	 * @param time
	 * @return
	 */
	public double getDistByTime(final IVector2 currentVel, final IVector2 initialVel, double time);
	
	
	/**
	 * @param currentPos
	 * @param currentVel
	 * @param initialVel
	 * @param velocity
	 * @return
	 */
	public IVector2 getPosByVel(final IVector2 currentPos, final IVector2 currentVel, final IVector2 initialVel,
			final double velocity);
	
	
	/**
	 * @param currentPos
	 * @param currentVel
	 * @param initialVel
	 * @param velocity
	 * @return
	 */
	public double getDistByVel(final IVector2 currentPos, final IVector2 currentVel, final IVector2 initialVel,
			double velocity);
	
	
	/**
	 * @param currentVel
	 * @param initialVel
	 * @param dist
	 * @return
	 */
	public double getTimeByDist(final double currentVel, final double initialVel, final double dist);
	
	
	/**
	 * @param currentVel
	 * @param initialVel
	 * @param velocity
	 * @return
	 */
	public double getTimeByVel(final double currentVel, final double initialVel, final double velocity);
	
	
	/**
	 * @param currentVel
	 * @param initialVel
	 * @param dist
	 * @return
	 */
	public double getVelByDist(final double currentVel, final double initialVel, final double dist);
	
	
	/**
	 * @param endVel
	 * @param time
	 * @return
	 */
	public double getVelForTime(final double endVel, final double time);
	
	
	/**
	 * @param dist
	 * @param endVel
	 * @return
	 */
	public double getVelForDist(final double dist, final double endVel);
	
}
