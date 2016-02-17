/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 7, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.rules.impl.violations;

import java.util.Optional;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Command;
import edu.tigers.autoreferee.AutoRefConfig;
import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.FollowUpAction.EActionType;
import edu.tigers.autoreferee.engine.IRuleEngineFrame;
import edu.tigers.autoreferee.engine.RuleViolation;
import edu.tigers.autoreferee.engine.RuleViolation.ERuleViolation;
import edu.tigers.autoreferee.engine.rules.RuleResult;
import edu.tigers.autoreferee.engine.rules.impl.AGameRule;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.wp.data.EGameStateNeutral;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * This rule detects ball speed violations when the game is running.
 * 
 * @author "Lukas Magel"
 */
public class BallSpeedingRule extends AGameRule
{
	private static int			priority					= 1;
	
	private static final int	REQUIRED_FRAME_COUNT	= 3;
	
	private boolean				speedingDetected		= true;
	private int						speedingFrameCount	= 0;
	
	
	/**
	 *
	 */
	public BallSpeedingRule()
	{
		super(EGameStateNeutral.RUNNING);
	}
	
	
	@Override
	public int getPriority()
	{
		return priority;
	}
	
	
	@Override
	public Optional<RuleResult> update(final IRuleEngineFrame frame)
	{
		double ballVelocity = frame.getWorldFrame().getBall().getVel().getLength2();
		if (ballVelocity > AutoRefConfig.getMaxBallVelocity())
		{
			speedingFrameCount++;
			if ((speedingFrameCount >= REQUIRED_FRAME_COUNT) && (speedingDetected == false))
			{
				speedingDetected = true;
				BotID violatorID = frame.getBotLastTouchedBall().getId();
				ITrackedBot violator = frame.getWorldFrame().getBot(violatorID);
				
				FollowUpAction action = new FollowUpAction(EActionType.INDIRECT_FREE, violator.getTeamColor().opposite(),
						violator.getPos());
				
				
				RuleViolation violation = new RuleViolation(ERuleViolation.BALL_SPEEDING, frame.getTimestamp(),
						violatorID.getTeamColor());
				return Optional.of(new RuleResult(Command.STOP, action, violation));
			}
		} else
		{
			reset();
		}
		
		return Optional.empty();
	}
	
	
	@Override
	public void reset()
	{
		speedingDetected = false;
		speedingFrameCount = 0;
	}
	
}
