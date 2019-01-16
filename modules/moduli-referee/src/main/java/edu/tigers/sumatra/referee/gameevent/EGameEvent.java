/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.referee.gameevent;

import java.util.EnumMap;
import java.util.Map;

import com.github.g3force.instanceables.IInstanceableEnum;
import com.github.g3force.instanceables.InstanceableClass;
import com.github.g3force.instanceables.InstanceableParameter;

import edu.tigers.sumatra.SslGameEvent;
import edu.tigers.sumatra.SslGameEvent.GameEventType;


/**
 * AutoRef game events and mapping to human readable texts and referee protocol enum
 */
public enum EGameEvent implements IInstanceableEnum
{
	// Match Proceeding
	PREPARED(GameEventType.PREPARED, Prepared.class),
	NO_PROGRESS_IN_GAME(GameEventType.NO_PROGRESS_IN_GAME, NoProgressInGame.class),
	PLACEMENT_FAILED(GameEventType.PLACEMENT_FAILED, PlacementFailed.class),
	PLACEMENT_SUCCEEDED(GameEventType.PLACEMENT_SUCCEEDED, PlacementSucceeded.class),
	BOT_SUBSTITUTION(GameEventType.BOT_SUBSTITUTION, BotSubstitution.class),
	TOO_MANY_ROBOTS(GameEventType.TOO_MANY_ROBOTS, TooManyRobots.class),
	
	
	// Ball out of field
	BALL_LEFT_FIELD_GOAL_LINE(GameEventType.BALL_LEFT_FIELD_GOAL_LINE, BallLeftFieldGoalLine.class),
	BALL_LEFT_FIELD_TOUCH_LINE(GameEventType.BALL_LEFT_FIELD_TOUCH_LINE, BallLeftFieldTouchLine.class),
	GOAL(GameEventType.GOAL, Goal.class),
	POSSIBLE_GOAL(GameEventType.POSSIBLE_GOAL, PossibleGoal.class),
	INDIRECT_GOAL(GameEventType.INDIRECT_GOAL, IndirectGoal.class),
	CHIP_ON_GOAL(GameEventType.CHIPPED_GOAL, ChippedGoal.class),
	
	
	// Minor offense
	AIMLESS_KICK(GameEventType.AIMLESS_KICK, AimlessKick.class),
	KICK_TIMEOUT(GameEventType.KICK_TIMEOUT, KickTimeout.class),
	KEEPER_HELD_BALL(GameEventType.KEEPER_HELD_BALL, KeeperHeldBall.class),
	ATTACKER_DOUBLE_TOUCHED_BALL(GameEventType.ATTACKER_DOUBLE_TOUCHED_BALL, AttackerDoubleTouchedBall.class),
	ATTACKER_IN_DEFENSE_AREA(GameEventType.ATTACKER_IN_DEFENSE_AREA, AttackerInDefenseArea.class),
	ATTACKER_TOUCH_KEEPER(GameEventType.ATTACKER_TOUCHED_KEEPER, AttackerTouchedKeeper.class),
	BOT_DRIBBLED_BALL_TOO_FAR(GameEventType.BOT_DRIBBLED_BALL_TOO_FAR, BotDribbledBallTooFar.class),
	BOT_KICKED_BALL_TOO_FAST(GameEventType.BOT_KICKED_BALL_TOO_FAST, BotKickedBallToFast.class),
	
	
	// Fouls
	ATTACKER_TOO_CLOSE_TO_DEFENSE_AREA(GameEventType.ATTACKER_TOO_CLOSE_TO_DEFENSE_AREA,
			AttackerTooCloseToDefenseArea.class),
	BOT_INTERFERED_PLACEMENT(GameEventType.BOT_INTERFERED_PLACEMENT, BotInterferedPlacement.class),
	BOT_CRASH_DRAWN(GameEventType.BOT_CRASH_DRAWN, BotCrashDrawn.class),
	BOT_CRASH_UNIQUE(GameEventType.BOT_CRASH_UNIQUE, BotCrashUnique.class),
	BOT_CRASH_UNIQUE_SKIPPED(GameEventType.BOT_CRASH_UNIQUE_SKIPPED, BotCrashUnique.class),
	BOT_PUSHED_BOT(GameEventType.BOT_PUSHED_BOT, BotPushedBot.class),
	BOT_PUSHED_BOT_SKIPPED(GameEventType.BOT_PUSHED_BOT_SKIPPED, BotPushedBot.class),
	BOT_HELD_BALL_DELIBERATELY(GameEventType.BOT_HELD_BALL_DELIBERATELY, BotHeldBallDeliberately.class),
	BOT_TIPPED_OVER(GameEventType.BOT_TIPPED_OVER, BotTippedOver.class),
	BOT_TOO_FAST_IN_STOP(GameEventType.BOT_TOO_FAST_IN_STOP, BotTooFastInStop.class),
	DEFENDER_TOO_CLOSE_TO_KICK_POINT(GameEventType.DEFENDER_TOO_CLOSE_TO_KICK_POINT, DefenderTooCloseToKickPoint.class),
	DEFENDER_IN_DEFENSE_AREA_PARTIALLY(GameEventType.DEFENDER_IN_DEFENSE_AREA_PARTIALLY,
			DefenderInDefenseAreaPartially.class),
	DEFENDER_IN_DEFENSE_AREA(GameEventType.DEFENDER_IN_DEFENSE_AREA, DefenderInDefenseArea.class),
	
	
	// Repeated
	MULTIPLE_CARDS(GameEventType.MULTIPLE_CARDS, MultipleCards.class),
	MULTIPLE_PLACEMENT_FAILURES(GameEventType.MULTIPLE_PLACEMENT_FAILURES, MultiplePlacementFailures.class),
	MULTIPLE_FOULS(GameEventType.MULTIPLE_FOULS, MultipleFouls.class),
	
	// Unsporting behaviors
	UNSPORTING_BEHAVIOR_MINOR(GameEventType.UNSPORTING_BEHAVIOR_MINOR, UnsportingBehaviorMinor.class),
	UNSPORTING_BEHAVIOR_MAJOR(GameEventType.UNSPORTING_BEHAVIOR_MAJOR, UnsportingBehaviorMajor.class)
	
	
	;
	
	
	private final InstanceableClass impl;
	private final GameEventType protoType;
	
	
	private static final Map<GameEventType, EGameEvent> EVENT_TYPE_MAP = new EnumMap<>(
			SslGameEvent.GameEventType.class);
	
	
	EGameEvent(final GameEventType protoType, final Class<? extends IGameEvent> wrapperImpl)
	{
		this.impl = new InstanceableClass(wrapperImpl, new InstanceableParameter(SslGameEvent.GameEvent.class, "", null));
		this.protoType = protoType;
	}
	
	
	@Override
	public InstanceableClass getInstanceableClass()
	{
		return impl;
	}
	
	
	public GameEventType getProtoType()
	{
		return protoType;
	}
	
	
	public static EGameEvent fromProto(GameEventType type)
	{
		return EVENT_TYPE_MAP.computeIfAbsent(type, e -> searchProto(type));
	}
	
	
	private static EGameEvent searchProto(GameEventType type)
	{
		for (EGameEvent event : values())
		{
			if (event.getProtoType() == type)
			{
				return event;
			}
		}
		throw new IllegalArgumentException("Could not map type: " + type);
	}
}
