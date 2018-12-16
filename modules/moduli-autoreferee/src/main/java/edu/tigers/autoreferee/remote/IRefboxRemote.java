/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.remote;

import edu.tigers.autoreferee.engine.events.IGameEvent;


public interface IRefboxRemote
{
	
	void sendEvent(IGameEvent event);
	
	
	void stop();
}
