/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 17, 2016
 * Author(s): Lukas Magel
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.rules.impl.violations;

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
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.wp.data.EGameStateNeutral;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * This class tries to detect ball dribbling
 * 
 * @author Lukas Magel
 */
public class DribblingRule extends AGameRule
{
	private static final int		priority								= 1;
	
	/** mm */
	private static final double	MAX_DRIBBLING_LENGTH				= 1000;
	/** Any distance to the ball closer than this value is considered dribbling */
	private static final double	DRIBBLING_BOT_BALL_DISTANCE	= 50;
	
	/** The position where the currently dribbling bot first touched the ball */
	private BotPosition				firstContact;
	/** The position where the currently dribbling bot last touched the ball */
	private BotPosition				lastContact;
	
	
	/**
	 * 
	 */
	public DribblingRule()
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
		BotPosition curLastContact = frame.getBotLastTouchedBall();
		if ((firstContact == null) || firstContact.getId().isUninitializedID())
		{
			if ((curLastContact != null) && !curLastContact.getId().isUninitializedID())
			{
				firstContact = curLastContact;
				lastContact = curLastContact;
			} else
			{
				return Optional.empty();
			}
		}
		
		IVector2 ballPos = frame.getWorldFrame().getBall().getPos();
		ITrackedBot bot = frame.getWorldFrame().getBot(lastContact.getId());
		ETeamColor dribblerColor = bot.getBotId().getTeamColor();
		
		if (lastContact.getTs() == curLastContact.getTs())
		{
			// The ball has not been touched since the last contact
			if (GeoMath.distancePP(bot.getPos(), ballPos) > DRIBBLING_BOT_BALL_DISTANCE)
			{
				reset();
				return Optional.empty();
			}
		} else
		{
			// The ball has been touched by a new robot
			if (lastContact.getId().equals(curLastContact.getId()))
			{
				lastContact = curLastContact;
			} else
			{
				reset();
				return Optional.empty();
			}
		}
		
		double totalDistance = GeoMath.distancePP(firstContact.getPos(), ballPos);
		if (totalDistance > MAX_DRIBBLING_LENGTH)
		{
			RuleViolation violation = new RuleViolation(ERuleViolation.BALL_DRIBBLING, frame.getTimestamp(), dribblerColor);
			FollowUpAction followUp = new FollowUpAction(EActionType.INDIRECT_FREE, dribblerColor.opposite(), ballPos);
			reset();
			return Optional.of(new RuleResult(Command.STOP, followUp, violation));
		}
		return Optional.empty();
	}
	
	
	@Override
	public void reset()
	{
		firstContact = null;
		lastContact = null;
	}
	
}
