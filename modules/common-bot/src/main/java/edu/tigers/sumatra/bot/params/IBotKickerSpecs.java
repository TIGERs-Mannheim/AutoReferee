package edu.tigers.sumatra.bot.params;


/**
 * Specs for the robot kicker unit.
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
}