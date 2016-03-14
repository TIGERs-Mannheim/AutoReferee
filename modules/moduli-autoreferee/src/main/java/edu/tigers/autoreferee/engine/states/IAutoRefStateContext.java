/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 2, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.states;

import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.RefCommand;


/**
 * @author "Lukas Magel"
 */
public interface IAutoRefStateContext
{
	/**
	 * @param cmd
	 */
	public void sendCommand(RefCommand cmd);
	
	
	/**
	 * @return
	 */
	public FollowUpAction getFollowUpAction();
	
	
	/**
	 * @param action
	 */
	public void setFollowUpAction(FollowUpAction action);
	
	
	/**
	 * @return
	 */
	public boolean doProceed();
	
}
