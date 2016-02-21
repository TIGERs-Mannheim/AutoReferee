/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 18, 2016
 * Author(s): Sion Sander
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.rules.impl.violations;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;

import edu.tigers.autoreferee.engine.AutoRefMath;
import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.FollowUpAction.EActionType;
import edu.tigers.autoreferee.engine.IRuleEngineFrame;
import edu.tigers.autoreferee.engine.RuleViolation;
import edu.tigers.autoreferee.engine.RuleViolation.ERuleViolation;
import edu.tigers.autoreferee.engine.rules.RuleResult;
import edu.tigers.autoreferee.engine.rules.impl.AGameRule;
import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.ids.IBotIDMap;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.referee.TeamConfig;
import edu.tigers.sumatra.shapes.circle.Circle;
import edu.tigers.sumatra.wp.data.EGameStateNeutral;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * This Rule detects attackers that touch the GoalKeeper
 * 
 * @author Simon Sander
 */
public class AttackerTouchKeeperRule extends AGameRule
{
	
	private static final int		priority					= 1;
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
		
		BotID keeperIDBlue = BotID.createBotId(TeamConfig.getKeeperIdBlue(), ETeamColor.BLUE);
		BotID keeperIDYellow = BotID.createBotId(TeamConfig.getKeeperIdYellow(), ETeamColor.YELLOW);
		ITrackedBot keeperBlue = bots.get(keeperIDBlue);
		ITrackedBot keeperYellow = bots.get(keeperIDYellow);
		
		Circle keeperBlueCircle = new Circle(keeperBlue.getPos(), MIN_KEEPER_DEFENSE);
		Circle keeperYellowCircle = new Circle(keeperYellow.getPos(), MIN_KEEPER_DEFENSE);
		
		
		List<ITrackedBot> attackingBlueBots = bots.values().stream()
				.filter(bot -> bot.getBotId().getTeamColor() == ETeamColor.BLUE)
				.collect(Collectors.toList());
		List<ITrackedBot> attackingYellowBots = bots.values().stream()
				.filter(bot -> bot.getBotId().getTeamColor() == ETeamColor.YELLOW)
				.collect(Collectors.toList());
				
		Set<BotID> yellowViolators = attackingYellowBots.stream()
				.filter(bot -> keeperBlueCircle.isPointInShape(bot.getPos(), 0)).map(bot -> bot.getBotId())
				.collect(Collectors.toSet());
				
		Set<BotID> blueViolators = attackingBlueBots.stream()
				.filter(bot -> keeperYellowCircle.isPointInShape(bot.getPos(), 0)).map(bot -> bot.getBotId())
				.collect(Collectors.toSet());
				
		Set<BotID> violators = Sets.union(yellowViolators, blueViolators);
		
		// remove the old Violators from the current Violators
		Set<BotID> newViolators = Sets.difference(violators, oldViolators);
		
		// remove the no longer active Violators from the old Violators
		oldViolators.removeAll(Sets.difference(oldViolators, violators));
		
		// get the first validator
		Optional<BotID> validatorID = newViolators.stream().findFirst();
		
		if (validatorID.isPresent() && bots.containsKey(validatorID.get()))
		{
			
			IVector2 ValidationPos = bots.get(validatorID.get()).getPos();
			ETeamColor validatorTeamColor = validatorID.get().getTeamColor();
			
			RuleViolation violation = new RuleViolation(ERuleViolation.ATTACKER_TOUCH_KEEPER, frame.getTimestamp(),
					validatorTeamColor);
			FollowUpAction followUp = new FollowUpAction(EActionType.INDIRECT_FREE, validatorTeamColor.opposite(),
					AutoRefMath.getClosestFreekickPos(ValidationPos, validatorTeamColor.opposite()));
					
			// add current validator to old validator
			oldViolators.add(validatorID.get());
			
			return Optional.of(new RuleResult(Command.STOP, followUp, violation));
			
		}
		return Optional.empty();
	}
	
	
	@Override
	public void reset()
	{
		oldViolators.clear();
	}
	
}
