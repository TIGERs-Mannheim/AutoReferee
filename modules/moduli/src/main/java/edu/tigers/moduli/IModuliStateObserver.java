/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 12.08.2010
 * Author(s): AndreR
 * *********************************************************
 */
package edu.tigers.moduli;


/**
 * Moduli state observer interface.
 * 
 * @author AndreR
 */
public interface IModuliStateObserver
{
	/**
	 * @param state the new state
	 */
	default void onModuliStateChanged(final ModulesState state)
	{
	}
}
