/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.detector;

import com.github.g3force.instanceables.IInstanceableEnum;
import com.github.g3force.instanceables.InstanceableClass;


/**
 * All sorted game event detectors
 */
public enum EGameEventDetectorType implements IInstanceableEnum
{
	KICK_TIMEOUT(new InstanceableClass(KickTimeoutDetector.class)),
	NO_PROGRESS(new InstanceableClass(NoProgressDetector.class)),
	READY_FOR_KICKOFF(new InstanceableClass(ReadyForKickoffDetector.class)),
	READY_FOR_PENALTY(new InstanceableClass(ReadyForPenaltyDetector.class)),
	BALL_PLACEMENT(new InstanceableClass(BallPlacementDetector.class)),
	ATTACKER_TO_DEFENSE_AREA_DISTANCE(new InstanceableClass(AttackerToDefenseAreaDistanceDetector.class)),
	ATTACKER_TOUCHED_OPPONENT_ROBOT(new InstanceableClass(AttackerTouchedOpponentRobotDetector.class)),
	BOT_COLLISION(new InstanceableClass(BotCollisionDetector.class)),
	BOT_IN_DEFENSE_AREA(new InstanceableClass(BotInDefenseAreaDetector.class)),
	BOT_STOP_SPEED(new InstanceableClass(BotStopSpeedDetector.class)),
	DOUBLE_TOUCH(new InstanceableClass(DoubleTouchDetector.class)),
	DRIBBLING(new InstanceableClass(DribblingDetector.class)),
	DEFENDER_TO_KICK_POINT_DISTANCE(new InstanceableClass(DefenderToKickPointDistanceDetector.class)),
	GOAL(new InstanceableClass(GoalDetector.class)),
	BALL_HOLD_IN_DEFENSE_AREA(new InstanceableClass(BallHoldInDefenseAreaDetector.class)),
	BALL_LEFT_FIELD(new InstanceableClass(BallLeftFieldDetector.class)),
	BALL_SPEEDING(new InstanceableClass(BallSpeedingDetector.class)),
	BOT_NUMBER(new InstanceableClass(BotNumberDetector.class)),
	READY_TO_CONTINUE(new InstanceableClass(ReadyToContinueDetector.class)),
	PUSHING(new InstanceableClass(PushingDetector.class)),
	BALL_PLACEMENT_INTERFERENCE(new InstanceableClass(BallPlacementInterferenceDetector.class)),

	;

	private final InstanceableClass clazz;


	EGameEventDetectorType(final InstanceableClass clazz)
	{
		this.clazz = clazz;
	}


	@Override
	public InstanceableClass getInstanceableClass()
	{
		return clazz;
	}
}
