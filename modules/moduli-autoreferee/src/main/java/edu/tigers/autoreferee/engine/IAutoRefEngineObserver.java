package edu.tigers.autoreferee.engine;

import edu.tigers.autoreferee.engine.events.IGameEvent;


public interface IAutoRefEngineObserver
{
	/**
	 * The autoRef engine has detected a new game event
	 * 
	 * @param gameEvent
	 */
	void onNewGameEventDetected(IGameEvent gameEvent);
}
