/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.util;

import static java.lang.Integer.max;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Handle the automatic interchange for one team
 */
public class PerTeamInterchangeCalc
{
	@Configurable(comment = "Disable automatic robot interchange for weak bots", defValue = "false")
	private static boolean disableWeakRobotInterchange = false;
	
	static
	{
		ConfigRegistration.registerClass("wp", PerTeamInterchangeCalc.class);
	}
	
	enum InterchangeState
	{
		WEAK_INTERCHANGE,
		FORCE_INTERCHANGE
	}
	
	private Set<BotID> ignoreForInterchange = new HashSet<>();
	private Map<BotID, InterchangeState> interchangeMap = new HashMap<>();
	
	
	/**
	 * Remove bots, that are too many, from the field
	 *
	 * @param bots
	 * @param allowed
	 */
	public void forceInterchangeBots(List<ITrackedBot> bots, int allowed)
	{
		int amount = max(0, computeNBotsOnField(bots) - allowed);
		computeWeakestBots(bots).stream()
				.filter(bot -> !ignoreForInterchange.contains(bot.getBotId()))
				.limit(amount)
				.forEach(bot -> interchangeMap.put(bot.getBotId(), InterchangeState.FORCE_INTERCHANGE));
	}
	
	
	/**
	 * Interchange weak bots if necessary
	 * 
	 * @param bots
	 */
	public void weakInterchangeBots(List<ITrackedBot> bots)
	{
		if (disableWeakRobotInterchange)
		{
			return;
		}
		computeBotsNeedingInterchange(bots)
				.forEach(bot -> interchangeMap.put(bot.getBotId(), InterchangeState.WEAK_INTERCHANGE));
		
	}
	
	
	private int computeNBotsOnField(final List<ITrackedBot> bots)
	{
		return (int) bots.stream()
				.filter(bot -> !interchangeMap.containsKey(bot.getBotId()))
				.count();
	}
	
	
	private List<ITrackedBot> computeWeakestBots(List<ITrackedBot> bots)
	{
		List<ITrackedBot> result = new ArrayList<>();
		result.addAll(computeBotsNeedingInterchange(bots));
		result.addAll(bots.stream().filter(bot -> !interchangeMap.containsKey(bot.getBotId()))
				.filter(bot -> bot.getRobotInfo().isOk())
				.sorted((bot1, bot2) -> Float.compare(bot1.getRobotInfo().getBattery(), bot2.getRobotInfo().getBattery()))
				.collect(Collectors.toList()));
		return result;
	}
	
	
	private List<ITrackedBot> computeBotsNeedingInterchange(List<ITrackedBot> bots)
	{
		return bots.stream().filter(bot -> !interchangeMap.containsKey(bot.getBotId()))
				.filter(bot -> !bot.getRobotInfo().isOk())
				.filter(bot -> !ignoreForInterchange.contains(bot.getBotId()))
				.collect(Collectors.toList());
	}
	
	
	/**
	 * Remove all absent bot from internal bot map
	 * 
	 * @param bots
	 */
	public void updateInterchangeSet(List<ITrackedBot> bots)
	{
		interchangeMap.keySet().removeIf(id -> !isBotIdInList(id, bots));
	}
	
	
	public Set<BotID> getInterchangeBots()
	{
		return interchangeMap.keySet();
	}
	
	
	/**
	 * Ignores
	 * 
	 * @param id
	 */
	public void ignoreBot(BotID id)
	{
		ignoreForInterchange.add(id);
	}
	
	
	private boolean isBotIdInList(BotID id, List<ITrackedBot> bots)
	{
		return bots.stream().anyMatch(bot -> bot.getBotId() == id);
	}
	
}
