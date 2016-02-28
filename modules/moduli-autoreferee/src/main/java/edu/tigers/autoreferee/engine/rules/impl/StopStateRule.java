/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 7, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.rules.impl;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.github.g3force.configurable.Configurable;

import edu.tigers.autoreferee.AutoRefConfig;
import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.IRuleEngineFrame;
import edu.tigers.autoreferee.engine.NGeometry;
import edu.tigers.autoreferee.engine.RefCommand;
import edu.tigers.autoreferee.engine.rules.RuleResult;
import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawablePoint;
import edu.tigers.sumatra.drawable.DrawableText;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.shapes.rectangle.Rectangle;
import edu.tigers.sumatra.wp.data.EGameStateNeutral;
import edu.tigers.sumatra.wp.data.ShapeMap;
import edu.tigers.sumatra.wp.data.TrackedBall;
import edu.tigers.sumatra.wp.vis.EWpShapesLayer;


/**
 * The stop state rule initiates new actions depending on the queue follow up action. If the ball is not in the correct
 * position for the action a ball placement command is issued. If a ball placement has been attempted but the ball is
 * still not in the correct position the game will restart anyway after a maximum wait time
 * {@literal AutoRefConfig#getMaxUnplacedWaitTime()} or the ball is closely placed and the closely placed wait time has
 * elapsed.
 * 
 * <pre>
 *                                         v
 *              +-----------+         +-----------+
 * +----+   No  | Has       |  Yes    | Minimum   | No  +----+
 * |Exit| <-----+ follow-up +-------> | wait time +---> |Exit|
 * +----+       | action?   |         | over?     |     +----+
 *              +-----------+         |           |
 *                                    +----+------+
 *                                         |
 *                                         v Yes
 * 
 *       +-------------------+    Yes +---------+  No  +------------+
 *       |  Bots stationary? | <------+ Is ball +----> | Placement  |
 *       |  Stop distance    |        | placed? |      | attempted? |
 *       |  correct?         |        +---------+      ++-------+---+
 *       +--+----------+-----+                          |       |
 *          |          |                         Yes +--+       +--+ No
 *   No  +--+          |Yes                          v             v
 *       v             v
 *                                                +------+   +------------+
 *    +------+    +-------------+                 | Exit |   | Place ball |
 *    | Exit |    | Send action |                 +------+   +------------+
 *    +------+    | command     |
 *                +-------------+
 * </pre>
 * 
 * @author "Lukas Magel"
 */
public class StopStateRule extends APreparingGameRule
{
	private static int	priority					= 1;
	
	@Configurable(comment = "Time to wait before performing an action after reaching the stop state in [ms]")
	private static long	STOP_WAIT_TIME_MS		= 2_000; // ms
																		
	@Configurable(comment = "The time to wait after all bots have come to a stop and the ball has been placed correctly")
	private static long	READY_WAIT_TIME_MS	= 3_000;
	
	private long			entryTime;
	private Long			readyTime;
	
	
	static
	{
		AGameRule.registerClass(StopStateRule.class);
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
		readyTime = null;
	}
	
	
	@Override
	public Optional<RuleResult> doUpdate(final IRuleEngineFrame frame)
	{
		if (!frame.getFollowUp().isPresent())
		{
			return Optional.empty();
		}
		
		FollowUpAction action = frame.getFollowUp().get();
		
		Rectangle field = NGeometry.getField();
		TrackedBall ball = frame.getWorldFrame().getBall();
		
		IVector2 kickPos = determineKickPos(action);
		visualizeKickPos(frame.getShapes(), kickPos);
		
		/*
		 * Wait a minimum amount of time before doing anything
		 */
		if ((frame.getTimestamp() - entryTime) < TimeUnit.MILLISECONDS.toNanos(STOP_WAIT_TIME_MS))
		{
			return Optional.empty();
		}
		
		boolean ballPlaced = ballIsPlaced(frame.getWorldFrame().getBall(), kickPos);
		
		boolean ballInsideField = field.isPointInShape(ball.getPos());
		boolean maxUnplacedWaitTimeElapsed = (frame.getTimestamp() - entryTime) > TimeUnit.MILLISECONDS
				.toNanos(AutoRefConfig.getMaxUnplacedWaitTime());
		
		boolean ballIsCloselyPlaced = ballIsCloselyPlaced(frame.getWorldFrame().getBall(), kickPos);
		boolean closelyPlacedWaitTimeElapsed = (frame.getTimestamp() - entryTime) > TimeUnit.MILLISECONDS
				.toNanos(AutoRefConfig.getMaxCloselyPlacedWaitTime());
		
		boolean botsStationary = botsAreStationary(frame.getWorldFrame().getBots().values());
		boolean botsCorrectDistance = botStopDistanceIsCorrect(frame.getWorldFrame());
		boolean readyWaitTimeOver = false;
		
		if (ballPlaced && botsStationary && botsCorrectDistance)
		{
			if (readyTime == null)
			{
				readyTime = frame.getTimestamp();
			} else
			{
				long waitTimeNS = frame.getTimestamp() - readyTime;
				readyWaitTimeOver = waitTimeNS > TimeUnit.MILLISECONDS.toNanos(READY_WAIT_TIME_MS);
			}
		} else
		{
			readyTime = null;
		}
		
		
		if (ballPlaced)
		{
			if (readyWaitTimeOver)
			{
				return Optional.of(new RuleResult(action.getCommand(), null, null));
			}
		} else
		{
			if (!placementWasAttempted(frame) && (AutoRefConfig.getBallPlacementTeams().size() > 0) && ball.isOnCam())
			{
				// Try to place the ball
				return Optional.ofNullable(handlePlacement(kickPos));
			}
			
			/*
			 * Only start the next action if the MAX_UNPLACED_WAIT_TIME_MS is set to a value > 0. A value <= 0 will
			 * deactivate this override.
			 */
			if ((maxUnplacedWaitTimeElapsed && ballInsideField && (AutoRefConfig.getMaxUnplacedWaitTime() > 0))
					|| (closelyPlacedWaitTimeElapsed && ballIsCloselyPlaced && (AutoRefConfig.getMaxCloselyPlacedWaitTime() > 0)))
			{
				return Optional.of(new RuleResult(action.getCommand(), null, null));
			}
		}
		
		return Optional.empty();
	}
	
	
	private void visualizeKickPos(final ShapeMap map, final IVector2 kickPos)
	{
		List<IDrawableShape> shapes = map.get(EWpShapesLayer.AUTOREFEREE);
		
		double radius = AutoRefConfig.getBallPlacementAccuracy();
		
		shapes.add(new DrawableCircle(kickPos, radius, Color.BLUE));
		shapes.add(new DrawablePoint(kickPos, Color.BLACK));
		IVector2 textPos = kickPos.addNew(new Vector2(radius, radius));
		shapes.add(new DrawableText(textPos, "New Ball Pos", Color.BLACK));
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
		return determineAttemptedPlacements(frame).size() >= 1;
	}
	
}
