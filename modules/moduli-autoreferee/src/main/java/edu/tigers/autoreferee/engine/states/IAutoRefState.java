/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 2, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.states;

import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.violations.IRuleViolation;


/**
 * @author "Lukas Magel"
 */
public interface IAutoRefState
{
	/**
	 * @param ctx
	 * @return true if the request can be fulfilled
	 */
	public boolean proceed(IAutoRefStateContext ctx);
	
	
	/**
	 * @param frame
	 * @param ctx
	 */
	public void update(IAutoRefFrame frame, IAutoRefStateContext ctx);
	
	
	/**
	 * @param violation
	 * @param ctx
	 */
	public void handleViolation(IRuleViolation violation, IAutoRefStateContext ctx);
	
	
	/**
	 * @return
	 */
	public boolean canProceed();
	
	
	/**
	 * 
	 */
	public void reset();
}
