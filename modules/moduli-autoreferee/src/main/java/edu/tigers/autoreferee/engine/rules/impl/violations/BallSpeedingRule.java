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

import org.apache.log4j.Logger;

import edu.tigers.autoreferee.AutoRefConfig;
import edu.tigers.autoreferee.engine.AutoRefMath;
import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.FollowUpAction.EActionType;
import edu.tigers.autoreferee.engine.IRuleEngineFrame;
import edu.tigers.autoreferee.engine.rules.RuleResult;
import edu.tigers.autoreferee.engine.rules.impl.AGameRule;
import edu.tigers.autoreferee.engine.violations.IRuleViolation.ERuleViolation;
import edu.tigers.autoreferee.engine.violations.SpeedViolation;
import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.wp.data.EGameStateNeutral;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * This rule detects ball speed violations when the game is running.
 * 
 * @author "Lukas Magel"
 */
public class BallSpeedingRule extends AGameRule
{
	private static int				priority					= 1;
	private static final Logger	log						= Logger.getLogger(BallSpeedingRule.class);
	
	private static final int		REQUIRED_FRAME_COUNT	= 3;
	
	private boolean					speedingDetected		= true;
	private int							speedingFrameCount	= 0;
	
	
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
				if (violator == null)
				{
					log.debug("Ball Speed Violator disappeard from the field: " + violatorID);
					return Optional.empty();
				}
				
				IVector2 kickPos = AutoRefMath.getClosestFreekickPos(violator.getPos(), violator.getTeamColor().opposite());
				
				FollowUpAction action = new FollowUpAction(EActionType.INDIRECT_FREE, violator.getTeamColor().opposite(),
						kickPos);
				
				SpeedViolation violation = new SpeedViolation(ERuleViolation.BALL_SPEEDING, frame.getTimestamp(),
						violatorID, ballVelocity);
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
