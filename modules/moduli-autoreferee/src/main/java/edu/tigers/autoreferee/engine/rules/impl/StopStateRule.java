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
import java.util.List;
import java.util.Optional;

import com.github.g3force.configurable.Configurable;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Command;
import edu.tigers.autoreferee.AutoRefConfig;
import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.IRuleEngineFrame;
import edu.tigers.autoreferee.engine.NGeometry;
import edu.tigers.autoreferee.engine.RefCommand;
import edu.tigers.autoreferee.engine.rules.RuleResult;
import edu.tigers.autoreferee.engine.rules.impl.violations.BallLeftFieldRule;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.shapes.rectangle.Rectangle;
import edu.tigers.sumatra.wp.data.EGameStateNeutral;
import edu.tigers.sumatra.wp.data.TrackedBall;


/**
 * The stop state rule initiates new actions depending on the queue follow up action. If the ball is not in the correct
 * position for the action a ball placement command is issued. If a ball placement has been attempted but the ball is
 * still not in the correct position the game will restart anyway after a maximum wait time
 * {@value StopStateRule#maxUnplacedWaitTime}.
 * 
 * @author "Lukas Magel"
 */
public class StopStateRule extends APreparingGameRule
{
	private static int	priority					= 1;
	
	@Configurable(comment = "Time to wait before performing an action after reaching the stop state in [ms]")
	private static long	stopWaitTime			= 2_000;	// ms
																			
	@Configurable(comment = "The time to wait before performing an action although the ball is not placed correctly")
	private static long	maxUnplacedWaitTime	= 20_000;
	
	private Long			entryTime;
	
	
	static
	{
		AGameRule.registerClass(BallLeftFieldRule.class);
	}
	
	
	/**
	 *
	 */
	public StopStateRule()
	{
		super(EGameStateNeutral.STOPPED);
	}
	
	
	@Override
	public int getPriority()
	{
		return priority;
	}
	
	
	@Override
	protected void prepare(final IRuleEngineFrame frame)
	{
		entryTime = frame.getTimestamp();
	}
	
	
	@Override
	public Optional<RuleResult> doUpdate(final IRuleEngineFrame frame)
	{
		if (!frame.getFollowUp().isPresent())
		{
			return Optional.empty();
		}
		
		FollowUpAction action = frame.getFollowUp().get();
		
		/*
		 * Wait a minimum amount of time before doing anything
		 */
		if ((frame.getTimestamp() - entryTime) < (stopWaitTime * 1_000_000))
		{
			return Optional.empty();
		}
		
		Rectangle field = NGeometry.getField();
		TrackedBall ball = frame.getWorldFrame().getBall();
		IVector2 kickPos = determineKickPos(action);
		
		boolean ballPlaced = ballIsPlaced(frame.getWorldFrame().getBall(), kickPos);
		boolean ballInsideField = field.isPointInShape(ball.getPos());
		boolean botsStationary = botsAreStationary(frame.getWorldFrame().getBots().values());
		boolean botsCorrectDistance = botStopDistanceIsCorrect(frame.getWorldFrame());
		
		boolean maxWaitTimeOver = (frame.getTimestamp() - entryTime) > (maxUnplacedWaitTime * 1_000_000);
		
		if ((ballPlaced && botsStationary && botsCorrectDistance) || (maxWaitTimeOver && ballInsideField))
		{
			return Optional.of(new RuleResult(action.getCommand(), null, null));
		}
		
		// Try to place the ball
		if (!placementWasAttempted(frame) && (AutoRefConfig.getBallPlacementTeams().size() > 0))
		{
			return Optional.ofNullable(handlePlacement(kickPos));
		}
		
		return Optional.empty();
	}
	
	
	/**
	 * @return
	 */
	private RuleResult handlePlacement(final IVector2 kickPos)
	{
		List<ETeamColor> teams = AutoRefConfig.getBallPlacementTeams();
		if (teams.size() == 0)
		{
			return null;
		}
		
		ETeamColor placingTeam = teams.get(0);
		ETeamColor preference = AutoRefConfig.getBallPlacementPreference();
		if ((teams.size() > 1) && (preference != ETeamColor.NEUTRAL) && (preference != ETeamColor.UNINITIALIZED))
		{
			placingTeam = preference;
		}
		
		Command cmd = placingTeam == ETeamColor.BLUE ? Command.BALL_PLACEMENT_BLUE : Command.BALL_PLACEMENT_YELLOW;
		return new RuleResult(new RefCommand(cmd, kickPos), null, null);
	}
	
	
	private IVector2 determineKickPos(final FollowUpAction action)
	{
		switch (action.getActionType())
		{
			case DIRECT_FREE:
			case INDIRECT_FREE:
			case FORCE_START:
				return action.getNewBallPosition().get();
			case KICK_OFF:
				return NGeometry.getCenter();
			default:
				throw new IllegalArgumentException("Update the StopState to handle the new ActionType: "
						+ action.getActionType());
		}
	}
	
	
	private List<ETeamColor> determineAttemptedPlacements(final IRuleEngineFrame frame)
	{
		List<ETeamColor> teams = new ArrayList<>();
		
		// Only search for attempts which were performed directly before this stop state
		List<EGameStateNeutral> stateHist = frame.getStateHistory();
		for (int i = 1; i < stateHist.size(); i++)
		{
			EGameStateNeutral state = stateHist.get(i);
			if (state == EGameStateNeutral.STOPPED)
			{
				break;
			} else if (state.isBallPlacement())
			{
				teams.add(state.getTeamColor());
			}
		}
		
		return teams;
	}
	
	
	private boolean placementWasAttempted(final IRuleEngineFrame frame)
	{
		return determineAttemptedPlacements(frame).size() > 1;
	}
	
}
