/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 3, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine;

import java.util.List;

import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.violations.IRuleViolation;


/**
 * @author "Lukas Magel"
 */
public class PassiveAutoRefEngine extends AbstractAutoRefEngine
{
	
	@Override
	public void stop()
	{
	}
	
	
	@Override
	public AutoRefMode getMode()
	{
		return AutoRefMode.PASSIVE;
	}
	
	
	@Override
	public synchronized void process(final IAutoRefFrame frame)
	{
		if (engineState == EEngineState.PAUSED)
		{
			return;
		}
		List<IRuleViolation> violations = getViolations(frame);
		violations.forEach(violation -> logViolation(violation));
	}
}
