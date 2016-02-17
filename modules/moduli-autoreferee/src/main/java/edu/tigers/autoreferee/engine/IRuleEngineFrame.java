/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 8, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine;

import java.util.Optional;

import edu.tigers.autoreferee.IAutoRefFrame;


/**
 * @author "Lukas Magel"
 */
public interface IRuleEngineFrame extends IAutoRefFrame
{
	/**
	 * @return
	 */
	public Optional<FollowUpAction> getFollowUp();
	
	
	@Override
	public IRuleEngineFrame getPreviousFrame();
	
}
