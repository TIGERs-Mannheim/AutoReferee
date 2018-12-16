/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.events.IGameEvent;
import edu.tigers.autoreferee.remote.IRefboxRemote;
import edu.tigers.sumatra.referee.data.GameState;


/**
 * @author "Lukas Magel"
 */
public class ActiveAutoRefEngine extends AbstractAutoRefEngine
{
	private final IRefboxRemote remote;
	private List<IAutoRefEngineObserver> engineObserver = new CopyOnWriteArrayList<>();
	
	
	/**
	 * @param remote
	 */
	public ActiveAutoRefEngine(final IRefboxRemote remote)
	{
		this.remote = remote;
	}
	
	
	@Override
	public synchronized void stop()
	{
		remote.stop();
	}
	

	
	@Override
	public AutoRefMode getMode()
	{
		return AutoRefMode.ACTIVE;
	}
	

	
	@Override
	public synchronized void process(final IAutoRefFrame frame)
	{
		if (engineState == EEngineState.PAUSED)
		{
			return;
		}
		
		super.process(frame);
		
		List<IGameEvent> gameEvents = getGameEvents(frame);
		
		if (!gameEvents.isEmpty())
		{
			IGameEvent gameEvent = gameEvents.remove(0);
			boolean accepted = false;
			if (activeGameEvents.contains(gameEvent.getType()))
			{
				remote.sendEvent(gameEvent);
			}
			gameLog.addEntry(gameEvent, accepted);
			logGameEvents(gameEvents);
		}
		
	}
	
	
	@Override
	protected void onGameStateChange(final GameState oldGameState, final GameState newGameState)
	{
		super.onGameStateChange(oldGameState, newGameState);
		notifyStateChange(false);
	}

	
	/**
	 * @param observer
	 */
	public void addObserver(final IAutoRefEngineObserver observer)
	{
		engineObserver.add(observer);
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final IAutoRefEngineObserver observer)
	{
		engineObserver.remove(observer);
	}
	
	
	private void notifyStateChange(final boolean canProceed)
	{
		engineObserver.forEach(obs -> obs.onStateChanged(canProceed));
	}
	
	
	/**
	 * @author Lukas Magel
	 */
	public interface IAutoRefEngineObserver
	{
		
		/**
		 * @param proceedPossible
		 */
		void onStateChanged(final boolean proceedPossible);
		
	}
}
