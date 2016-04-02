/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 16, 2016
 * Author(s): Lukas Magel
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.violations.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.github.g3force.configurable.Configurable;

import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.AutoRefMath;
import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.FollowUpAction.EActionType;
import edu.tigers.autoreferee.engine.NGeometry;
import edu.tigers.autoreferee.engine.calc.BotPosition;
import edu.tigers.autoreferee.engine.violations.IRuleViolation;
import edu.tigers.autoreferee.engine.violations.IRuleViolation.ERuleViolation;
import edu.tigers.autoreferee.engine.violations.RuleViolation;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.referee.TeamConfig;
import edu.tigers.sumatra.wp.data.EGameStateNeutral;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.PenaltyArea;


/**
 * This rule detects attackers/defenders that touch the ball while inside the defense area of the defending/their own
 * team.
 * 
 * @author Lukas Magel
 */
public class BotInDefenseAreaDetector extends APreparingViolationDetector
{
	private static final int			priority				= 1;
	
	@Configurable(comment = "The cooldown time before registering a ball touch with the same bot again in ms")
	private static int					COOLDOWN_TIME_MS	= 3_000;
	
	@Configurable(comment = "If true: Attacker is considered to be violating if it touches the penalty area")
	private static boolean				attackerStrictMode			= false;
	
	private long							entryTime			= 0;
	private Map<BotID, BotPosition>	lastViolators		= new HashMap<>();
	
	static
	{
		AViolationDetector.registerClass(BotInDefenseAreaDetector.class);
	}
	
	
	/**
	 * 
	 */
	public BotInDefenseAreaDetector()
	{
		super(EGameStateNeutral.RUNNING);
	}
	
	
	@Override
	public int getPriority()
	{
		return priority;
	}
	
	
	@Override
	protected void prepare(final IAutoRefFrame frame)
	{
		entryTime = frame.getTimestamp();
	}
	
	
	@Override
	public Optional<IRuleViolation> doUpdate(final IAutoRefFrame frame, final List<IRuleViolation> violations)
	{
		BotPosition curKicker = frame.getBotLastTouchedBall();
		
		if (curKicker.getTs() < entryTime)
		{
			/*
			 * The ball was last touched before the game state changed to RUNNING
			 */
			return Optional.empty();
		}
		if ((curKicker == null) || curKicker.getId().isUninitializedID())
		{
			return Optional.empty();
		}
		
		BotID curKickerId = curKicker.getId();
		BotPosition lastViolationOfCurKicker = lastViolators.get(curKickerId);
		
		if (lastViolationOfCurKicker != null)
		{
			if (curKicker.getTs() == lastViolationOfCurKicker.getTs())
			{
				// The offense has already been reported
				return Optional.empty();
			}
			
			if (curKicker.getId().equals(lastViolationOfCurKicker.getId()))
			{
				// Wait a certain amount of time before reporting the offense again for the same bot
				long timeDiff = curKicker.getTs() - lastViolationOfCurKicker.getTs();
				if (timeDiff < TimeUnit.MILLISECONDS.toNanos(COOLDOWN_TIME_MS))
				{
					return Optional.empty();
				}
			}
		}
		
		Set<BotID> keeper = getKeeper();
		if (keeper.contains(curKickerId))
		{
			return Optional.empty();
		}
		
		IVector2 curKickerPos = curKicker.getPos();
		ETeamColor curKickerColor = curKicker.getId().getTeamColor();
		
		PenaltyArea opponentPenArea = NGeometry.getPenaltyArea(curKickerColor.opposite());
		PenaltyArea ownPenArea = NGeometry.getPenaltyArea(curKickerColor);
		
		if (opponentPenArea.isPointInShape(curKickerPos, getAttackerPenaltyAreaMargin()))
		{
			/*
			 * Attacker touched the ball while being located partially/fully inside the opponent's penalty area
			 */
			lastViolators.put(curKickerId, curKicker);
			
			FollowUpAction followUp = new FollowUpAction(EActionType.INDIRECT_FREE, curKickerColor.opposite(),
					AutoRefMath.getClosestFreekickPos(curKickerPos, curKickerColor.opposite()));
			
			RuleViolation violation = new RuleViolation(ERuleViolation.ATTACKER_IN_DEFENSE_AREA, frame.getTimestamp(),
					curKickerId, followUp);
			
			return Optional.of(violation);
		} else if (ownPenArea.isPointInShape(curKickerPos, -Geometry.getBotRadius()))
		{
			/*
			 * Multiple Defender:
			 * Defender touched the ball while being located entirely inside the own defense area
			 */
			lastViolators.put(curKickerId, curKicker);
			
			FollowUpAction followUp = new FollowUpAction(EActionType.PENALTY, curKickerColor.opposite(),
					NGeometry.getPenaltyMark(curKickerColor));
			RuleViolation violation = new RuleViolation(ERuleViolation.MULTIPLE_DEFENDER, frame.getTimestamp(),
					curKickerId, followUp);
			return Optional.of(violation);
		}
		
		return Optional.empty();
	}
	
	
	private double getAttackerPenaltyAreaMargin()
	{
		if (attackerStrictMode)
		{
			return Geometry.getBotRadius() - Geometry.getBallRadius();
		}
		return 0;
	}
	
	
	private Set<BotID> getKeeper()
	{
		return new HashSet<>(Arrays.asList(TeamConfig.getKeeperBotIDBlue(), TeamConfig.getKeeperBotIDYellow()));
	}
	
	
	@Override
	public void doReset()
	{
		lastViolators.clear();
	}
	
}
