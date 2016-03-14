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

import org.apache.log4j.Logger;

import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.violations.IRuleViolation;
import edu.tigers.autoreferee.engine.violations.IViolationDetector.EViolationDetectorType;
import edu.tigers.autoreferee.engine.violations.RuleViolationEngine;


/**
 * @author "Lukas Magel"
 */
public abstract class AbstractAutoRefEngine implements IAutoRefEngine
{
	private static final Logger	log					= Logger.getLogger(AbstractAutoRefEngine.class);
	
	private RuleViolationEngine	violationEngine	= null;
	protected EEngineState			engineState			= null;
	
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
	
	
	protected void logViolation(final IRuleViolation violation)
	{
		log.warn(violation.buildLogString());
		if (violation.getFollowUpAction() != null)
		{
			logFollowUp(violation.getFollowUpAction());
		}
	}
	
	
	protected void logFollowUp(final FollowUpAction action)
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Follow-up action: ");
		builder.append(action.getActionType());
		builder.append(" | Team in favor: ");
		builder.append(action.getTeamInFavor());
		action.getNewBallPosition().ifPresent(ballPos -> {
			builder.append(" At position: ");
			builder.append(ballPos.x() + " | " + ballPos.y());
		});
		log.info(builder.toString());
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
