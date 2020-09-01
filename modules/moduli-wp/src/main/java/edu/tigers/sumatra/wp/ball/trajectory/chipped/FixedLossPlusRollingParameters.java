/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.ball.trajectory.chipped;

import edu.tigers.sumatra.geometry.BallParameters;
import edu.tigers.sumatra.geometry.Geometry;
import lombok.AllArgsConstructor;
import lombok.Value;


/**
 * Parameter class
 */
@Value
@AllArgsConstructor
public class FixedLossPlusRollingParameters
{
	double chipDampingXYFirstHop;
	double chipDampingXYOtherHops;
	double chipDampingZ;
	double accRoll;
	double minHopHeight;
	double maxInterceptableHeight;


	/**
	 * Constructor with global parameters.
	 */
	public FixedLossPlusRollingParameters()
	{
		this(Geometry.getBallParameters());
	}


	/**
	 * Constructor with ball parameters.
	 */
	private FixedLossPlusRollingParameters(BallParameters ballParams)
	{
		chipDampingXYFirstHop = ballParams.getChipDampingXYFirstHop();
		chipDampingXYOtherHops = ballParams.getChipDampingXYOtherHops();
		chipDampingZ = ballParams.getChipDampingZ();
		accRoll = ballParams.getAccRoll();
		minHopHeight = ballParams.getMinHopHeight();
		maxInterceptableHeight = ballParams.getMaxInterceptableHeight();
	}
}
