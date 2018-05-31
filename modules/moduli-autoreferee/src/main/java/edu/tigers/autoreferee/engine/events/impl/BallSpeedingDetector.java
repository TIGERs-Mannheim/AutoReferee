/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine.events.impl;

import java.util.Collections;
import java.util.Optional;

import com.github.g3force.configurable.Configurable;

import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.AutoRefMath;
import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.FollowUpAction.EActionType;
import edu.tigers.autoreferee.engine.events.EGameEvent;
import edu.tigers.autoreferee.engine.events.EGameEventDetectorType;
import edu.tigers.autoreferee.engine.events.IGameEvent;
import edu.tigers.autoreferee.engine.events.SpeedViolation;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.vision.data.IKickEvent;


/**
 * This rule detects ball speed violations when the game is running.
 * 
 * @author "Lukas Magel"
 */
public class BallSpeedingDetector extends AGameEventDetector
{
	private static final int PRIORITY = 2;
	@Configurable(comment = "[m/s] The ball is not considered to be too fast if above this threshold to prevent false positives", defValue = "12.0")
	private static double topSpeedThreshold = 12.0d;
	
	@Configurable(comment = "Max waiting time [s]", defValue = "1.0")
	private static double maxWaitingTime = 1;

	private double lastSpeedEstimate = 0;
	private IKickEvent lastKickEvent;

	static
	{
		AGameEventDetector.registerClass(BallSpeedingDetector.class);
	}
	
	
	/**
	 * Create new instance
	 */
	public BallSpeedingDetector()
	{
		super(EGameEventDetectorType.BALL_SPEEDING, EGameState.RUNNING);
	}
	
	
	@Override
	public int getPriority()
	{
		return PRIORITY;
	}
	
	
	@Override
	public Optional<IGameEvent> update(final IAutoRefFrame frame)
	{
		if (frame.getWorldFrame().getKickFitState().isPresent() && frame.getWorldFrame().getKickEvent().isPresent()
				&& isNewOrSameKicker(frame.getWorldFrame().getKickEvent().get(), frame.getTimestamp())
				&& isBallInGame(frame))
		{
			lastSpeedEstimate = frame.getWorldFrame().getKickFitState().get().getKickVel().getLength() / 1000.;
			if (lastKickEvent == null)
			{
				lastKickEvent = frame.getWorldFrame().getKickEvent().get();
			}
		} else
		{
			if (lastSpeedEstimate > RuleConstraints.getMaxBallSpeed() + 0.01 && (lastSpeedEstimate < topSpeedThreshold))
			{
				SpeedViolation violation = createViolation(lastKickEvent.getKickingBot(), frame.getTimestamp());
				reset();
				return Optional.of(violation);
			}
			reset();
		}
		
		return Optional.empty();
	}
	
	
	private boolean isBallInGame(IAutoRefFrame frame)
	{
		return frame.isBallInsideField() && !frame.getPossibleGoal().isPresent();
	}


	private boolean isNewOrSameKicker(IKickEvent event, long timestamp)
	{
		return lastKickEvent == null
				|| (lastKickEvent.getKickingBot() == event.getKickingBot()
						&& (timestamp - lastKickEvent.getTimestamp()) / 1_000_000_000. < maxWaitingTime);
	}


	private SpeedViolation createViolation(BotID violator, long timestamp)
	{
		IVector2 kickPos = AutoRefMath.getClosestFreekickPos(lastKickEvent.getPosition(),
				violator.getTeamColor().opposite());

		FollowUpAction action = new FollowUpAction(EActionType.INDIRECT_FREE, violator.getTeamColor().opposite(),
				kickPos);

		return new SpeedViolation(EGameEvent.BALL_SPEED, timestamp,
				violator, action, lastSpeedEstimate, Collections.emptyList());
	}


	@Override
	public void reset()
	{
		lastSpeedEstimate = 0;
		lastKickEvent = null;
	}
	
}
