/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.wp.data;

import edu.tigers.sumatra.cam.data.CamBall;
import edu.tigers.sumatra.cam.data.CamDetectionFrame;
import edu.tigers.sumatra.cam.data.CamRobot;

import java.util.List;


/**
 * This frame _might_ contain data from multiple camera frames
 */
public class ExtendedCamDetectionFrame extends CamDetectionFrame
{
	private final CamBall ball;


	/**
	 * @param frame
	 * @param ball
	 */
	public ExtendedCamDetectionFrame(final CamDetectionFrame frame, final CamBall ball)
	{
		super(frame);
		this.ball = ball;
	}


	/**
	 * @param frameId
	 * @param frame
	 * @param balls
	 * @param yellowBots
	 * @param blueBots
	 * @param ball
	 */
	public ExtendedCamDetectionFrame(final long frameId, final CamDetectionFrame frame,
			final List<CamBall> balls, final List<CamRobot> yellowBots, final List<CamRobot> blueBots,
			final CamBall ball)
	{
		super(frame.gettCapture(), frame.gettSent(), frame.getTCaptureCamera(), frame.getCameraId(),
				frame.getCamFrameNumber(), frameId, balls,
				yellowBots,
				blueBots);
		this.ball = ball;
	}


	/**
	 * @return the ball
	 */
	public final CamBall getBall()
	{
		return ball;
	}
}
