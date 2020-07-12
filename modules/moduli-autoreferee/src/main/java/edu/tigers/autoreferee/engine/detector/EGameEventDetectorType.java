/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.detector;

import com.github.g3force.instanceables.IInstanceableEnum;
import com.github.g3force.instanceables.InstanceableClass;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * All sorted game event detectors
 */
public enum EGameEventDetectorType implements IInstanceableEnum
{
	ATTACKER_TO_DEFENSE_AREA_DISTANCE(new InstanceableClass(AttackerToDefenseAreaDistanceDetector.class)),
	BOT_IN_DEFENSE_AREA(new InstanceableClass(BotInDefenseAreaDetector.class)),
	DRIBBLING(new InstanceableClass(DribblingDetector.class)),

	BALL_SPEEDING(new InstanceableClass(BallSpeedingDetector.class)),
	BOT_COLLISION(new InstanceableClass(BotCollisionDetector.class)),

	DEFENDER_TO_KICK_POINT_DISTANCE(new InstanceableClass(DefenderToKickPointDistanceDetector.class)),
	BOT_STOP_SPEED(new InstanceableClass(BotStopSpeedDetector.class)),
	BALL_PLACEMENT_INTERFERENCE(new InstanceableClass(BallPlacementInterferenceDetector.class)),

	GOAL(new InstanceableClass(GoalDetector.class)),

	DOUBLE_TOUCH(new InstanceableClass(DoubleTouchDetector.class)),
	BALL_PLACEMENT_SUCCEEDED(new InstanceableClass(BallPlacementSucceededDetector.class)),

	BALL_LEFT_FIELD(new InstanceableClass(BallLeftFieldDetector.class)),
	BOUNDARY_CROSSING(new InstanceableClass(BoundaryCrossingDetector.class)),

	PUSHING(new InstanceableClass(PushingDetector.class), false),

	;

	private final InstanceableClass clazz;
	private final boolean enabledByDefault;


	EGameEventDetectorType(final InstanceableClass clazz)
	{
		this(clazz, true);
	}


	EGameEventDetectorType(final InstanceableClass clazz, final boolean enabledByDefault)
	{
		this.clazz = clazz;
		this.enabledByDefault = enabledByDefault;
	}


	@Override
	public InstanceableClass getInstanceableClass()
	{
		return clazz;
	}


	public boolean isEnabledByDefault()
	{
		return enabledByDefault;
	}


	public static Set<EGameEventDetectorType> valuesEnabledByDefault()
	{
		return Arrays.stream(values())
				.filter(EGameEventDetectorType::isEnabledByDefault)
				.collect(Collectors.toSet());
	}
}
