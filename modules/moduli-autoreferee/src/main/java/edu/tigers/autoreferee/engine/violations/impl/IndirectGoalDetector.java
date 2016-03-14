/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 2, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.violations.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.AutoRefMath;
import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.FollowUpAction.EActionType;
import edu.tigers.autoreferee.engine.calc.BotPosition;
import edu.tigers.autoreferee.engine.calc.PossibleGoalCalc.PossibleGoal;
import edu.tigers.autoreferee.engine.violations.IRuleViolation;
import edu.tigers.autoreferee.engine.violations.IRuleViolation.ERuleViolation;
import edu.tigers.autoreferee.engine.violations.RuleViolation;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.referee.TeamConfig;
import edu.tigers.sumatra.wp.data.EGameStateNeutral;


/**
 * @author "Lukas Magel"
 */
public class IndirectGoalDetector extends APreparingViolationDetector
{
	private static final int	priority				= 1;
	
	private BotPosition			indirectKickPos	= null;
	private boolean				indirectStillHot	= false;
	
	
	/**
	 * 
	 */
	public IndirectGoalDetector()
	{
		super(EGameStateNeutral.RUNNING);
	}
	
	
	@Override
	public int getPriority()
	{
		return priority;
	}
	
	
	@Override
	protected void prepare(final IAutoRefFrame frame)
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
	protected Optional<IRuleViolation> doUpdate(final IAutoRefFrame frame, final List<IRuleViolation> violations)
	{
		Set<BotID> keeperIDs = getKeeperIDs();
		if ((indirectKickPos != null) && indirectStillHot)
		{
			BotID kickerId = indirectKickPos.getId();
			BotPosition lastKickPos = frame.getBotLastTouchedBall();
			if (!kickerId.equals(lastKickPos.getId()) && (!keeperIDs.contains(lastKickPos.getId())))
			{
				indirectStillHot = false;
			}
		}
		
		Optional<PossibleGoal> optGoalShot = frame.getPossibleGoal();
		if (optGoalShot.isPresent())
		{
			PossibleGoal goalShot = optGoalShot.get();
			IVector2 ballPos = frame.getWorldFrame().getBall().getPos();
			
			if ((indirectKickPos != null) && indirectStillHot)
			{
				// The ball was kicked from an indirect freekick -> the goal is not valid
				BotID kicker = indirectKickPos.getId();
				ETeamColor kickerColor = kicker.getTeamColor();
				
				indirectStillHot = false;
				IVector2 kickPos = getKickPos(goalShot.getGoalColor(), kickerColor, ballPos);
				
				FollowUpAction followUp = new FollowUpAction(EActionType.DIRECT_FREE, kickerColor.opposite(), kickPos);
				RuleViolation violation = new RuleViolation(ERuleViolation.INDIRECT_GOAL, frame.getTimestamp(), kicker,
						followUp);
				return Optional.of(violation);
			}
		}
		return Optional.empty();
	}
	
	
	private Set<BotID> getKeeperIDs()
	{
		return new HashSet<>(Arrays.asList(TeamConfig.getKeeperBotIDBlue(), TeamConfig.getKeeperBotIDYellow()));
	}
	
	
	private IVector2 getKickPos(final ETeamColor goalColor, final ETeamColor kickerColor, final IVector2 ballPos)
	{
		if (goalColor == kickerColor)
		{
			// The ball entered the goal of the kicker --> Corner Kick
			return AutoRefMath.getClosestCornerKickPos(ballPos);
		}
		// The ball entered the goal of the other team --> Goal Kick
		return AutoRefMath.getClosestGoalKickPos(ballPos);
	}
	
	
	@Override
	protected void doReset()
	{
		indirectKickPos = null;
		indirectStillHot = false;
	}
}
