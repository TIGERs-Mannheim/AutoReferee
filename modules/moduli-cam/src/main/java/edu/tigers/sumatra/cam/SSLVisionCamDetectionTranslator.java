/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.07.2010
 * Author(s):
 * Lukas
 * Clemens
 * Gero
 * *********************************************************
 */
package edu.tigers.sumatra.cam;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.MessagesRobocupSslDetection.SSL_DetectionBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.MessagesRobocupSslDetection.SSL_DetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.MessagesRobocupSslDetection.SSL_DetectionRobot;
import edu.tigers.sumatra.cam.data.CamBall;
import edu.tigers.sumatra.cam.data.CamDetectionFrame;
import edu.tigers.sumatra.cam.data.CamRobot;
import edu.tigers.sumatra.export.CSVExporter;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;


/**
 * Provides a static conversion-method for the {@link SSL_DetectionFrame} to wrap the incoming SSL-Vision formats with
 * our own, internal representations
 * 
 * @author Lukas, Clemens, Gero
 */
public class SSLVisionCamDetectionTranslator
{
	@SuppressWarnings("unused")
	private static final Logger	log				= Logger.getLogger(SSLVisionCamDetectionTranslator.class
																		.getName());
																		
	private static final int		BUFFER_SIZE		= 30;
																
	private long						offset			= 0;
	private final Queue<Long>		offsetBuffer	= new CircularFifoQueue<>(BUFFER_SIZE);
	private final Queue<Long>		diffBuffer		= new CircularFifoQueue<>(BUFFER_SIZE);
																
																
	private CSVExporter				exp				= null;
																
																
	// private CSVExporter exp = new CSVExporter("logs/visiontime",true);
	
	
	private long convertVision2LocalTime(final double visionS, final long offset)
	{
		return ((long) (visionS * 1e9)) - offset;
	}
	
	
	private long calcOffset(final long tNow, final double tVision)
	{
		return (long) (tVision * 1e9) - tNow;
	}
	
	
	private double average(final Collection<Long> deque)
	{
		int size = deque.size();
		double avg = 0;
		for (Long l : deque)
		{
			avg += (double) l / size;
		}
		return avg;
	}
	
	
	/**
	 * Static Method for translating.
	 * <p>
	 * By convention, we are always playing from right to left. So this method has to turn everything around to guarantee
	 * that
	 * </p>
	 * 
	 * @param detectionFrame
	 * @return
	 */
	public CamDetectionFrame translate(final SSL_DetectionFrame detectionFrame)
	{
		long tNow = System.nanoTime();
		long localSentNs = convertVision2LocalTime(detectionFrame.getTSent(), offset);
		long diff = tNow - localSentNs;
		
		diffBuffer.add(diff);
		
		double avgDiff = Math.abs(average(diffBuffer));
		if (exp != null)
		{
			exp.addValues(tNow, localSentNs, diff, avgDiff);
		}
		
		if ((avgDiff > 3e8) || !offsetBuffer.isEmpty())
		{
			offsetBuffer.add(calcOffset(tNow, detectionFrame.getTSent()));
			offset = (long) average(offsetBuffer);
			if (avgDiff < 100_000)
			{
				log.debug("Synced with Vision clock. offset=" + offset + " diff=" + avgDiff);
				offsetBuffer.clear();
			}
			
			localSentNs = convertVision2LocalTime(detectionFrame.getTSent(), offset);
		}
		
		long localCaptureNs = convertVision2LocalTime(detectionFrame.getTCapture(), offset);
		
		final List<CamBall> balls = new ArrayList<CamBall>();
		final List<CamRobot> blues = new ArrayList<CamRobot>();
		final List<CamRobot> yellows = new ArrayList<CamRobot>();
		
		// --- if we play from left to right, turn ball and robots, so that we're always playing from right to left ---
		// --- process team Blue ---
		for (final SSL_DetectionRobot bot : detectionFrame.getRobotsBlueList())
		{
			blues.add(convertRobot(bot, ETeamColor.BLUE, detectionFrame.getFrameNumber(), detectionFrame.getCameraId(),
					localCaptureNs, localSentNs));
		}
		
		// --- process team Yellow ---
		for (final SSL_DetectionRobot bot : detectionFrame.getRobotsYellowList())
		{
			yellows.add(convertRobot(bot, ETeamColor.YELLOW, detectionFrame.getFrameNumber(),
					detectionFrame.getCameraId(),
					localCaptureNs, localSentNs));
		}
		
		// --- process ball ---
		for (final SSL_DetectionBall ball : detectionFrame.getBallsList())
		{
			balls.add(convertBall(ball, localCaptureNs, localSentNs, detectionFrame.getCameraId(),
					detectionFrame.getFrameNumber()));
		}
		
		CamDetectionFrame frame = new CamDetectionFrame(localCaptureNs, localSentNs,
				detectionFrame.getCameraId(),
				detectionFrame.getFrameNumber(), balls, yellows, blues);
		return frame;
	}
	
	
	private static CamRobot convertRobot(
			final SSL_DetectionRobot bot,
			final ETeamColor color,
			final long frameId,
			final int camId,
			final long tCapture,
			final long tSent)
	{
		double orientation = bot.getOrientation();
		double x = bot.getX();
		double y = bot.getY();
		BotID botId = BotID.createBotId(bot.getRobotId(), color);
		return new CamRobot(bot.getConfidence(), bot.getPixelX(), bot.getPixelY(), tCapture, tSent, camId, frameId, x, y,
				orientation, bot.getHeight(), botId);
	}
	
	
	private static CamBall convertBall(final SSL_DetectionBall ball, final long tCapture, final long tSent,
			final int camId,
			final long frameId)
	{
		double x;
		double y;
		x = ball.getX();
		y = ball.getY();
		
		return new CamBall(ball.getConfidence(),
				ball.getArea(),
				x, y,
				ball.getZ(),
				ball.getPixelX(), ball.getPixelY(),
				tCapture,
				tSent,
				camId,
				frameId);
	}
}
