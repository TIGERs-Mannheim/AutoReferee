/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.clock;

/**
 * Simple FPS Counter
 */
public class FpsCounter
{
	private static final int UPDATE_FREQ = 10;
	private long lastTime = 0;
	private double fps = 0;
	private int counter = 0;


	/**
	 * Signal for new frame. Call this each time, a new frame comes in
	 *
	 * @param timestamp
	 * @return
	 */
	public boolean newFrame(final long timestamp)
	{
		boolean fpsChanged = false;
		if (counter >= UPDATE_FREQ)
		{
			long timeDiff = timestamp - lastTime;
			if (timeDiff != 0)
			{
				double newFps = UPDATE_FREQ / (timeDiff / 1e9);
				fpsChanged = Math.abs(fps - newFps) > 0.01;
				fps = newFps;
				lastTime = timestamp;
				counter = 0;
			}
		}
		counter++;
		return fpsChanged;
	}


	/**
	 * Reset counter
	 */
	public void reset()
	{
		counter = 0;
		lastTime = 0;
		fps = 0;
	}


	/**
	 * Returns the average fps
	 *
	 * @return
	 */
	public double getAvgFps()
	{
		return fps;
	}
}
