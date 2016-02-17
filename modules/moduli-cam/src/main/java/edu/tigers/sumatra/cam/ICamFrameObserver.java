/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 15, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.cam;

import edu.tigers.sumatra.cam.data.CamDetectionFrame;
import edu.tigers.sumatra.cam.data.CamGeometry;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface ICamFrameObserver
{
	/**
	 * @param frame
	 */
	default void onNewCameraFrame(final CamDetectionFrame frame)
	{
	}
	
	
	/**
	 * @param geometry
	 */
	default void onNewCameraGeometry(final CamGeometry geometry)
	{
	}
	
	
	/**
	 * 
	 */
	default void onClearCamFrame()
	{
	}
}
