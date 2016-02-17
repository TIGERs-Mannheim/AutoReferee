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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.tigers.sumatra.cam.data.CamRobot;
import edu.tigers.sumatra.clock.StaticSimulationClock;
import edu.tigers.sumatra.export.CSVExporter;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.wp.ExportDataContainer;
import edu.tigers.sumatra.wp.ExportDataContainer.WpBot;
import edu.tigers.sumatra.wp.data.ITrackedBot;
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
			
			final RobotMotionResult_V2 oldState = (RobotMotionResult_V2) existingBot.getLookahead(0);
			
			existingBot.observation(timestamp, visionBot);
			estimateControl(oldState, existingBot, dt);
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
	
	
	/**
	 */
	public void performCollisionAwareLookahead()
	{
		for (int i = 1; i <= context.getStepCount(); i++)
		{
			final Iterator<IFilter> foodIt = context.getBlueBots().values().iterator();
			while (foodIt.hasNext())
			{
				final IFilter food = foodIt.next();
				food.performLookahead(i);
			}
			
			final Iterator<IFilter> tigerIt = context.getYellowBots().values().iterator();
			while (tigerIt.hasNext())
			{
				final IFilter tiger = tigerIt.next();
				tiger.performLookahead(i);
			}
		}
	}
	
	
	private void estimateControl(final RobotMotionResult_V2 oldState, final IFilter bot, final double dt)
	{
		final double oldX = oldState.x;
		final double oldY = oldState.y;
		final double oldTheta = oldState.orientation;
		
		final RobotMotionResult_V2 newState = (RobotMotionResult_V2) bot.getLookahead(0);
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
	
	
	/**
	 * @param folder
	 */
	public static void runOnData(final String folder)
	{
		List<CamRobot> yellowBots = ExportDataContainer.readRawBots(folder, "rawBots", ETeamColor.YELLOW);
		List<CamRobot> blueBots = ExportDataContainer.readRawBots(folder, "rawBots", ETeamColor.BLUE);
		
		StaticSimulationClock clock = new StaticSimulationClock();
		long curFrameId;
		if (!yellowBots.isEmpty())
		{
			clock.setNanoTime(yellowBots.get(0).getTimestamp());
			curFrameId = yellowBots.get(0).getFrameId();
		} else if (!blueBots.isEmpty())
		{
			clock.setNanoTime(blueBots.get(0).getTimestamp());
			curFrameId = blueBots.get(0).getFrameId();
		} else
		{
			return;
		}
		
		PredictionContext context = new PredictionContext();
		BotProcessor botProcessor = new BotProcessor(context);
		TrackingManager tm = new TrackingManager(context);
		
		List<WpBot> outBots = new ArrayList<>();
		
		while (!yellowBots.isEmpty() || !blueBots.isEmpty())
		{
			List<CamRobot> camBotsY = new ArrayList<>();
			List<CamRobot> camBotsB = new ArrayList<>();
			while (!yellowBots.isEmpty() && (yellowBots.get(0).getFrameId() == curFrameId))
			{
				camBotsY.add(yellowBots.remove(0));
			}
			while (!blueBots.isEmpty() && (blueBots.get(0).getFrameId() == curFrameId))
			{
				camBotsB.add(blueBots.remove(0));
			}
			
			curFrameId++;
			final long curTimeStamp;
			if (!camBotsB.isEmpty())
			{
				curTimeStamp = camBotsB.get(0).getTimestamp();
			} else if (!camBotsY.isEmpty())
			{
				curTimeStamp = camBotsY.get(0).getTimestamp();
			} else
			{
				continue;
			}
			
			botProcessor.process(camBotsY, camBotsB);
			botProcessor.performCollisionAwareLookahead();
			tm.checkItems(curTimeStamp);
			
			if (!camBotsB.isEmpty())
			{
				clock.setNanoTime(camBotsB.get(0).getTimestamp());
			} else if (!camBotsY.isEmpty())
			{
				clock.setNanoTime(camBotsY.get(0).getTimestamp());
			}
			
			addBots(context.getBlueBots(), context.getStepCount(), curFrameId, curTimeStamp, ETeamColor.BLUE, outBots);
			addBots(context.getYellowBots(), context.getStepCount(), curFrameId, curTimeStamp, ETeamColor.YELLOW, outBots);
		}
		
		CSVExporter.exportList(folder, "wpBotsTest", outBots.stream().map(c -> c));
	}
	
	
	private static void addBots(final Map<Integer, IFilter> bots, final int lookaheadSteps, final long frameId,
			final long timestamp, final ETeamColor color, final List<WpBot> outBots)
	{
		for (Map.Entry<Integer, IFilter> entry : bots.entrySet())
		{
			int id = color == ETeamColor.YELLOW ? entry.getKey() - WPConfig.YELLOW_ID_OFFSET : entry.getKey()
					- WPConfig.BLUE_ID_OFFSET;
			BotID botId = BotID.createBotId(id, color);
			IFilter f = entry.getValue();
			RobotMotionResult_V2 motion = (RobotMotionResult_V2) f.getLookahead(lookaheadSteps);
			ITrackedBot tBot = motion.motionToTrackedBot(botId);
			outBots.add(ExportDataContainer.trackedBot2WpBot(tBot, frameId, timestamp));
		}
	}
}
