/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.clock;

import org.junit.Test;

import java.util.concurrent.locks.LockSupport;

import static org.junit.Assert.assertTrue;


/**
 * Tests methods in {@link ThreadUtil}
 *
 * @author Gero
 */
public class ThreadUtilTest
{
	/**
	 * Test method for {@link edu.tigers.sumatra.clock.ThreadUtil#parkNanosSafe(long)}.
	 */
	@Test
	public void testParkNanosSafeLong()
	{
		final long sleepFor = 50;

		for (int i = 0; i < 10; i++)
		{
			final long start = System.nanoTime();
			ThreadUtil.parkNanosSafe(sleepFor);
			final long stop = System.nanoTime();

			final long duration = stop - start;
			assertTrue("Not slept enough: " + duration + "ns < " + sleepFor + "ns!!!", duration > sleepFor);
		}
	}


	private static void measureParkNanos()
	{
		long iterationCount = 100;
		long[] sleepTimes = new long[] { 0, 1, 10_000, 100_000, 500_000, 1_000_000, 10_000_000 };

		for (long sleepTime : sleepTimes)
		{
			long startNanos = System.nanoTime();
			for (long i = 0; i < iterationCount; i++)
			{
				LockSupport.parkNanos(sleepTime);
			}
			long durationNanos = System.nanoTime() - startNanos;
			long microsPerIteration = durationNanos / iterationCount;
			long diff = (microsPerIteration - sleepTime);
			double rel = (double) diff / sleepTime;

			System.out.printf("%8d | %8d | %8d | %.1f %n", sleepTime, microsPerIteration, diff, rel * 100);
		}
	}


	public static void main(String[] args)
	{
		measureParkNanos();
	}
}
