/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 7, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.rules.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import edu.tigers.autoreferee.engine.AutoRefMath;
import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.FollowUpAction.EActionType;
import edu.tigers.autoreferee.engine.IRuleEngineFrame;
import edu.tigers.autoreferee.engine.NGeometry;
import edu.tigers.autoreferee.engine.RefCommand;
import edu.tigers.autoreferee.engine.RuleViolation;
import edu.tigers.autoreferee.engine.RuleViolation.ERuleViolation;
import edu.tigers.autoreferee.engine.calc.BotPosition;
import edu.tigers.autoreferee.engine.rules.RuleResult;
import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.wp.data.EGameStateNeutral;


/**
 * The goal rule detects regular and indirect goals
 * 
 * @author "Lukas Magel"
 */
public class GoalRule extends APreparingGameRule
{
	private static int	priority				= 1;
	
	private boolean		goalDetected		= false;
	
	private BotPosition	indirectKickPos	= null;
	private boolean		indirectStillHot	= false;
	
	
	/**
	 *
	 */
	public GoalRule()
	{
		super(EGameStateNeutral.RUNNING);
	}
	
	
	@Override
	public int getPriority()
	{
		return priority;
	}
	
	
	@Override
	protected void prepare(final IRuleEngineFrame frame)
	{
		/*
		 * Save the position of the kicker in case this RUNNING state was initiated by an INDIRECT freekick.
		 * This will allow the rule to determine if an indirect goal occured
		 */
		List<EGameStateNeutral> stateHistory = frame.getStateHistory();
		if (stateHistory.size() > 1)
		{
			EGameStateNeutral lastState = stateHistory.get(1);
			if ((lastState == EGameStateNeutral.INDIRECT_KICK_BLUE)
					|| (lastState == EGameStateNeutral.INDIRECT_KICK_YELLOW))
			{
				indirectKickPos = frame.getBotLastTouchedBall();
				indirectStillHot = true;
			}
		}
	}
	
	
	@Override
	public Optional<RuleResult> doUpdate(final IRuleEngineFrame frame)
	{
		if ((indirectKickPos != null) && indirectStillHot)
		{
			BotID kickerId = indirectKickPos.getId();
			BotPosition lastKickPos = frame.getBotLastTouchedBall();
			if (!kickerId.equals(lastKickPos.getId()))
			{
				indirectStillHot = false;
			}
		}
		IVector2 ballPos = frame.getWorldFrame().getBall().getPos();
		
		if (NGeometry.ballInsideGoal(ballPos))
		{
			// The ball is inside the goal
			if (goalDetected == false)
			{
				goalDetected = true;
				
				BotPosition kickPos = frame.getBotLastTouchedBall();
				if ((indirectKickPos != null) && indirectStillHot)
				{
					// The ball was kicked from an indirect freekick -> the goal is not valid
					return Optional.of(handleIndirectGoal(ballPos, kickPos.getId().getTeamColor(), frame.getTimestamp()));
				}
				
				return Optional.of(buildGoalCommand(ballPos));
			}
		} else
		{
			goalDetected = false;
		}
		
		return Optional.empty();
	}
	
	
	private RuleResult buildGoalCommand(final IVector2 ballPos)
	{
		Line blueGoalLine = NGeometry.getGoalLine(ETeamColor.BLUE);
		Line yellowGoalLine = NGeometry.getGoalLine(ETeamColor.YELLOW);
		ETeamColor inFavor = null;
		
		if (GeoMath.distancePL(ballPos, yellowGoalLine) < GeoMath.distancePL(ballPos, blueGoalLine))
		{
			inFavor = ETeamColor.BLUE;
		} else
		{
			inFavor = ETeamColor.YELLOW;
		}
		
		Command goalCmd = inFavor == ETeamColor.BLUE ? Command.GOAL_BLUE : Command.GOAL_YELLOW;
		FollowUpAction action = new FollowUpAction(EActionType.KICK_OFF, inFavor.opposite(), null);
		return new RuleResult(Arrays.asList(new RefCommand(Command.STOP), new RefCommand(goalCmd)),
				action, null);
	}
	
	
	private RuleResult handleIndirectGoal(final IVector2 ballPos, final ETeamColor kickerColor, final long ts)
	{
		ETeamColor goalColor = NGeometry.getTeamOfClosestGoalLine(ballPos);
		
		IVector2 kickPos = null;
		if (goalColor == kickerColor)
		{
			// The ball entered the goal of the kicker --> Corner Kick
			kickPos = AutoRefMath.getClosestCornerKickPos(ballPos);
		} else
		{
			// The ball entered the goal of the other team --> Goal Kick
			kickPos = AutoRefMath.getClosestGoalKickPos(ballPos);
		}
		
		FollowUpAction followUp = new FollowUpAction(EActionType.DIRECT_FREE, kickerColor.opposite(), kickPos);
		RuleViolation violation = new RuleViolation(ERuleViolation.INDIRECT_GOAL, ts, kickerColor);
		return new RuleResult(Command.STOP, followUp, violation);
	}
	
	
	@Override
	public void doReset()
	{
		goalDetected = false;
		indirectKickPos = null;
		indirectStillHot = false;
	}
	
}
