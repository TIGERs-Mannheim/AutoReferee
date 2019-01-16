/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;


public class AutoRefEngine
{
	private final GameEventEngine gameEventEngine = new GameEventEngine();
	private final List<IAutoRefEngineObserver> observers = new CopyOnWriteArrayList<>();
	
	
	public void addObserver(IAutoRefEngineObserver observer)
	{
		observers.add(observer);
	}
	
	
	public void removeObserver(IAutoRefEngineObserver observer)
	{
		observers.remove(observer);
	}
	
	
	protected List<IGameEvent> processEngine(final IAutoRefFrame frame)
	{
		return gameEventEngine.update(frame);
	}
	
	
	public void process(final IAutoRefFrame frame)
	{
		// empty
	}
	
	
	public void start()
	{
		// empty
	}
	
	
	public void stop()
	{
		// empty
	}
	
	
	protected void processGameEvent(final IGameEvent gameEvent)
	{
		observers.forEach(o -> o.onNewGameEventDetected(gameEvent));
	}
	
	
	public GameEventEngine getGameEventEngine()
	{
		return gameEventEngine;
	}
}
