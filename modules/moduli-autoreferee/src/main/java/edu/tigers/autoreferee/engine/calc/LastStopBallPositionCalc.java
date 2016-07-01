/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 1, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.calc;

import edu.tigers.autoreferee.AutoRefFrame;
import edu.tigers.sumatra.math.AVector2;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.wp.data.EGameStateNeutral;


/**
 * @author "Lukas Magel"
 */
public class LastStopBallPositionCalc implements IRefereeCalc
{
	private IVector2	lastPos	= AVector2.ZERO_VECTOR;
	
	
	@Override
	public void process(final AutoRefFrame frame)
	{
		if (frame.getGameState() == EGameStateNeutral.STOPPED)
		{
			lastPos = frame.getWorldFrame().getBall().getPos();
		}
		frame.setLastStopBallPosition(lastPos);
	}
	
}
