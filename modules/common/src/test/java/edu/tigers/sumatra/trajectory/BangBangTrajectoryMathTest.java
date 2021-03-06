/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.trajectory;

import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import org.junit.Assert;
import org.junit.Test;


/**
 * Test BangBangTrajectoryMath
 */
public class BangBangTrajectoryMathTest
{
	// Constants for brake functions
	private final IVector2 testVel = Vector2f.fromXY(1.0, 1.0);
	private final double testMaxBrk = 1.0;

	// Additional constants for maxvel functions
	private final IVector2 testInitialVel = Vector2f.fromXY(0.5, 0.5);
	private final double testMaxAcc = 0.5;
	private final double testMaxVel = 2.0;

	private final BangBangTrajectoryFactory trajectoryFactory = new BangBangTrajectoryFactory();


	/**
	 * Test functions concerning brake distance and -time calculations
	 */
	@Test
	public void testBrakeDistanceFunctions()
	{
		checkBrakeVector();
		checkBrakeDistance();
		checkBrakeTime();
	}


	/**
	 * Test functions concerning maximum velocity distance calculations
	 */
	@Test
	public void testDistanceToAchieveMaxVel()
	{
		checkMaxVelDistance();
		checkMaxVelVector();
	}


	private void checkMaxVelDistance()
	{
		double distance = BangBangTrajectoryMath.distanceToAchieveMaxVelocity(testInitialVel, testMaxVel, testMaxAcc);
		BangBangTrajectory2D traj = trajectoryFactory.sync(Vector2.fromXY(0, 0),
				Vector2.fromXY(1, 1).normalizeNew().multiplyNew(distance),
				testInitialVel, testMaxVel + 100, testMaxAcc); // maxvel has an offset because it should be
		// calculated properly --> that is to check...
		double resultvmax = BangBangTrajectoryMath.maxVelocityOfTrajectory(traj);
		Assert.assertEquals(testMaxVel, resultvmax, 5e-3);

	}


	private void checkMaxVelVector()
	{
		IVector2 distance = BangBangTrajectoryMath.distanceVectorToAchieveMaxVelocity(testInitialVel, testMaxVel,
				testMaxAcc);
		BangBangTrajectory2D traj = trajectoryFactory
				.sync(Vector2.fromXY(0, 0), distance, testInitialVel,
						testMaxVel + 100, testMaxAcc);
		Assert.assertEquals(testMaxVel, BangBangTrajectoryMath.maxVelocityOfTrajectory(traj), 5e-3);
	}


	private void checkBrakeVector()
	{
		IVector2 expectedDistanceVector = Vector2f.fromXY(SumatraMath.sqrt(2) / 2.0, SumatraMath.sqrt(2) / 2.0);
		IVector2 resultVector = BangBangTrajectoryMath.brakeDistanceVector(testVel, testMaxBrk);
		Assert.assertTrue(expectedDistanceVector.isCloseTo(resultVector, 1e-6));
	}


	private void checkBrakeDistance()
	{
		double expectedDistance = 1.0;
		double resultDistance = BangBangTrajectoryMath.brakeDistance(testVel, testMaxBrk);
		Assert.assertTrue(SumatraMath.isEqual(expectedDistance, resultDistance, 1e-6));
	}


	private void checkBrakeTime()
	{
		double expectedTime = SumatraMath.sqrt(2);
		double resultTime = BangBangTrajectoryMath.timeToBrake(testVel, testMaxBrk);
		Assert.assertTrue(SumatraMath.isEqual(expectedTime, resultTime, 1e-6));
	}

}
