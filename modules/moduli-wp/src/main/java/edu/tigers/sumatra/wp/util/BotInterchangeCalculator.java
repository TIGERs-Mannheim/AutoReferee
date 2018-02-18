/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.util;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import com.google.common.collect.Sets;

import edu.tigers.sumatra.Referee;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.ids.IBotIDMap;
import edu.tigers.sumatra.referee.data.RefereeMsg;
import edu.tigers.sumatra.referee.data.TeamInfo;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Calculate which bots should be interchanged
 *
 * @author ArneS <arne.sachtler@dlr.de>
 */
public class BotInterchangeCalculator
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(BotInterchangeCalculator.class);
	
	@Configurable(comment = "Disable automatic robot interchange", defValue = "false")
	private static boolean disableAutomaticRobotInterchange = false;
	
	static
	{
		ConfigRegistration.registerClass("wp", BotInterchangeCalculator.class);
	}
	
	private Map<ETeamColor, PerTeamInterchangeCalc> calcs = new EnumMap<>(ETeamColor.class);
	
	
	/**
	 * Creates a bot interchange calculator
	 */
	public BotInterchangeCalculator()
	{
		for (ETeamColor color : ETeamColor.yellowBlueValues())
		{
			calcs.put(color, new PerTeamInterchangeCalc());
		}
	}
	
	
	/**
	 * Compute bots that should be interchanged
	 *
	 * @param bots
	 * @param msg
	 * @return
	 */
	public Set<BotID> computeBotsToInterchange(IBotIDMap<ITrackedBot> bots, RefereeMsg msg)
	{
		if (disableAutomaticRobotInterchange)
		{
			return Collections.emptySet();
		}
		Set<BotID> returnSet = new HashSet<>();
		for (ETeamColor color : ETeamColor.yellowBlueValues())
		{
			PerTeamInterchangeCalc calc = calcs.get(color);
			calc.ignoreBot(msg.getKeeperBotID(color));
			List<ITrackedBot> own = getOwnBots(bots, color);
			int allowedBots = computeNAllowedBots(msg.getTeamInfo(color));
			if (msg.getCommand() == Referee.SSL_Referee.Command.STOP)
			{
				calc.updateInterchangeSet(own);
				calc.forceInterchangeBots(own, allowedBots);
				calc.weakInterchangeBots(own);
			}
			returnSet.addAll(calc.getInterchangeBots());
		}
		Set<BotID> keepers = Sets.newHashSet(msg.getKeeperBotID(ETeamColor.BLUE), msg.getKeeperBotID(ETeamColor.YELLOW));
		returnSet.removeAll(keepers);
		return returnSet;
	}
	
	
	private int computeNAllowedBots(TeamInfo teamInfo)
	{
		int nCards = teamInfo.getYellowCardsTimes().size() + teamInfo.getRedCards();
		return RuleConstraints.getBotsPerTeam() - nCards;
	}
	
	
	private List<ITrackedBot> getOwnBots(final IBotIDMap<ITrackedBot> bots, final ETeamColor color)
	{
		return bots.values().stream()
				.filter(bot -> bot.getBotId().getTeamColor() == color)
				.collect(Collectors.toList());
	}
}