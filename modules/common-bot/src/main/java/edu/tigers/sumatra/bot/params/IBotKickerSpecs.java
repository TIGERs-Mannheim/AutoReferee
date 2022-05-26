/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.bot.params;


/**
 * Specs for the robot kicker-dribbler unit.
 */
public interface IBotKickerSpecs
{
	/**
	 * @return the chipAngle
	 */
	double getChipAngle();


	/**
	 * @return the maxAbsoluteChipVelocity
	 */
	double getMaxAbsoluteChipVelocity();


	/**
	 * @return the maxAbsoluteStraightVelocity
	 */
	double getMaxAbsoluteStraightVelocity();

	/**
	 * @return max dribble speed to apply on robot
	 */
	double getMaxDribbleSpeed();

	/**
	 * @return gain factor to apply to dribble speed from skill
	 */
	double getDribbleSpeedGain();

	/**
	 * @return Maximum acceleration the dribbler can put on the ball.
	 */
	double getMaxDribbleBallAcceleration();

	/**
	 * @return Angle from robot negative Y axis (aka back) where a ball can be kept at dribbler [rad].
	 */
	double getMaxRetainingBallAngle();
}