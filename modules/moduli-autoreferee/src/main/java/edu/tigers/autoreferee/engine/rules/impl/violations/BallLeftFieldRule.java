/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 7, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.rules.impl.violations;

import java.util.Optional;

import com.github.g3force.configurable.Configurable;

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
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.wp.data.EGameStateNeutral;
import edu.tigers.sumatra.wp.data.Geometry;


/**
 * This rule initiates throw-ins/goalkicks/corner kicks when the ball leaves the field. This rule also handles icing.
 * 
 * @author "Lukas Magel"
 */
public class BallLeftFieldRule extends AGameRule
{
	private static final int	priority				= 1;
	
	@Configurable(comment = "The goal line threshold in mm")
	private static double		goalLineThreshold	= 1;
	
	private boolean				ballOutsideField	= false;
	
	static
	{
		AGameRule.registerClass(BallLeftFieldRule.class);
	}
	
	
	/**
	 * 
	 */
	public BallLeftFieldRule()
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
		IVector2 ballPos = frame.getWorldFrame().getBall().getPos();
		if (Geometry.getField().isPointInShape(ballPos) || NGeometry.ballInsideGoal(ballPos))
		{
			// The ball is inside the field or inside the goal
			ballOutsideField = false;
			return Optional.empty();
		}
		
		if (ballOutsideField == false)
		{
			ballOutsideField = true;
			IVector2 intersection = frame.getBallLeftFieldPos();
			BotPosition lastTouched = frame.getBotLastTouchedBall();
			long ts = frame.getTimestamp();
			if ((Math.abs((Geometry.getFieldLength() / 2) - intersection.x()) < goalLineThreshold)
					&& (Math.abs((Geometry.getFieldWidth() / 2) - intersection.y()) > goalLineThreshold))
			{
				// The ball exited the field over the goal line
				return handleGoalLineOff(intersection, lastTouched, ts);
			}
			return handleSideLineOff(intersection, lastTouched, ts);
		}
		return Optional.empty();
	}
	
	
	private Optional<RuleResult> handleSideLineOff(final IVector2 ballPos,
			final BotPosition lastTouched, final long ts)
	{
		ETeamColor lastTouchedColor = lastTouched.getId().getTeamColor();
		int ySide = ballPos.y() > 0 ? 1 : -1;
		IVector2 kickPos = ballPos.addNew(new Vector2(0, -100 * ySide));
		return Optional.of(buildThrowInResult(lastTouchedColor, kickPos, ts));
	}
	
	
	protected Optional<RuleResult> handleGoalLineOff(final IVector2 intersection,
			final BotPosition lastTouched, final long ts)
	{
		ETeamColor lastTouchedColor = lastTouched.getId().getTeamColor();
		
		if (isIcing(lastTouched, intersection))
		{
			return Optional.of(buildIcingResult(lastTouchedColor, lastTouched.getPos(), ts));
		}
		
		ETeamColor goalLineColor = NGeometry.getTeamOfClosestGoalLine(intersection);
		
		if (lastTouchedColor == goalLineColor)
		{
			// This is a corner kick
			IVector2 kickPos = AutoRefMath.getClosestCornerKickPos(intersection);
			return Optional.of(buildCornerKickResult(lastTouchedColor, kickPos, ts));
		}
		// This is a goal kick
		IVector2 kickPos = AutoRefMath.getClosestGoalKickPos(intersection);
		
		return Optional.of(buildGoalKickResult(lastTouchedColor, kickPos, ts));
	}
	
	
	private boolean isIcing(final BotPosition lastTouched, final IVector2 intersection)
	{
		ETeamColor colorOfGoalLine = NGeometry.getTeamOfClosestGoalLine(intersection);
		ETeamColor kickerColor = lastTouched.getId().getTeamColor();
		
		boolean kickerWasInHisHalf = NGeometry.getFieldSide(kickerColor).isPointInShape(lastTouched.getPos());
		boolean crossedOppositeGoalLine = kickerColor != colorOfGoalLine;
		if (kickerWasInHisHalf && crossedOppositeGoalLine)
		{
			return true;
		}
		return false;
	}
	
	
	private RuleResult buildCornerKickResult(final ETeamColor lastTouched, final IVector2 kickPos, final long ts)
	{
		Command cmd = Command.STOP;
		FollowUpAction action = new FollowUpAction(EActionType.DIRECT_FREE, lastTouched.opposite(), kickPos);
		RuleViolation violation = new RuleViolation(ERuleViolation.BALL_LEFT_FIELD, ts, lastTouched);
		return new RuleResult(cmd, action, violation);
	}
	
	
	private RuleResult buildGoalKickResult(final ETeamColor lastTouched, final IVector2 kickPos, final long ts)
	{
		Command cmd = Command.STOP;
		FollowUpAction action = new FollowUpAction(EActionType.DIRECT_FREE, lastTouched.opposite(), kickPos);
		RuleViolation violation = new RuleViolation(ERuleViolation.BALL_LEFT_FIELD, ts, lastTouched);
		return new RuleResult(cmd, action, violation);
	}
	
	
	private RuleResult buildIcingResult(final ETeamColor lastTouched, final IVector2 kickPos, final long ts)
	{
		Command cmd = Command.STOP;
		FollowUpAction action = new FollowUpAction(EActionType.INDIRECT_FREE, lastTouched.opposite(), kickPos);
		RuleViolation violation = new RuleViolation(ERuleViolation.NO_TOUCH_GOAL_LINE, ts, lastTouched);
		return new RuleResult(cmd, action, violation);
	}
	
	
	private RuleResult buildThrowInResult(final ETeamColor lastTouched, final IVector2 kickPos, final long ts)
	{
		Command cmd = Command.STOP;
		FollowUpAction action = new FollowUpAction(EActionType.INDIRECT_FREE, lastTouched
				.opposite(), kickPos);
		RuleViolation violation = new RuleViolation(ERuleViolation.BALL_LEFT_FIELD, ts, lastTouched);
		return new RuleResult(cmd, action, violation);
	}
	
	
	@Override
	public void reset()
	{
		ballOutsideField = false;
	}
}
