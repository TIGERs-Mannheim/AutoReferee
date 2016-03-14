/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 17, 2016
 * Author(s): Lukas Magel
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.violations.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.AutoRefMath;
import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.FollowUpAction.EActionType;
import edu.tigers.autoreferee.engine.NGeometry;
import edu.tigers.autoreferee.engine.violations.IRuleViolation;
import edu.tigers.autoreferee.engine.violations.IRuleViolation.ERuleViolation;
import edu.tigers.autoreferee.engine.violations.RuleViolation;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.wp.data.EGameStateNeutral;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.PenaltyArea;


/**
 * Monitors the distance between attackers and the defense area of the defending team when a freekick is performed.
 * 
 * @author Lukas Magel
 */
public class AttackerToDefenseAreaDistanceDetector extends AViolationDetector
{
	private static final int		priority								= 1;
	
	/** The minimum allowed distance between the bots of the attacking team and the defense area */
	private static final double	MIN_ATTACKER_DEFENSE_DISTANCE	= 200;
	
	
	/**
	 * 
	 */
	public AttackerToDefenseAreaDistanceDetector()
	{
		super(Arrays.asList(
				EGameStateNeutral.INDIRECT_KICK_BLUE, EGameStateNeutral.INDIRECT_KICK_YELLOW,
				EGameStateNeutral.DIRECT_KICK_BLUE, EGameStateNeutral.DIRECT_KICK_YELLOW,
				EGameStateNeutral.KICKOFF_BLUE, EGameStateNeutral.KICKOFF_YELLOW,
				EGameStateNeutral.RUNNING));
	}
	
	
	@Override
	public int getPriority()
	{
		return priority;
	}
	
	
	@Override
	public Optional<IRuleViolation> update(final IAutoRefFrame frame, final List<IRuleViolation> violations)
	{
		EGameStateNeutral lastFrameState = frame.getPreviousFrame().getGameState();
		boolean isRunning = frame.getGameState() == EGameStateNeutral.RUNNING;
		boolean lastFrameNotRunning = lastFrameState != EGameStateNeutral.RUNNING;
		
		if (isRunning && lastFrameNotRunning)
		{
			ETeamColor attackerColor = lastFrameState.getTeamColor();
			PenaltyArea penArea = NGeometry.getPenaltyArea(attackerColor.opposite());
			
			Collection<ITrackedBot> bots = frame.getWorldFrame().getBots().values();
			List<ITrackedBot> attackingBots = bots.stream()
					.filter(bot -> bot.getBotId().getTeamColor() == attackerColor)
					.collect(Collectors.toList());
			
			Optional<BotID> optOffender = attackingBots.stream()
					.filter(bot -> penArea.isPointInShape(bot.getPos(), MIN_ATTACKER_DEFENSE_DISTANCE))
					.map(bot -> bot.getBotId())
					.findFirst();
			
			if (optOffender.isPresent())
			{
				BotID offender = optOffender.get();
				IVector2 kickPos = AutoRefMath.getClosestFreekickPos(frame.getWorldFrame().getBall().getPos(),
						offender.getTeamColor().opposite());
				FollowUpAction followUp = new FollowUpAction(EActionType.INDIRECT_FREE, attackerColor.opposite(), kickPos);
				RuleViolation violation = new RuleViolation(ERuleViolation.ATTACKER_TO_DEFENCE_AREA, frame.getTimestamp(),
						offender, followUp);
				return Optional.of(violation);
			}
		}
		return Optional.empty();
	}
	
	
	@Override
	public void reset()
	{
	}
	
}
