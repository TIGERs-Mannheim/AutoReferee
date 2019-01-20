/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.detector;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;

import edu.tigers.autoreferee.engine.NGeometry;
import edu.tigers.autoreferee.generic.TimedPosition;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.AObjectID;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.referee.gameevent.BallLeftFieldGoalLine;
import edu.tigers.sumatra.referee.gameevent.ChippedGoal;
import edu.tigers.sumatra.referee.gameevent.Goal;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;
import edu.tigers.sumatra.referee.gameevent.IndirectGoal;
import edu.tigers.sumatra.referee.gameevent.PossibleGoal;
import edu.tigers.sumatra.vision.data.IKickEvent;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Detect goals, invalid indirect goals and invalid chipped goals.
 */
public class GoalDetector extends AGameEventDetector
{
	private final Logger log = Logger.getLogger(GoalDetector.class.getName());
	
	static
	{
		AGameEventDetector.registerClass(GoalDetector.class);
	}
	
	private TimedPosition lastBallLeftFieldPos;
	private boolean indirectStillHot = false;
	private BotID indirectFreeKickBot;
	private IKickEvent lastKickEvent;
	private double maxBallHeight = 0.0;
	
	
	public GoalDetector()
	{
		super(EGameEventDetectorType.GOAL, EGameState.RUNNING);
	}
	
	
	@Override
	protected void doPrepare()
	{
		indirectStillHot = false;
		maxBallHeight = 0.0;
		
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
				indirectFreeKickBot = frame.getWorldFrame().getBots().values().stream()
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
		updateIndirectDetection();
		
		updateChipDetection();
		
		if (!frame.getBallLeftFieldPos().isPresent() || frame.getBallLeftFieldPos().get().similarTo(lastBallLeftFieldPos))
		{
			return Optional.empty();
		}
		
		final TimedPosition ballLeftFieldPos = frame.getBallLeftFieldPos().get();
		lastBallLeftFieldPos = ballLeftFieldPos;
		
		ETeamColor forTeam = goalForTeam(ballLeftFieldPos);
		if (forTeam == null)
		{
			return Optional.empty();
		}
		
		warnIfNoKickEventPresent();
		
		final Optional<IKickEvent> kickEvent = frame.getWorldFrame().getKickEvent();
		final IVector2 kickLocation = kickEvent.map(IKickEvent::getPosition).orElse(null);
		final BotID kickingBot = kickEvent.map(IKickEvent::getKickingBot).filter(AObjectID::isBot).orElse(null);
		
		if (ballLeftFieldPos.getPos3().z() > Geometry.getGoalHeight())
		{
			// ball flew over the goal
			return Optional.of(new BallLeftFieldGoalLine(kickingBot, ballLeftFieldPos.getPos()));
		}
		
		if (maxBallHeight > RuleConstraints.getMaxRobotHeight() && kickEvent.isPresent())
		{
			// ball was chipped
			return Optional.of(new ChippedGoal(kickingBot, ballLeftFieldPos.getPos(), kickLocation, maxBallHeight));
		}
		
		if (indirectStillHot && kickEvent.isPresent())
		{
			indirectStillHot = false;
			
			// The ball was kicked from an indirect free kick -> the goal is not valid
			IGameEvent violation = new IndirectGoal(kickingBot, ballLeftFieldPos.getPos(), kickLocation);
			return Optional.of(violation);
		}
		return createEvent(ballLeftFieldPos, forTeam, kickLocation, kickingBot);
	}
	
	
	private void updateChipDetection()
	{
		final Optional<IKickEvent> kickEvent = frame.getWorldFrame().getKickEvent();
		if (kickEvent.isPresent())
		{
			if (lastKickEvent != null && kickEvent.get().getTimestamp() != lastKickEvent.getTimestamp())
			{
				maxBallHeight = 0.0;
				log.debug("New kick event: reset maxBallHeight: " + kickEvent.get() + " != " + lastKickEvent);
			}
			lastKickEvent = kickEvent.get();
		}
		maxBallHeight = Math.max(maxBallHeight, frame.getWorldFrame().getBall().getHeight());
	}
	
	
	private void warnIfNoKickEventPresent()
	{
		if (!frame.getWorldFrame().getKickEvent().isPresent())
		{
			log.warn("Goal detected, but no kick event found.");
		}
	}
	
	
	private void updateIndirectDetection()
	{
		if (indirectStillHot
				&& frame.getBotsLastTouchedBall().stream().noneMatch(b -> b.getBotID().equals(indirectFreeKickBot)))
		{
			indirectStillHot = false;
		}
	}
	
	
	private ETeamColor goalForTeam(final TimedPosition ballLeftFieldPos)
	{
		if (NGeometry.getGoal(ETeamColor.YELLOW).getGoalLine().distanceTo(ballLeftFieldPos.getPos()) < 1)
		{
			return ETeamColor.BLUE;
		} else if (NGeometry.getGoal(ETeamColor.BLUE).getGoalLine().distanceTo(ballLeftFieldPos.getPos()) < 1)
		{
			return ETeamColor.YELLOW;
		}
		return null;
	}
	
	
	private Optional<IGameEvent> createEvent(final TimedPosition ballLeftFieldPos, final ETeamColor forTeam,
			final IVector2 kickLocation, final BotID kickingBot)
	{
		// Return Goal in Sim and PossibleGoal in real use
		if (SumatraModel.getInstance().isSimulation())
		{
			return Optional.of(new Goal(forTeam, kickingBot, ballLeftFieldPos.getPos(), kickLocation));
		}
		return Optional.of(new PossibleGoal(forTeam, kickingBot, ballLeftFieldPos.getPos(), kickLocation));
	}
}
