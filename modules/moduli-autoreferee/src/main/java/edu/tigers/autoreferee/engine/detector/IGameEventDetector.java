/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine.detector;

import java.util.Optional;

import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.events.IGameEvent;
import edu.tigers.sumatra.referee.data.EGameState;


/**
 * Detects {@link IGameEvent}s
 */
public interface IGameEventDetector
{
	/**
	 * @param state
	 * @return
	 */
	boolean isActiveIn(EGameState state);
	
	
	/**
	 * @param frame
	 * @return
	 */
	Optional<IGameEvent> update(IAutoRefFrame frame);
	
	
	/**
	 * Reset
	 */
	void reset();
	
	
	/**
	 * @return the type of the game event detector
	 */
	EGameEventDetectorType getType();
}
