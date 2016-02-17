/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 13, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.rules.impl.violations;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import edu.tigers.autoreferee.AutoRefConfig;
import edu.tigers.autoreferee.engine.IRuleEngineFrame;
import edu.tigers.autoreferee.engine.RuleViolation;
import edu.tigers.autoreferee.engine.RuleViolation.ERuleViolation;
import edu.tigers.autoreferee.engine.rules.RuleResult;
import edu.tigers.autoreferee.engine.rules.impl.AGameRule;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.wp.data.EGameStateNeutral;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Monitors the maximum allowed bot speed during a game stoppage
 * 
 * @author "Lukas Magel"
 */
public class BotStopSpeedRule extends AGameRule
{
	private static final int	priority			= 1;
	
	private Set<BotID>			lastViolators	= new HashSet<>();
	
	static
	{
		AGameRule.registerClass(BotStopSpeedRule.class);
	}
	
	
	/**
	 * 
	 */
	public BotStopSpeedRule()
	{
		super(EGameStateNeutral.STOPPED);
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
		Set<BotID> violators = bots.stream().filter(bot -> bot.getVel().getLength() > AutoRefConfig.getMaxBotStopSpeed())
				.map(bot -> bot.getBotId()).collect(Collectors.toSet());
		
		Set<BotID> newViolators = new HashSet<BotID>(violators);
		newViolators.removeAll(lastViolators);
		lastViolators = violators;
		
		Optional<BotID> violator = newViolators.stream().findFirst();
		if (violator.isPresent())
		{
			RuleViolation violation = new RuleViolation(ERuleViolation.BOT_STOP_SPEED, frame.getTimestamp(), violator
					.get().getTeamColor());
			return Optional.of(new RuleResult(Collections.emptyList(), null, violation));
		}
		
		return Optional.empty();
	}
	
	
	@Override
	public void reset()
	{
		lastViolators.clear();
	}
	
}
