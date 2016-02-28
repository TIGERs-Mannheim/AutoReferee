/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 16.05.2010
 * Authors:
 * Maren K�nemund <Orphen@fantasymail.de>,
 * Peter Birkenkampf <birkenkampf@web.de>,
 * Marcel Sauer <sauermarcel@yahoo.de>,
 * Gero
 * *********************************************************
 */
package edu.tigers.sumatra.wp.kalman;

import edu.tigers.sumatra.cam.data.CamBall;
import edu.tigers.sumatra.wp.data.ExtendedCamDetectionFrame;
import edu.tigers.sumatra.wp.kalman.data.PredictionContext;
import edu.tigers.sumatra.wp.kalman.data.WPCamBall;


/**
 * Prepares all new data from the incoming
 * {@link edu.tigers.sumatra.cam.data.CamDetectionFrame} concerning the ball(s), and add it
 * to the {@link PredictionContext} if necessary
 */
public class BallProcessor
{
	private final PredictionContext context;
	
	
	/**
	 * @param context
	 */
	public BallProcessor(final PredictionContext context)
	{
		this.context = context;
	}
	
	
	/**
	 * @param frame
	 */
	public void process(final ExtendedCamDetectionFrame frame)
	{
		CamBall ball = frame.getBall();
		final WPCamBall visionBall = new WPCamBall(ball);
		context.getBall().observation(ball.getTimestamp(), visionBall);
	}
}
