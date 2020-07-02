/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.visualizer;

@FunctionalInterface
public interface IScreenshotListener
{
	void takeScreenshot(final EScreenshotOption screenshotOption, final int w, final int h);
}
