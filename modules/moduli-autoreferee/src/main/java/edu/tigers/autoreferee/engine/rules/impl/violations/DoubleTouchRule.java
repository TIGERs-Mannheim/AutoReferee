/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 13, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.rules.impl.violations;

import java.util.Arrays;
import java.util.Optional;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Command;
import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.FollowUpAction.EActionType;
import edu.tigers.autoreferee.engine.IRuleEngineFrame;
import edu.tigers.autoreferee.engine.RuleViolation;
import edu.tigers.autoreferee.engine.RuleViolation.ERuleViolation;
import edu.tigers.autoreferee.engine.calc.BotPosition;
import edu.tigers.autoreferee.engine.rules.RuleResult;
import edu.tigers.autoreferee.engine.rules.impl.AGameRule;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.wp.data.EGameStateNeutral;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * This class detects a violation of the Double Touch Rule which can occur if the bot who performs a
 * kickoff/direct/indirect touches the ball a second time.
 * 
 * @author "Simon Sander"
 */
public class DoubleTouchRule extends AGameRule
{
	private static final int	priority							= 1;
																			
	private EGameStateNeutral	lastGamestae					= EGameStateNeutral.UNKNOWN;
	private IVector2				lastRefreePosition			= null;
	private boolean				foundDoubleTouchViolation	= false;
																			
																			
	/**
	 * 
	 */
	public DoubleTouchRule()
	{
		super(Arrays.asList(
				EGameStateNeutral.KICKOFF_BLUE, EGameStateNeutral.KICKOFF_YELLOW,
				EGameStateNeutral.DIRECT_KICK_BLUE, EGameStateNeutral.DIRECT_KICK_YELLOW,
				EGameStateNeutral.INDIRECT_KICK_BLUE, EGameStateNeutral.INDIRECT_KICK_YELLOW,
				EGameStateNeutral.RUNNING));
	}
	
	
	@Override
	public int getPriority()
	{
		return priority;
	}
	
	
	@Override
	public Optional<RuleResult> update(final IRuleEngineFrame frame)
	{
		boolean isDoubleTouch = false;
		EGameStateNeutral currentGameState = frame.getGameState();
		Optional<BotPosition> botLastTouchBallPosition = Optional.ofNullable(frame.getBotLastTouchedBall());
		Optional<BotPosition> currentBotTouchBallPosition = frame.getBotTouchedBall();
		
		// detect the Change of the Gamestate
		boolean hasGameStateChanged = !lastGamestae.equals(currentGameState);
		if (hasGameStateChanged)
		{
			reset();
			lastRefreePosition = frame.getWorldFrame().getBall().getPos();
		} else if ((currentGameState != EGameStateNeutral.RUNNING)
				&& currentBotTouchBallPosition.isPresent()
				&& botLastTouchBallPosition.isPresent())
		{
			isDoubleTouch = botLastTouchBallPosition.get().getId().equals(currentBotTouchBallPosition.get().getId());
		}
		
		
		BotID violatorID = frame.getBotLastTouchedBall().getId();
		ITrackedBot violator = frame.getWorldFrame().getBot(violatorID);
		
		FollowUpAction action = new FollowUpAction(EActionType.INDIRECT_FREE, violator.getTeamColor().opposite(),
				lastRefreePosition);
				
				
		RuleViolation violation = new RuleViolation(ERuleViolation.DOUBLE_TOUCH, frame.getBotLastTouchedBall().getTs(),
				violatorID.getTeamColor());
				
		if (isDoubleTouch && !foundDoubleTouchViolation)
		{
			foundDoubleTouchViolation = true;
			return Optional.of(new RuleResult(Command.STOP, action, violation));
		}
		
		lastGamestae = currentGameState;
		return Optional.empty();
	}
	
	
	@Override
	public void reset()
	{
		lastGamestae = EGameStateNeutral.UNKNOWN;
		lastRefreePosition = null;
		foundDoubleTouchViolation = false;
	}
	
	
}
