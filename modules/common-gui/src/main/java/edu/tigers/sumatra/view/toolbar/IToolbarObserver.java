/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.view.toolbar;


/**
 * Toolbar observer
 *
 * @author AndreR
 */
public interface IToolbarObserver
{
	/**
	 * Start or stop all modules
	 */
	default void onStartStopModules()
	{
	}


	/**
	 * trigger emergency stop
	 */
	default void onEmergencyStop()
	{
	}


	/**
	 * Start or stop record
	 */
	default void onToggleRecord()
	{
	}
}
