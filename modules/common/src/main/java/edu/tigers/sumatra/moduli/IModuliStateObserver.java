/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.moduli;


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
