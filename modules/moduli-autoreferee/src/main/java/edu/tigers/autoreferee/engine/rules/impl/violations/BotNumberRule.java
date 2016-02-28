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
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import edu.tigers.autoreferee.engine.IRuleEngineFrame;
import edu.tigers.autoreferee.engine.NGeometry;
import edu.tigers.autoreferee.engine.rules.RuleResult;
import edu.tigers.autoreferee.engine.rules.impl.AGameRule;
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
public class BotNumberRule extends AGameRule
{
	private static final int	priority				= 1;
	private static int			maxTeamBotCount	= 6;
	
	private int						blueLastDiff		= 0;
	private int						yellowLastDiff		= 0;
	
	
	/**
	 * 
	 */
	public BotNumberRule()
	{
		super(Arrays.asList(EGameStateNeutral.RUNNING));
	}
	
	
	@Override
	public int getPriority()
	{
		return priority;
	}
	
	
	@Override
	public Optional<RuleResult> update(final IRuleEngineFrame frame)
	{
		Collection<ITrackedBot> bots = frame.getWorldFrame().getBots().values();
		RefereeMsg refMsg = frame.getRefereeMsg();
		
		int blueDiff = getTeamOnFieldBotCount(bots, ETeamColor.BLUE) - getAllowedTeamBotCount(refMsg, ETeamColor.BLUE);
		int yellowDiff = getTeamOnFieldBotCount(bots, ETeamColor.YELLOW)
				- getAllowedTeamBotCount(refMsg, ETeamColor.YELLOW);
		
		RuleViolation violation = null;
		if ((blueDiff > blueLastDiff) && (blueDiff > 0))
		{
			blueLastDiff = blueDiff;
			violation = new RuleViolation(ERuleViolation.BOT_COUNT, frame.getTimestamp(), ETeamColor.BLUE);
		} else if ((yellowDiff > yellowLastDiff) && (yellowDiff > 0))
		{
			yellowLastDiff = yellowDiff;
			violation = new RuleViolation(ERuleViolation.BOT_COUNT, frame.getTimestamp(), ETeamColor.YELLOW);
		}
		return violation != null ? Optional.of(new RuleResult(Collections.emptyList(), null, violation)) : Optional
				.empty();
	}
	
	
	private int getAllowedTeamBotCount(final RefereeMsg msg, final ETeamColor color)
	{
		long curTime = System.currentTimeMillis() * 1_000; // us
		long msgTime = msg.getPacketTimestamp();
		TeamInfo teamInfo = color == ETeamColor.BLUE ? msg.getTeamInfoBlue() : msg.getTeamInfoYellow();
		
		int yellowCards = (int) teamInfo.getYellowCardsTimes().stream()
				.map(cardTime -> cardTime - (curTime - msgTime))
				.filter(cardTime -> cardTime > 0).count();
		
		return maxTeamBotCount - yellowCards;
	}
	
	
	private int getTeamOnFieldBotCount(final Collection<ITrackedBot> bots, final ETeamColor color)
	{
		return (int) bots.stream()
				.filter(bot -> bot.getBotId().getTeamColor() == color)
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
