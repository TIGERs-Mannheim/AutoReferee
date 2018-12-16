/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.events.impl;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import edu.tigers.autoreferee.engine.AutoRefMath;
import edu.tigers.autoreferee.engine.calc.PossibleGoalCalc.PossibleGoal;
import edu.tigers.autoreferee.engine.events.EGameEventDetectorType;
import edu.tigers.autoreferee.engine.events.IGameEvent;
import edu.tigers.autoreferee.engine.events.data.Goal;
import edu.tigers.autoreferee.engine.events.data.IndirectGoal;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Detect goals and invalid indirect goals
 */
public class GoalDetector extends AGameEventDetector
{
	static
	{
		AGameEventDetector.registerClass(GoalDetector.class);
	}
	
	/**
	 * we have to remember the last goal, because in passive mode, a goal may be detected by the autoRef, by game
	 * may not be stopped by the human ref -> in this case, we still want to detect new goals.
	 */
	private PossibleGoal lastGoal = null;
	private BotID attackerId = null;
	private boolean indirectStillHot = false;
	private boolean goalDetected = false;
	
	
	public GoalDetector()
	{
		super(EGameEventDetectorType.GOAL, EGameState.RUNNING);
	}
	
	
	@Override
	protected void doPrepare()
	{
		goalDetected = false;
		indirectStillHot = false;
		attackerId = null;
		
		/*
		 * Save the position of the kicker in case this RUNNING state was initiated by an INDIRECT freekick.
		 * This will allow the rule to determine if an indirect goal occurred
		 */
		List<GameState> stateHistory = frame.getStateHistory();
		if (stateHistory.size() > 1)
		{
			EGameState lastState = stateHistory.get(1).getState();
			if (lastState == EGameState.INDIRECT_FREE)
			{
				attackerId = frame.getWorldFrame().getBots().values().stream()
						.min(Comparator.comparingDouble(b -> b.getPos().distanceTo(frame.getWorldFrame().getBall().getPos())))
						.map(ITrackedBot::getBotId)
						.orElse(null);
				indirectStillHot = true;
			}
		}
	}
	
	
	@Override
	protected Optional<IGameEvent> doUpdate()
	{
		if (indirectStillHot && frame.getBotsLastTouchedBall().stream().noneMatch(b -> b.getBotID().equals(attackerId)))
		{
			indirectStillHot = false;
		}
		
		Optional<PossibleGoal> optGoalShot = frame.getPossibleGoal();
		if (optGoalShot.isPresent() && !optGoalShot.get().equals(lastGoal))
		{
			PossibleGoal goalShot = optGoalShot.get();
			lastGoal = goalShot;
			IVector2 ballPos = frame.getWorldFrame().getBall().getPos();
			
			if (!goalDetected)
			{
				goalDetected = true;
				if (indirectStillHot)
				{
					indirectStillHot = false;
					
					// The ball was kicked from an indirect freekick -> the goal is not valid
					IGameEvent violation = createIndirectGoalViolation(goalShot, ballPos);
					return Optional.of(violation);
				}
				
				ETeamColor goalColor = goalShot.getGoalColor();
				
				// pass correct data to goal
				
				if (attackerId == null)
				{
					return Optional
							.of(new Goal(!SumatraModel.getInstance().isSimulation(),
									BotID.createBotId(0, goalShot.getGoalColor().opposite()), ballPos,
									ballPos));
				}
				
				// Return Goal in Sim and PossibleGoal in real use
				return Optional
						.of(new Goal(!SumatraModel.getInstance().isSimulation(), attackerId, ballPos,
								getKickPos(goalColor, attackerId.getTeamColor(), ballPos)));
			}
		} else
		{
			goalDetected = false;
		}
		return Optional.empty();
	}
	
	
	private IndirectGoal createIndirectGoalViolation(final PossibleGoal goalShot, final IVector2 ballPos)
	{
		ETeamColor kickerColor = attackerId.getTeamColor();
		IVector2 kickPos = getKickPos(goalShot.getGoalColor(), kickerColor, ballPos);
		
		return new IndirectGoal(attackerId, ballPos, kickPos);
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
}
