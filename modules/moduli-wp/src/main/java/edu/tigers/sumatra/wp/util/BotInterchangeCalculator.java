/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.Referee;
import edu.tigers.sumatra.bot.RobotInfo;
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
	private static final Logger log = Logger.getLogger(BallContactCalculator.class);
	
	private static final int N_BOTS_IN_STANDARD_GAME = 6;
	
	private static final int N_BOTS_IN_MIXED_TEAM_GAME = 11;
	
	@Configurable(comment = "Disable automatic robot interchange", defValue = "false")
	private static boolean disableAutomaticRobotInterchange = false;
	
	private boolean isMixedTeam = false;
	
	private Set<BotID> botsToInterchange = new HashSet<>();
	
	static
	{
		ConfigRegistration.registerClass("wp", BotInterchangeCalculator.class);
	}
	
	
	public BotInterchangeCalculator()
	{
	}
	
	
	/**
	 * Set if mixed team
	 *
	 * @param isMixedTeam
	 */
	public BotInterchangeCalculator(boolean isMixedTeam)
	{
		this.isMixedTeam = isMixedTeam;
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
		if (disableAutomaticRobotInterchange) {
			return Collections.emptySet();
		}
		cleanupInterchangeSet(bots);
		for (ETeamColor color : ETeamColor.yellowBlueValues())
		{
			int nAllowedBots = computeNAllowedBots(msg.getTeamInfo(color));
			List<ITrackedBot> own = bots.values().stream()
					.filter(bot -> bot.getBotId().getTeamColor() == color)
					.collect(Collectors.toList());
			
			botsToInterchange.addAll(own.stream()
					.filter(bot -> !bot.getRobotInfo().isOk())
					.map(ITrackedBot::getBotId)
					.collect(Collectors.toSet()));
			
			int nBotsOnField = (int) own.stream()
					.map(ITrackedBot::getRobotInfo)
					.filter(RobotInfo::isOk)
                    .filter(bot -> !botsToInterchange.contains(bot.getBotId()))
					.count();
			
			int nForcedToInterchange = nBotsOnField - nAllowedBots;
			own.removeAll(
					own.stream().filter(bot -> botsToInterchange.contains(bot.getBotId())).collect(Collectors.toList())
            );

			if (nForcedToInterchange > 0)
			{
				botsToInterchange.addAll(selectNBotsToInterchange(own, nForcedToInterchange, msg));
			} else if (nForcedToInterchange < 0)
			{
				botsToInterchange.removeAll(selectNBotsToIntroduce(-nForcedToInterchange, msg));
			}
		}
		return botsToInterchange;
	}
	
	
	private void cleanupInterchangeSet(IBotIDMap bots)
	{
		botsToInterchange.removeIf(b -> !bots.containsKey(b));
	}
	
	
	private int computeNAllowedBots(TeamInfo teamInfo)
	{
		int nCards = teamInfo.getYellowCardsTimes().size() + teamInfo.getRedCards();
		if (isMixedTeam)
		{
			return N_BOTS_IN_MIXED_TEAM_GAME - nCards;
		} else
		{
			return N_BOTS_IN_STANDARD_GAME - nCards;
		}
	}
	
	
	private boolean isBotKeeper(BotID botId, RefereeMsg msg)
	{
		return botId == msg.getKeeperBotID(botId.getTeamColor());
	}
	
	
	private Set<BotID> selectNBotsToInterchange(List<ITrackedBot> bots, int amount, RefereeMsg msg)
	{
		return bots
				.stream()
				.filter(bot -> !isBotKeeper(bot.getBotId(), msg)) // we never interchange keepers
				.sorted((bot1, bot2) -> Float.compare(bot2.getRobotInfo().getBattery(),
						bot1.getRobotInfo().getBattery()))
				.distinct()
				.limit(amount)
				.map(ITrackedBot::getBotId)
				.collect(Collectors.toSet());
	}
	
	
	private Set<BotID> selectNBotsToIntroduce(int amount, RefereeMsg msg)
	{
		if (msg.getCommand() != Referee.SSL_Referee.Command.STOP)
		{
			return Collections.emptySet(); // we only introduce bots in STOP
		}
		
		return botsToInterchange.stream().limit(amount).collect(Collectors.toSet());
	}
}
