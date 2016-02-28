/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.07.2010
 * Author(s):
 * Maren K�nemund <Orphen@fantasymail.de>,
 * Peter Birkenkampf <birkenkampf@web.de>,
 * Marcel Sauer <sauermarcel@yahoo.de>,
 * Gero
 * *********************************************************
 */
package edu.tigers.sumatra.wp.kalman;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.BotIDMap;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.ids.IBotIDMap;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.data.ExtendedCamDetectionFrame;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;
import edu.tigers.sumatra.wp.data.TrackedBall;
import edu.tigers.sumatra.wp.kalman.data.BallMotionResult;
import edu.tigers.sumatra.wp.kalman.data.PredictionContext;
import edu.tigers.sumatra.wp.kalman.data.RobotMotionResult_V2;
import edu.tigers.sumatra.wp.kalman.filter.IFilter;


/**
 * This is the extended kalman implementation of the WP
 */
public class ExtKalman extends AWorldPredictor
{
	@SuppressWarnings("unused")
	private static final Logger	log			= Logger.getLogger(ExtKalman.class.getName());
															
	private TrackingManager			trackingManager;
	private BallProcessor			ballProcessor;
	private BotProcessor				botProcessor;
											
	private PredictionContext		context;
											
	@Configurable
	private static boolean			processBots	= true;
															
															
	static
	{
		ConfigRegistration.registerClass("wp", ExtKalman.class);
	}
	
	
	/**
	 * @param config
	 */
	public ExtKalman(final SubnodeConfiguration config)
	{
		super(config);
	}
	
	
	private void processBot(final IFilter filterBot, final IBotIDMap<ITrackedBot> bots, final ETeamColor color,
			final long timestamp)
	{
		final BotID botID = BotID.createBotId(filterBot.getId(), color);
		
		ITrackedBot trackedTigerBot;
		final RobotMotionResult_V2 motion = (RobotMotionResult_V2) filterBot.getPrediction(timestamp);
		
		trackedTigerBot = motion.motionToTrackedBot(botID);
		bots.put(botID, trackedTigerBot);
	}
	
	
	/**
	 * Predict a new {@link SimpleWorldFrame}
	 * 
	 * @param frame
	 * @return
	 */
	public SimpleWorldFrame predictSimpleWorldFrame(final ExtendedCamDetectionFrame frame)
	{
		long timestamp = frame.gettCapture() + (long) (context.getPredictionLookahead() * 1e9);
		
		context.updateMotionContext(timestamp);
		processMotionContext(context.getMotionContext());
		
		ballProcessor.process(frame);
		final IFilter ball = context.getBall();
		final BallMotionResult motion = (BallMotionResult) ball.getPrediction(timestamp);
		TrackedBall trackedBall = motion.toTrackedBall();
		
		
		final BotIDMap<ITrackedBot> bots = new BotIDMap<>();
		if (processBots)
		{
			botProcessor.process(frame.getRobotsYellow(), frame.getRobotsBlue());
			for (final IFilter filterBot : context.getYellowBots().values())
			{
				processBot(filterBot, bots, ETeamColor.YELLOW, timestamp);
			}
			for (final IFilter filterBot : context.getBlueBots().values())
			{
				processBot(filterBot, bots, ETeamColor.BLUE, timestamp);
			}
			
			trackingManager.checkItems(frame.gettCapture());
		}
		
		return new SimpleWorldFrame(bots, trackedBall, frame.getFrameNumber(),
				frame.gettCapture());
	}
	
	
	@Override
	protected void processCameraDetectionFrame(final ExtendedCamDetectionFrame frame)
	{
		SimpleWorldFrame wFrame = predictSimpleWorldFrame(frame);
		pushFrame(wFrame);
	}
	
	
	@Override
	public void start()
	{
		context = new PredictionContext();
		trackingManager = new TrackingManager(context);
		ballProcessor = new BallProcessor(context);
		botProcessor = new BotProcessor(context);
		trackingManager.setFirstIteration(true);
	}
	
	
	@Override
	public void onClearCamFrame()
	{
		super.onClearCamFrame();
		start();
	}
	
	
	/**
	 * @return the context
	 */
	public PredictionContext getContext()
	{
		return context;
	}
}
