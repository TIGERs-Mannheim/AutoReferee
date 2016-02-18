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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import edu.tigers.sumatra.cam.data.CamBall;
import edu.tigers.sumatra.cam.data.CamDetectionFrame;
import edu.tigers.sumatra.cam.data.CamRobot;
import edu.tigers.sumatra.export.CSVExporter;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.wp.ExportDataContainer;
import edu.tigers.sumatra.wp.ExportDataContainer.WpBall;
import edu.tigers.sumatra.wp.data.ExtendedCamDetectionFrame;
import edu.tigers.sumatra.wp.data.TrackedBall;
import edu.tigers.sumatra.wp.kalman.data.BallMotionResult;
import edu.tigers.sumatra.wp.kalman.data.PredictionContext;
import edu.tigers.sumatra.wp.kalman.data.WPCamBall;
import edu.tigers.sumatra.wp.kalman.data.WPCamBot;
import edu.tigers.sumatra.wp.kalman.filter.ExtKalmanFilter;
import edu.tigers.sumatra.wp.kalman.filter.IFilter;
import edu.tigers.sumatra.wp.kalman.motionModels.BallMotionModel;
import edu.tigers.sumatra.wp.kalman.motionModels.TigersMotionModel;


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
		if (context.getBall() == null)
		{
			context.setBall(new ExtKalmanFilter());
			context.getBall().init(new BallMotionModel(), context, frame.gettCapture(), new WPCamBall(new CamBall()));
		}
		context.getBall().observation(ball.getTimestamp(), visionBall);
	}
	
	
	/**
	 */
	public void performCollisionAwareLookahead()
	{
		final IFilter ballWeKnow = context.getBall();
		for (int i = 1; i <= context.getStepCount(); i++)
		{
			ballWeKnow.performLookahead(i);
		}
	}
	
	
	/**
	 * @param folder
	 */
	public static void runOnData(final String folder)
	{
		List<CamBall> allRawBalls = ExportDataContainer.readRawBall(folder, "rawBalls");
		List<CamBall> rawBalls = ExportDataContainer.readRawBall(folder, "rawBall");
		List<CamRobot> yellowBots = ExportDataContainer.readRawBots(folder, "rawBots", ETeamColor.YELLOW);
		List<CamRobot> blueBots = ExportDataContainer.readRawBots(folder, "rawBots", ETeamColor.BLUE);
		
		PredictionContext context = new PredictionContext();
		ExtKalmanFilter filter = new ExtKalmanFilter();
		filter.init(new BallMotionModel(), context, rawBalls.get(0).getTimestamp(), new WPCamBall(rawBalls.get(0)));
		context.setBall(filter);
		
		BallProcessor ballProcessor = new BallProcessor(context);
		
		List<WpBall> wpBalls = new ArrayList<>();
		
		for (int i = 0; i < rawBalls.size(); i++)
		{
			CamBall rawBall = rawBalls.get(i);
			long timestamp = rawBall.getTimestamp();
			
			List<CamBall> frameBalls = allRawBalls.stream().filter(b -> b.getFrameId() == rawBall.getFrameId())
					.map(rb -> rb).collect(Collectors.toList());
			List<CamRobot> frameBotsY = yellowBots.stream().filter(b -> b.getFrameId() == rawBall.getFrameId())
					.map(rb -> rb).collect(Collectors.toList());
			List<CamRobot> frameBotsB = blueBots.stream().filter(b -> b.getFrameId() == rawBall.getFrameId())
					.map(rb -> rb).collect(Collectors.toList());
					
			context.getBlueBots().clear();
			context.getYellowBots().clear();
			for (CamRobot bot : frameBotsB)
			{
				TigersMotionModel botModel = new TigersMotionModel();
				IFilter botFilter = new ExtKalmanFilter();
				botFilter.init(botModel, context, bot.gettCapture(), new WPCamBot(bot));
				context.getBlueBots().put(bot.getRobotID(), botFilter);
			}
			for (CamRobot bot : frameBotsY)
			{
				TigersMotionModel botModel = new TigersMotionModel();
				IFilter botFilter = new ExtKalmanFilter();
				botFilter.init(botModel, context, bot.gettCapture(), new WPCamBot(bot));
				context.getYellowBots().put(bot.getRobotID(), botFilter);
			}
			
			CamDetectionFrame frame = new CamDetectionFrame(
					timestamp, timestamp,
					rawBall.getCameraId(), i,
					frameBalls, frameBotsY, frameBotsB);
					
			ExtendedCamDetectionFrame eFrame = new ExtendedCamDetectionFrame(frame, rawBall);
			context.updateMotionContext();
			ballProcessor.process(eFrame);
			
			final IFilter filteredBall = context.getBall();
			final BallMotionResult motion = (BallMotionResult) filteredBall.getLookahead(context.getStepCount());
			TrackedBall trackedBall = motion.toTrackedBall();
			
			wpBalls.add(new WpBall(trackedBall.getPos3(), trackedBall.getVel3(), trackedBall.getAcc3(), i, rawBall
					.getTimestamp(), trackedBall.getConfidence()));
		}
		
		CSVExporter.exportList(folder, "wpBallTest", wpBalls.stream().map(c -> c));
	}
}
