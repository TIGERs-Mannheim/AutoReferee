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

import java.util.List;
import java.util.Map;

import edu.tigers.sumatra.cam.data.CamRobot;
import edu.tigers.sumatra.wp.kalman.data.OmnibotControl_V2;
import edu.tigers.sumatra.wp.kalman.data.PredictionContext;
import edu.tigers.sumatra.wp.kalman.data.RobotMotionResult_V2;
import edu.tigers.sumatra.wp.kalman.data.UnregisteredBot;
import edu.tigers.sumatra.wp.kalman.data.WPCamBot;
import edu.tigers.sumatra.wp.kalman.filter.IFilter;


/**
 * Prepares all new data from the incoming
 * {@link edu.tigers.sumatra.cam.data.CamDetectionFrame} concerning the bots (enemies and
 * tigers), and add
 * it to the {@link PredictionContext} if necessary
 */
public class BotProcessor
{
	
	private final PredictionContext context;
	
	
	/**
	 * @param context
	 */
	public BotProcessor(final PredictionContext context)
	{
		this.context = context;
	}
	
	
	/**
	 * @param camYellowBots
	 * @param camBlueBots
	 */
	public void process(final List<CamRobot> camYellowBots, final List<CamRobot> camBlueBots)
	{
		// ---Check oldTigers list and update each founded element
		// ---if incoming CamRobos not known, check list of newTigers
		// ---if found in newTigers refresh element
		// ---if not found add to list
		
		for (final CamRobot visionBotCam : camYellowBots)
		{
			final WPCamBot visionBot = new WPCamBot(visionBotCam);
			final int botID = visionBot.id + WPConfig.YELLOW_ID_OFFSET;
			
			final IFilter existingBot = context.getYellowBots().get(botID);
			processBot(visionBotCam, botID, existingBot, visionBotCam.getTimestamp(), context.getNewYellowBots());
		}
		
		// --- same for blue ~~~~
		for (final CamRobot visionBotCam : camBlueBots)
		{
			final WPCamBot visionBot = new WPCamBot(visionBotCam);
			final int botID = visionBot.id + WPConfig.BLUE_ID_OFFSET;
			
			final IFilter existingBot = context.getBlueBots().get(botID);
			processBot(visionBotCam, botID, existingBot, visionBotCam.getTimestamp(), context.getNewBlueBots());
		}
	}
	
	
	private void processBot(final CamRobot visionBotCam, final int botID, final IFilter existingBot,
			final long timestamp,
			final Map<Integer, UnregisteredBot> contextNewBots)
	{
		final WPCamBot visionBot = new WPCamBot(visionBotCam);
		if (existingBot != null)
		{
			// drop doubled observation if bot is in overlap area of cameras
			final double dt = (timestamp - existingBot.getTimestamp()) * WPConfig.FILTER_CONVERT_NS_TO_INTERNAL_TIME;
			if (dt <= WPConfig.MIN_CAMFRAME_DELAY_TIME)
			{
				return;
			}
			
			final RobotMotionResult_V2 oldState = (RobotMotionResult_V2) existingBot
					.getPrediction(existingBot.getTimestamp());
					
			existingBot.observation(timestamp, visionBot);
			estimateControl(oldState, existingBot, dt, timestamp);
			return;
		}
		
		UnregisteredBot newBot = contextNewBots.get(botID);
		if (newBot != null)
		{
			newBot.addBot(timestamp, visionBot);
		} else
		{
			newBot = new UnregisteredBot(timestamp, visionBot);
			contextNewBots.put(botID, newBot);
		}
	}
	
	
	private void estimateControl(final RobotMotionResult_V2 oldState, final IFilter bot, final double dt,
			final long timestamp)
	{
		final double oldX = oldState.x;
		final double oldY = oldState.y;
		final double oldTheta = oldState.orientation;
		
		final RobotMotionResult_V2 newState = (RobotMotionResult_V2) bot.getPrediction(timestamp);
		final double newX = newState.x;
		final double newY = newState.y;
		final double newTheta = newState.orientation;
		
		final double sinOri = Math.sin(oldTheta);
		final double cosOri = Math.cos(oldTheta);
		
		// Determine new v_x and v_y
		final double dX = (newX - oldX);
		final double dY = (newY - oldY);
		
		final double vT = ((cosOri * dX) + (sinOri * dY)) / dt;
		final double vO = ((-sinOri * dX) + (cosOri * dY)) / dt;
		
		// Determine new omega
		double dOmega = newTheta - oldTheta;
		if (Math.abs(dOmega) > Math.PI)
		{
			dOmega = ((2 * Math.PI) - Math.abs(dOmega)) * (-1 * Math.signum(dOmega));
		}
		final double omega = dOmega / dt;
		
		// Determine new eta
		final double eta = 0.0 / dt;
		
		// Set determined values (control)
		bot.setControl(new OmnibotControl_V2(vT, vO, omega, eta));
	}
}
