/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.detector;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import edu.tigers.autoreferee.engine.calc.PossibleGoalCalc.PossibleGoal;
import edu.tigers.autoreferee.engine.events.Goal;
import edu.tigers.autoreferee.engine.events.IGameEvent;
import edu.tigers.autoreferee.engine.events.IndirectGoal;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
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
	private IVector2 attackerPos;
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
		attackerPos = null;
		
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
				attackerPos = frame.getWorldFrame().getBot(attackerId).getBotKickerPos();
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
					
					// The ball was kicked from an indirect free kick -> the goal is not valid
					IGameEvent violation = new IndirectGoal(attackerId, ballPos, attackerPos);
					return Optional.of(violation);
				}
				
				if (attackerId == null)
				{
					return Optional
							.of(new Goal(!SumatraModel.getInstance().isSimulation(),
									BotID.createBotId(0, goalShot.getGoalColor().opposite()), ballPos,
									Vector2.zero()));
				}
				
				// Return Goal in Sim and PossibleGoal in real use
				return Optional
						.of(new Goal(!SumatraModel.getInstance().isSimulation(), attackerId, ballPos, Vector2.zero()));
			}
		} else
		{
			goalDetected = false;
		}
		return Optional.empty();
	}
}
