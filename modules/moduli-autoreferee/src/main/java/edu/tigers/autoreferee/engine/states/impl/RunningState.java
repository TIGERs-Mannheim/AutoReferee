/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 7, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.states.impl;

import java.util.Optional;

import org.apache.log4j.Logger;

import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.FollowUpAction.EActionType;
import edu.tigers.autoreferee.engine.RefCommand;
import edu.tigers.autoreferee.engine.calc.PossibleGoalCalc.PossibleGoal;
import edu.tigers.autoreferee.engine.states.IAutoRefStateContext;
import edu.tigers.autoreferee.engine.violations.IRuleViolation;
import edu.tigers.autoreferee.engine.violations.IRuleViolation.ERuleViolation;
import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.wp.data.Geometry;


/**
 * The goal rule detects regular and indirect goals
 * 
 * @author "Lukas Magel"
 */
public class RunningState extends AbstractAutoRefState
{
	private static final Logger	log					= Logger.getLogger(RunningState.class);
	
	private boolean					goalDetected		= false;
	private boolean					indirectDetected	= false;
	
	
	/**
	 *
	 */
	public RunningState()
	{
		
	}
	
	
	@Override
	public void handleViolation(final IRuleViolation violation, final IAutoRefStateContext ctx)
	{
		switch (violation.getType())
		{
			case BALL_LEFT_FIELD:
				if (goalDetected
						|| ((ctx.getFollowUpAction() != null) && (ctx.getFollowUpAction().getActionType() == EActionType.KICK_OFF)))
				{
					log.debug("Dropping " + ERuleViolation.BALL_LEFT_FIELD + " violation since a goal has been detected");
					return;
				}
				break;
			case INDIRECT_GOAL:
				indirectDetected = true;
				break;
			default:
				break;
		}
		super.handleViolation(violation, ctx);
	}
	
	
	@Override
	public void doUpdate(final IAutoRefFrame frame, final IAutoRefStateContext ctx)
	{
		Optional<PossibleGoal> optDetectedGoal = frame.getPossibleGoal();
		
		if (optDetectedGoal.isPresent())
		{
			PossibleGoal detectedGoal = optDetectedGoal.get();
			
			if ((goalDetected == false) && (indirectDetected == false))
			{
				goalDetected = true;
				ETeamColor goalColor = detectedGoal.getGoalColor();
				Command goalCmd = goalColor == ETeamColor.BLUE ? Command.GOAL_YELLOW : Command.GOAL_BLUE;
				
				ctx.sendCommand(new RefCommand(Command.STOP));
				ctx.sendCommand(new RefCommand(goalCmd));
				
				FollowUpAction followUp = new FollowUpAction(EActionType.KICK_OFF, goalColor, Geometry.getCenter());
				ctx.setFollowUpAction(followUp);
			}
		} else
		{
			doReset();
		}
	}
	
	
	@Override
	public void doReset()
	{
		goalDetected = false;
		indirectDetected = false;
	}
	
}
