/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 13, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.violations.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import edu.tigers.autoreferee.AutoRefUtil.ColorFilter;
import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.NGeometry;
import edu.tigers.autoreferee.engine.violations.IRuleViolation;
import edu.tigers.autoreferee.engine.violations.IRuleViolation.ERuleViolation;
import edu.tigers.autoreferee.engine.violations.RuleViolation;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.referee.RefereeMsg;
import edu.tigers.sumatra.referee.TeamInfo;
import edu.tigers.sumatra.wp.data.EGameStateNeutral;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * This rule compares the number of bots that are located inside the field with the maximum allowed number of bots. It
 * also consults the team info sent by the referee box to detect if a team did not decrease their bot number after a
 * yellow card.
 * Currently it only returns a RuleViolation when the game state changes to {@link EGameStateNeutral#RUNNING} but does
 * not stop the game.
 * 
 * @author "Lukas Magel"
 */
public class BotNumberDetector extends AViolationDetector
{
	private static final int	priority				= 1;
	private static int			maxTeamBotCount	= 6;
	
	private int						blueLastDiff		= 0;
	private int						yellowLastDiff		= 0;
	
	
	/**
	 * 
	 */
	public BotNumberDetector()
	{
		super(Arrays.asList(EGameStateNeutral.RUNNING));
	}
	
	
	@Override
	public int getPriority()
	{
		return priority;
	}
	
	
	@Override
	public Optional<IRuleViolation> update(final IAutoRefFrame frame, final List<IRuleViolation> violations)
	{
		Collection<ITrackedBot> bots = frame.getWorldFrame().getBots().values();
		RefereeMsg refMsg = frame.getRefereeMsg();
		long ts = frame.getTimestamp();
		
		int blueDiff = getTeamOnFieldBotCount(bots, ETeamColor.BLUE)
				- getAllowedTeamBotCount(refMsg, ETeamColor.BLUE, ts);
		int yellowDiff = getTeamOnFieldBotCount(bots, ETeamColor.YELLOW)
				- getAllowedTeamBotCount(refMsg, ETeamColor.YELLOW, ts);
		
		RuleViolation violation = null;
		if ((blueDiff > blueLastDiff) && (blueDiff > 0))
		{
			blueLastDiff = blueDiff;
			violation = new RuleViolation(ERuleViolation.BOT_COUNT, frame.getTimestamp(), ETeamColor.BLUE, null);
		} else if ((yellowDiff > yellowLastDiff) && (yellowDiff > 0))
		{
			yellowLastDiff = yellowDiff;
			violation = new RuleViolation(ERuleViolation.BOT_COUNT, frame.getTimestamp(), ETeamColor.YELLOW, null);
		}
		return violation != null ? Optional.of(violation) : Optional.empty();
	}
	
	
	private int getAllowedTeamBotCount(final RefereeMsg msg, final ETeamColor color, final long curTime_ns)
	{
		TeamInfo teamInfo = color == ETeamColor.BLUE ? msg.getTeamInfoBlue() : msg.getTeamInfoYellow();
		long msgTime_ns = msg.getFrameTimestamp();
		long passedTime_us = TimeUnit.NANOSECONDS.toMicros(curTime_ns - msgTime_ns);
		
		int yellowCards = (int) teamInfo.getYellowCardsTimes().stream()
				.map(cardTime_us -> cardTime_us - passedTime_us)
				.filter(cardTime_us -> cardTime_us > 0)
				.count();
		
		return maxTeamBotCount - yellowCards;
	}
	
	
	private int getTeamOnFieldBotCount(final Collection<ITrackedBot> bots, final ETeamColor color)
	{
		return (int) bots.stream()
				.filter(ColorFilter.get(color))
				.filter(bot -> NGeometry.getField().isPointInShape(bot.getPos()))
				.count();
	}
	
	
	@Override
	public void reset()
	{
		blueLastDiff = 0;
		yellowLastDiff = 0;
	}
	
}
