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
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;

import edu.tigers.autoreferee.AutoRefConfig;
import edu.tigers.autoreferee.engine.IRuleEngineFrame;
import edu.tigers.autoreferee.engine.rules.RuleResult;
import edu.tigers.autoreferee.engine.rules.impl.AGameRule;
import edu.tigers.autoreferee.engine.rules.impl.APreparingGameRule;
import edu.tigers.autoreferee.engine.violations.IRuleViolation.ERuleViolation;
import edu.tigers.autoreferee.engine.violations.RuleViolation;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.wp.data.EGameStateNeutral;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Monitors the maximum allowed bot speed during a game stoppage
 * 
 * @author "Lukas Magel"
 */
public class BotStopSpeedRule extends APreparingGameRule
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
		super(Arrays.asList(EGameStateNeutral.STOPPED,
				EGameStateNeutral.PREPARE_KICKOFF_BLUE, EGameStateNeutral.PREPARE_KICKOFF_YELLOW));
	}
	
	
	@Override
	public int getPriority()
	{
		return priority;
	}
	
	
	@Override
	protected void prepare(final IRuleEngineFrame frame)
	{
		/*
		 * The speed of some of the bots on the field might still be to high when the game state suddenly changes from
		 * running to stopped. To avoid unnecessary violations all bots that violate the speed rule at the time the game
		 * state changes are initially added to the list of last offenders. That way a violation will only be reported if
		 * they increase their speed above the limit a second time.
		 */
		lastViolators.addAll(getViolators(frame.getWorldFrame().getBots().values()));
	}
	
	
	@Override
	public Optional<RuleResult> doUpdate(final IRuleEngineFrame frame)
	{
		Collection<ITrackedBot> bots = frame.getWorldFrame().getBots().values();
		Set<BotID> violators = getViolators(bots);
		
		Set<BotID> newViolators = Sets.difference(violators, lastViolators);
		Set<BotID> oldViolators = Sets.difference(lastViolators, violators);
		lastViolators.removeAll(oldViolators);
		
		Optional<BotID> violator = newViolators.stream().findFirst();
		if (violator.isPresent())
		{
			lastViolators.add(violator.get());
			RuleViolation violation = new RuleViolation(ERuleViolation.BOT_STOP_SPEED, frame.getTimestamp(), violator
					.get());
			return Optional.of(new RuleResult(Collections.emptyList(), null, violation));
		}
		
		return Optional.empty();
	}
	
	
	private Set<BotID> getViolators(final Collection<ITrackedBot> bots)
	{
		return bots.stream()
				.filter(bot -> bot.getVel().getLength() > AutoRefConfig.getMaxBotStopSpeed())
				.map(bot -> bot.getBotId())
				.collect(Collectors.toSet());
	}
	
	
	@Override
	public void doReset()
	{
		lastViolators.clear();
	}
	
}
