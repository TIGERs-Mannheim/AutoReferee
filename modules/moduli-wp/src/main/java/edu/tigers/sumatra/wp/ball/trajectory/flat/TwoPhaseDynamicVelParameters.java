/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.ball.trajectory.flat;

import edu.tigers.sumatra.geometry.BallParameters;
import edu.tigers.sumatra.geometry.Geometry;
import lombok.AllArgsConstructor;
import lombok.Value;


/**
 * Parameter class
 */
@Value
@AllArgsConstructor
public class TwoPhaseDynamicVelParameters
{
	double accSlide;
	double accRoll;
	double kSwitch;


	/**
	 * Constructor with global ball parameters.
	 */
	public TwoPhaseDynamicVelParameters()
	{
		this(Geometry.getBallParameters());
	}


	/**
	 * Constructor with ball parameters.
	 */
	private TwoPhaseDynamicVelParameters(BallParameters ballParams)
	{
		accSlide = ballParams.getAccSlide();
		accRoll = ballParams.getAccRoll();
		kSwitch = ballParams.getKSwitch();
	}
}
