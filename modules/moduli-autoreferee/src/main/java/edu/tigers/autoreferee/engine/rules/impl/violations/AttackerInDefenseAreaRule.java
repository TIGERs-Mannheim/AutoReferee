/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 16, 2016
 * Author(s): Lukas Magel
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.rules.impl.violations;

import java.util.Optional;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Command;
import edu.tigers.autoreferee.engine.AutoRefMath;
import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.FollowUpAction.EActionType;
import edu.tigers.autoreferee.engine.IRuleEngineFrame;
import edu.tigers.autoreferee.engine.NGeometry;
import edu.tigers.autoreferee.engine.RuleViolation;
import edu.tigers.autoreferee.engine.RuleViolation.ERuleViolation;
import edu.tigers.autoreferee.engine.calc.BotPosition;
import edu.tigers.autoreferee.engine.rules.RuleResult;
import edu.tigers.autoreferee.engine.rules.impl.AGameRule;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.wp.data.EGameStateNeutral;


/**
 * This rule detects attackers that touch the ball while inside the defense area of the defending team.
 * 
 * @author Lukas Magel
 */
public class AttackerInDefenseAreaRule extends AGameRule
{
	private static final int	priority	= 1;
	
	
	/**
	 * 
	 */
	public AttackerInDefenseAreaRule()
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
		BotPosition lastKicker = frame.getBotLastTouchedBall();
		if ((lastKicker == null) || lastKicker.getId().isUninitializedID())
		{
			return Optional.empty();
		}
		
		IVector2 kickerPos = lastKicker.getPos();
		ETeamColor kickerColor = lastKicker.getId().getTeamColor();
		if (NGeometry.posInsidePenaltyArea(kickerPos))
		{
			ETeamColor penAreaColor = NGeometry.getTeamOfClosestGoalLine(kickerPos);
			if (kickerColor == penAreaColor.opposite())
			{
				FollowUpAction followUp = new FollowUpAction(EActionType.INDIRECT_FREE, kickerColor.opposite(),
						AutoRefMath.getClosestFreekickPos(kickerPos, penAreaColor));
				RuleViolation violation = new RuleViolation(ERuleViolation.ATTACKER_IN_DEFENSE_AREA, frame.getTimestamp(),
						kickerColor);
				return Optional.of(new RuleResult(Command.STOP, followUp, violation));
			}
		}
		return Optional.empty();
	}
	
	
	@Override
	public void reset()
	{
	}
	
}
