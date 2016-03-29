/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 3, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine;

import java.util.List;
import java.util.Set;

import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.log.GameLog;
import edu.tigers.autoreferee.engine.violations.IRuleViolation;
import edu.tigers.autoreferee.engine.violations.IViolationDetector.EViolationDetectorType;
import edu.tigers.autoreferee.engine.violations.RuleViolationEngine;
import edu.tigers.sumatra.Referee.SSL_Referee.Stage;
import edu.tigers.sumatra.referee.RefereeMsg;
import edu.tigers.sumatra.wp.data.EGameStateNeutral;


/**
 * @author "Lukas Magel"
 */
public abstract class AbstractAutoRefEngine implements IAutoRefEngine
{
	private RuleViolationEngine	violationEngine	= null;
	protected EEngineState			engineState			= null;
	protected GameLog					gameLog				= new GameLog();
	
	private boolean					firstFrame			= true;
	
	protected enum EEngineState
	{
		RUNNING,
		PAUSED
	}
	
	
	/**
	 * 
	 */
	public AbstractAutoRefEngine()
	{
		violationEngine = new RuleViolationEngine();
		engineState = EEngineState.RUNNING;
	}
	
	
	/**
	 * @param detectors
	 */
	public AbstractAutoRefEngine(final Set<EViolationDetectorType> detectors)
	{
		violationEngine = new RuleViolationEngine(detectors);
	}
	
	
	@Override
	public synchronized void setActiveViolations(final Set<EViolationDetectorType> types)
	{
		violationEngine.setActiveDetectors(types);
	}
	
	
	protected List<IRuleViolation> getViolations(final IAutoRefFrame frame)
	{
		return violationEngine.update(frame);
	}
	
	
	@Override
	public synchronized void process(final IAutoRefFrame frame)
	{
		if (firstFrame == true)
		{
			firstFrame = false;
			onFirstFrame(frame);
		}
		gameLog.setCurrentTimestamp(frame.getTimestamp());
		
		EGameStateNeutral curGameState = frame.getGameState();
		EGameStateNeutral lastGameState = frame.getPreviousFrame().getGameState();
		if (curGameState != lastGameState)
		{
			onGameStateChange(lastGameState, curGameState);
		}
		
		Stage curStage = frame.getRefereeMsg().getStage();
		Stage previousStage = frame.getPreviousFrame().getRefereeMsg().getStage();
		if (curStage != previousStage)
		{
			onStageChange(previousStage, curStage);
		}
		
		RefereeMsg curRefMsg = frame.getRefereeMsg();
		RefereeMsg lastRefMsg = frame.getPreviousFrame().getRefereeMsg();
		if (curRefMsg.getCommandCounter() != lastRefMsg.getCommandCounter())
		{
			gameLog.addEntry(curRefMsg);
		}
	}
	
	
	protected void onFirstFrame(final IAutoRefFrame frame)
	{
		gameLog.initialize(frame.getTimestamp());
	}
	
	
	protected void onGameStateChange(final EGameStateNeutral oldGameState, final EGameStateNeutral newGameState)
	{
		gameLog.addEntry(newGameState);
	}
	
	
	protected void onStageChange(final Stage oldStage, final Stage newStage)
	{
		
	}
	
	
	protected void logViolations(final List<IRuleViolation> violations)
	{
		violations.forEach(violation -> gameLog.addEntry(violation));
	}
	
	
	@Override
	public GameLog getGameLog()
	{
		return gameLog;
	}
	
	
	@Override
	public synchronized void reset()
	{
		violationEngine.reset();
	}
	
	
	@Override
	public synchronized void resume()
	{
		violationEngine.reset();
		engineState = EEngineState.RUNNING;
	}
	
	
	@Override
	public synchronized void pause()
	{
		engineState = EEngineState.PAUSED;
	}
}
