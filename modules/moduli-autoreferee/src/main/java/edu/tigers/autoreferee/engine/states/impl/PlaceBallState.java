/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine.states.impl;


import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import edu.tigers.autoreferee.AutoRefConfig;
import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.AutoRefGlobalState;
import edu.tigers.autoreferee.engine.AutoRefMath;
import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.RefboxRemoteCommand;
import edu.tigers.autoreferee.engine.events.EGameEvent;
import edu.tigers.autoreferee.engine.events.GameEvent;
import edu.tigers.autoreferee.engine.states.IAutoRefStateContext;
import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * This rule handles the ball placement states. It waits for the ball to be placed by the responsible team or sends a
 * new ball placement command if the ball is not placed after a certain amount of time. If no other team is capable of
 * placing the ball or the ball has been placed the rule will issue a {@link Command#STOP} command.
 * 
 * @author "Lukas Magel"
 */
public class PlaceBallState extends AbstractAutoRefState
{
	private static final Logger log = Logger.getLogger(PlaceBallState.class.getName());
	
	private boolean stopSend = false;
	
	
	@Override
	protected void prepare(final IAutoRefFrame frame, final IAutoRefStateContext ctx)
	{
		super.prepare(frame, ctx);
		ctx.getAutoRefGlobalState().setBallPlacementStage(AutoRefGlobalState.EBallPlacementStage.IN_PROGRESS);
	}
	
	
	@Override
	public void doUpdate(final IAutoRefFrame frame, final IAutoRefStateContext ctx)
	{
		IVector2 targetPos = frame.getRefereeMsg().getBallPlacementPosNeutral();
		if (criteriaAreMet(frame, targetPos))
		{
			// The ball has been placed at the kick position. Return to the stopped state to perform the action
			sendCommandIfReady(ctx, new RefboxRemoteCommand(Command.STOP, null), !stopSend);
			stopSend = true;
			
			ctx.getAutoRefGlobalState().setBallPlacementStage(AutoRefGlobalState.EBallPlacementStage.SUCCEEDED);
			
			// reset failure log
			ctx.getAutoRefGlobalState().getFailedBallPlacements().put(frame.getGameState().getForTeam(), 0);
			return;
		}
		
		// Wait until the team has had enough time to place the ball
		// For teams with 5 failed attempts, the placement will fail immediately
		if (ctx.getAutoRefGlobalState().getFailedBallPlacements().getOrDefault(frame.getGameState().getForTeam(), 0) < 5
				&& (frame.getTimestamp() - getEntryTime()) < TimeUnit.MILLISECONDS
						.toNanos(AutoRefConfig.getBallPlacementWindow()))
		{
			return;
		}
		
		RefboxRemoteCommand cmd = determineNextAction(frame, ctx);
		sendCommandIfReady(ctx, cmd, !stopSend);
		ctx.getAutoRefGlobalState().setBallPlacementStage(AutoRefGlobalState.EBallPlacementStage.FAILED);
		
		if (!stopSend)
		{
			logFailure(frame, ctx);
		}
		
		stopSend = cmd.getCommand() == Command.STOP;
	}
	
	
	private RefboxRemoteCommand determineNextAction(final IAutoRefFrame frame, final IAutoRefStateContext ctx)
	{
		int numberOfFailures = ctx.getAutoRefGlobalState().getFailedBallPlacements()
				.getOrDefault(frame.getGameState().getForTeam(), 0) + 1;
		String customMessage = null;
		if (numberOfFailures > 1)
		{
			customMessage = String.format("Placement failed %d times in a row", numberOfFailures);
		}
		
		final RefboxRemoteCommand cmd;
		if (ctx.getFollowUpAction() == null ||
				(frame.getGameState().getForTeam() != ctx.getFollowUpAction().getTeamInFavor()
						&& ctx.getFollowUpAction().getTeamInFavor() != ETeamColor.NEUTRAL))
		{
			// ball placement failed, but was not executed by team in favor
			GameEvent gameEvent = new GameEvent(EGameEvent.BALL_PLACEMENT_FAILED,
					frame.getTimestamp(),
					frame.getRefereeMsg().getCommand() == Command.BALL_PLACEMENT_BLUE ? ETeamColor.BLUE : ETeamColor.YELLOW,
					null);
			gameEvent.setCustomMessage(customMessage);
			cmd = new RefboxRemoteCommand(Command.STOP, gameEvent.toProtobuf());
		} else
		{
			FollowUpAction followUpAction = new FollowUpAction(FollowUpAction.EActionType.INDIRECT_FREE,
					frame.getGameState().getForTeam().opposite(),
					frame.getRefereeMsg().getBallPlacementPosNeutral());
			GameEvent gameEvent = new GameEvent(EGameEvent.BALL_PLACEMENT_FAILED,
					frame.getTimestamp(),
					frame.getGameState().getForTeam(),
					followUpAction);
			gameEvent.setCustomMessage(customMessage);
			ctx.setFollowUpAction(followUpAction);
			cmd = new RefboxRemoteCommand(Command.STOP, gameEvent.toProtobuf());
		}
		
		return cmd;
	}
	
	
	private void logFailure(final IAutoRefFrame frame, final IAutoRefStateContext ctx)
	{
		ETeamColor team = frame.getGameState().getForTeam();
		int failures = ctx.getAutoRefGlobalState().getFailedBallPlacements()
				.compute(team, (k, v) -> v == null ? 1 : v + 1);
		if (failures <= 5)
		{
			log.info(String.format("Ball placement failed %d times for team %s", failures, team));
		}
	}
	
	
	private boolean criteriaAreMet(final IAutoRefFrame frame, final IVector2 targetPos)
	{
		ITrackedBall ball = frame.getWorldFrame().getBall();
		final ETeamColor forTeam = frame.getGameState().getForTeam();
		// only respect placing teams bots. If opponent bots are in the way, the placement should not fail
		Collection<ITrackedBot> bots = frame.getWorldFrame().getBots().values().stream()
				.filter(b -> b.getBotId().getTeamColor() == forTeam).collect(Collectors.toList());
		boolean botDistanceCorrect = AutoRefMath.botStopDistanceIsCorrect(bots, ball.getPos());
		boolean ballPlaced = AutoRefMath.ballIsPlaced(ball, targetPos, AutoRefConfig.getRobotBallPlacementAccuracy());
		boolean ballStationary = AutoRefMath.ballIsStationary(ball);
		
		
		return botDistanceCorrect && ballPlaced && ballStationary;
	}
	
	
	@Override
	public void doReset()
	{
		stopSend = false;
	}
}
