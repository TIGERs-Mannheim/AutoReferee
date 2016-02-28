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

import org.apache.log4j.Logger;

import com.github.g3force.configurable.Configurable;

import edu.tigers.autoreferee.engine.AutoRefMath;
import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.FollowUpAction.EActionType;
import edu.tigers.autoreferee.engine.IRuleEngineFrame;
import edu.tigers.autoreferee.engine.calc.BotPosition;
import edu.tigers.autoreferee.engine.rules.RuleResult;
import edu.tigers.autoreferee.engine.rules.impl.AGameRule;
import edu.tigers.autoreferee.engine.rules.impl.APreparingGameRule;
import edu.tigers.autoreferee.engine.violations.IRuleViolation.ERuleViolation;
import edu.tigers.autoreferee.engine.violations.RuleViolation;
import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.wp.data.EGameStateNeutral;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * This class tries to detect ball dribbling
 * 
 * @author Lukas Magel
 */
public class DribblingRule extends APreparingGameRule
{
	private static final int		priority								= 1;
	private static final Logger	log									= Logger.getLogger(DribblingRule.class);
	
	@Configurable(comment = "Any dribbling distance above this value in mm is considered a violation")
	private static final double	MAX_DRIBBLING_LENGTH				= 1000;
	
	@Configurable(comment = "Any distance to the ball closer than this value is considered dribbling")
	private static final double	DRIBBLING_BOT_BALL_DISTANCE	= 100;
	
	/** The position where the currently dribbling bot first touched the ball */
	private BotPosition				firstContact;
	/** The position where the currently dribbling bot last touched the ball */
	private BotPosition				lastContact;
	
	private long						resetTime;
	
	static
	{
		AGameRule.registerClass(DribblingRule.class);
	}
	
	
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
	protected void prepare(final IRuleEngineFrame frame)
	{
		resetTime = frame.getTimestamp();
	}
	
	
	@Override
	public Optional<RuleResult> doUpdate(final IRuleEngineFrame frame)
	{
		BotPosition curLastContact = frame.getBotLastTouchedBall();
		if (!(isSane(firstContact) && isSane(lastContact)))
		{
			if (isSane(curLastContact) && (curLastContact.getTs() >= resetTime))
			{
				firstContact = curLastContact;
				lastContact = curLastContact;
			} else
			{
				return Optional.empty();
			}
		}
		
		IVector2 ballPos = frame.getWorldFrame().getBall().getPos();
		ETeamColor dribblerColor = lastContact.getId().getTeamColor();
		ITrackedBot bot = frame.getWorldFrame().getBot(lastContact.getId());
		if (bot == null)
		{
			log.warn("Bot that last touched the ball disappeard from the field: " + lastContact.getId());
			return Optional.empty();
		}
		
		if (lastContact.getTs() == curLastContact.getTs())
		{
			// The ball has not been touched since the last contact
			if (GeoMath.distancePP(bot.getPos(), ballPos) > (DRIBBLING_BOT_BALL_DISTANCE + Geometry.getBotRadius()))
			{
				resetRule(frame.getTimestamp());
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
				resetRule(curLastContact.getTs());
				return Optional.empty();
			}
		}
		
		double totalDistance = GeoMath.distancePP(firstContact.getPos(), ballPos);
		if (totalDistance > MAX_DRIBBLING_LENGTH)
		{
			IVector2 kickPos = AutoRefMath.getClosestFreekickPos(ballPos, dribblerColor.opposite());
			RuleViolation violation = new RuleViolation(ERuleViolation.BALL_DRIBBLING, frame.getTimestamp(),
					lastContact.getId());
			FollowUpAction followUp = new FollowUpAction(EActionType.INDIRECT_FREE, dribblerColor.opposite(), kickPos);
			resetRule(frame.getTimestamp());
			return Optional.of(new RuleResult(Command.STOP, followUp, violation));
		}
		return Optional.empty();
	}
	
	
	@Override
	public void doReset()
	{
		firstContact = null;
		lastContact = null;
	}
	
	
	private void resetRule(final long ts)
	{
		resetTime = ts;
		doReset();
	}
	
	
	private boolean isSane(final BotPosition pos)
	{
		if (pos == null)
		{
			return false;
		} else if (pos.getId().isUninitializedID())
		{
			return false;
		}
		return true;
	}
	
}
