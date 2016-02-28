/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 18, 2016
 * Author(s): Sion Sander
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.rules.impl.violations;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.google.common.collect.Sets;

import edu.tigers.autoreferee.engine.AutoRefMath;
import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.FollowUpAction.EActionType;
import edu.tigers.autoreferee.engine.IRuleEngineFrame;
import edu.tigers.autoreferee.engine.NGeometry;
import edu.tigers.autoreferee.engine.rules.RuleResult;
import edu.tigers.autoreferee.engine.rules.impl.AGameRule;
import edu.tigers.autoreferee.engine.violations.IRuleViolation.ERuleViolation;
import edu.tigers.autoreferee.engine.violations.RuleViolation;
import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.ids.IBotIDMap;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.referee.TeamConfig;
import edu.tigers.sumatra.shapes.circle.Circle;
import edu.tigers.sumatra.wp.data.EGameStateNeutral;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.PenaltyArea;


/**
 * This Rule detects attackers that touch the GoalKeeper
 * 
 * @author Simon Sander
 */
public class AttackerTouchKeeperRule extends AGameRule
{
	private static final int		priority					= 1;
	private static final Logger	log						= Logger.getLogger(AttackerTouchKeeperRule.class);
	
	private static final double	MIN_KEEPER_DEFENSE	= 200;
	private Set<BotID>				oldViolators			= new HashSet<>();
	
	
	/**
	 * 
	 */
	public AttackerTouchKeeperRule()
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
		IBotIDMap<ITrackedBot> bots = frame.getWorldFrame().getBots();
		Set<BotID> keepers = getKeepers();
		
		Set<BotID> violators = new HashSet<>();
		
		for (BotID keeperID : keepers)
		{
			ITrackedBot keeper = bots.getWithNull(keeperID);
			if (keeper == null)
			{
				log.debug("A Keeper disappeard from the field: " + keeperID);
				continue;
			}
			
			// Only check for violators if the keeper is positioned inside his own penalty area
			PenaltyArea penArea = NGeometry.getPenaltyArea(keeperID.getTeamColor());
			if (penArea.isPointInShape(keeper.getPos()))
			{
				violators.addAll(getViolators(bots, keeper));
			}
		}
		
		// remove the old Violators from the current Violators
		Set<BotID> newViolators = Sets.difference(violators, oldViolators);
		
		// remove the no longer active Violators from the old Violators
		oldViolators.removeAll(Sets.difference(oldViolators, violators));
		
		// get the first validator
		Optional<BotID> violatorID = newViolators.stream().findFirst();
		
		if (violatorID.isPresent() && bots.containsKey(violatorID.get()))
		{
			ITrackedBot violator = bots.get(violatorID.get());
			IVector2 violatorPos = violator.getPos();
			ETeamColor violatorTeamColor = violatorID.get().getTeamColor();
			
			RuleViolation violation = new RuleViolation(ERuleViolation.ATTACKER_TOUCH_KEEPER, frame.getTimestamp(),
					violatorID.get());
			FollowUpAction followUp = new FollowUpAction(EActionType.INDIRECT_FREE, violatorTeamColor.opposite(),
					AutoRefMath.getClosestFreekickPos(violatorPos, violatorTeamColor.opposite()));
			
			// add current validator to old validator
			oldViolators.add(violatorID.get());
			
			return Optional.of(new RuleResult(Command.STOP, followUp, violation));
			
		}
		return Optional.empty();
	}
	
	
	private Set<BotID> getViolators(final IBotIDMap<ITrackedBot> bots, final ITrackedBot target)
	{
		ETeamColor targetColor = target.getBotId().getTeamColor();
		Circle circle = new Circle(target.getPos(), MIN_KEEPER_DEFENSE);
		
		List<ITrackedBot> attackingBots = bots.values().stream()
				.filter(bot -> bot.getBotId().getTeamColor() == targetColor.opposite())
				.collect(Collectors.toList());
		
		return attackingBots.stream()
				.filter(bot -> circle.isPointInShape(bot.getPos(), 0))
				.map(bot -> bot.getBotId())
				.collect(Collectors.toSet());
	}
	
	
	private Set<BotID> getKeepers()
	{
		return new HashSet<>(Arrays.asList(TeamConfig.getKeeperBotIDBlue(), TeamConfig.getKeeperBotIDYellow()));
	}
	
	
	@Override
	public void reset()
	{
		oldViolators.clear();
	}
	
}
