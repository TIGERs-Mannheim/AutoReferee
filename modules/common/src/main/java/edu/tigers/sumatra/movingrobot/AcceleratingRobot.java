/*
 * Copyright (c) 2009 - 2024, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.movingrobot;

/**
 * A moving robot that is accelerating to the maximum velocity and keeps driving at this velocity.
 */
public class AcceleratingRobot extends SlowingDownRobot
{
	protected AcceleratingRobot(MovingRobotParams params)
	{
		super(params, params.vLimit());
	}
}
