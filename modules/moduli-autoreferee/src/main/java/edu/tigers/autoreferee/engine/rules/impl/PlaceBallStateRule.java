/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 7, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.rules.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Command;
import edu.tigers.autoreferee.AutoRefConfig;
import edu.tigers.autoreferee.engine.IRuleEngineFrame;
import edu.tigers.autoreferee.engine.RefCommand;
import edu.tigers.autoreferee.engine.rules.RuleResult;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.wp.data.EGameStateNeutral;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;
import edu.tigers.sumatra.wp.data.TrackedBall;


/**
 * This rule handles the ball placement states. It waits for the ball to be placed by the responsible team or sends a
 * new ball placement command if the ball is not placed after a certain amount of time. If no other team is capable of
 * placing the ball or the ball has been placed the rule will issue a {@link Command#STOP} command.
 * 
 * @author "Lukas Magel"
 */
public class PlaceBallStateRule extends AGameRule
{
	private static int	priority	= 1;
	
	private Long			entryTime;
	
	
	/**
	 *
	 */
	public PlaceBallStateRule()
	{
		super(Arrays.asList(EGameStateNeutral.BALL_PLACEMENT_BLUE, EGameStateNeutral.BALL_PLACEMENT_YELLOW));
	}
	
	
	@Override
	public int getPriority()
	{
		return priority;
	}
	
	
	@Override
	public Optional<RuleResult> update(final IRuleEngineFrame frame)
	{
		updateEntryTime(frame);
		
		if (!frame.getFollowUp().isPresent() || !frame.getFollowUp().get().getNewBallPosition().isPresent())
		{
			// No idea where the ball is supposed to be placed, this situation needs to be fixed by the human ref
			return Optional.empty();
		}
		
		IVector2 kickPos = frame.getFollowUp().get().getNewBallPosition().get();
		if (criteriaAreMet(frame.getWorldFrame(), kickPos))
		{
			// The ball has been placed at the kick position. Return to the stopped state to perform the action
			return Optional.of(new RuleResult(Command.STOP, null, null));
		}
		
		// Wait until the team has had enough time to place the ball
		if ((frame.getTimestamp() - entryTime) < (AutoRefConfig.getBallPlacementWindow() * 1_000_000))
		{
			return Optional.empty();
		}
		
		return Optional.of(determineNextAction(frame, kickPos));
	}
	
	
	/**
	 * @param frame
	 * @return
	 */
	private RuleResult determineNextAction(final IRuleEngineFrame frame, final IVector2 kickPos)
	{
		List<ETeamColor> completedAttempts = determinePlacementAttempts(frame);
		List<ETeamColor> capableTeams = AutoRefConfig.getBallPlacementTeams();
		
		capableTeams.removeAll(completedAttempts);
		
		RefCommand cmd = new RefCommand(Command.STOP, null);
		
		if (capableTeams.size() >= 1)
		{
			cmd = new RefCommand(capableTeams.get(0) == ETeamColor.BLUE ? Command.BALL_PLACEMENT_BLUE
					: Command.BALL_PLACEMENT_YELLOW, kickPos);
		}
		
		
		return new RuleResult(cmd, null, null);
	}
	
	
	private List<ETeamColor> determinePlacementAttempts(final IRuleEngineFrame frame)
	{
		List<ETeamColor> placements = new ArrayList<>();
		
		// Add the team which is currently attempting to place the ball
		placements.add(frame.getGameState().getTeamColor());
		
		// Add the last state if it was also a placement attempt
		List<EGameStateNeutral> stateHistory = frame.getStateHistory();
		if ((stateHistory.size() > 1) && stateHistory.get(1).isBallPlacement())
		{
			placements.add(stateHistory.get(1).getTeamColor());
		}
		
		return placements;
	}
	
	
	private void updateEntryTime(final IRuleEngineFrame frame)
	{
		if ((entryTime == null) || (frame.getGameState() != frame.getPreviousFrame().getGameState()))
		{
			entryTime = frame.getTimestamp();
		}
	}
	
	
	private boolean criteriaAreMet(final SimpleWorldFrame frame, final IVector2 targetPos)
	{
		TrackedBall ball = frame.getBall();
		boolean botDistanceCorrect = botStopDistanceIsCorrect(frame);
		boolean ballPlaced = ballIsPlaced(ball, targetPos);
		boolean ballStationary = ballIsStationary(ball);
		
		
		return botDistanceCorrect && ballPlaced && ballStationary;
	}
	
	
	@Override
	public void reset()
	{
		entryTime = null;
	}
	
}
