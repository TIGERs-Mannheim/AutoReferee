/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 17, 2016
 * Author(s): Lukas Magel
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.rules.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Command;
import edu.tigers.autoreferee.engine.IRuleEngineFrame;
import edu.tigers.autoreferee.engine.RuleViolation;
import edu.tigers.autoreferee.engine.RuleViolation.ERuleViolation;
import edu.tigers.autoreferee.engine.rules.RuleResult;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.wp.data.EGameStateNeutral;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * This rule monitors the bot to ball distance of the defending team during a freekick situation and restarts the play
 * if necessary.
 * 
 * @author Lukas Magel
 */
public class FreekickRule extends APreparingGameRule
{
	private static final int	priority	= 1;
	private IVector2				ballPos;
	
	
	/**
	 * 
	 */
	public FreekickRule()
	{
		super(Arrays.asList(
				EGameStateNeutral.DIRECT_KICK_BLUE, EGameStateNeutral.DIRECT_KICK_YELLOW,
				EGameStateNeutral.INDIRECT_KICK_BLUE, EGameStateNeutral.INDIRECT_KICK_YELLOW));
	}
	
	
	@Override
	public int getPriority()
	{
		return priority;
	}
	
	
	@Override
	protected void prepare(final IRuleEngineFrame frame)
	{
		ballPos = frame.getWorldFrame().getBall().getPos();
	}
	
	
	@Override
	protected Optional<RuleResult> doUpdate(final IRuleEngineFrame frame)
	{
		EGameStateNeutral state = frame.getGameState();
		ETeamColor attackingColor = state.getTeamColor();
		
		List<ITrackedBot> bots = new ArrayList<>(frame.getWorldFrame().getBots().values());
		List<ITrackedBot> defendingBots = bots.stream()
				.filter(bot -> bot.getBotId().getTeamColor() == attackingColor.opposite()).collect(Collectors.toList());
		
		Optional<ITrackedBot> optOffender = defendingBots.stream()
				.filter(bot -> GeoMath.distancePP(ballPos, bot.getPos()) < Geometry.getBotToBallDistanceStop()).findFirst();
		
		if (optOffender.isPresent())
		{
			RuleViolation violation = new RuleViolation(ERuleViolation.DEFENDER_KICK_DISTANCE, frame.getTimestamp(),
					attackingColor.opposite());
			return Optional.of(new RuleResult(Command.STOP, frame.getFollowUp().orElse(null), violation));
		}
		
		return Optional.empty();
	}
	
}
