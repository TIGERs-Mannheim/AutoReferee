/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.11.2011
 * Author(s): osteinbrecher
 * *********************************************************
 */
package edu.tigers.sumatra.wp;

import java.util.Random;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.BotIDMap;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.ids.IBotIDMap;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;
import edu.tigers.sumatra.wp.data.TrackedBall;
import edu.tigers.sumatra.wp.data.TrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * Factory which creates a list of {@link WorldFrame}s with random positioned bots.
 * 
 * @author Oliver Steinbrecher
 */
public class WorldFrameFactory
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/** random without seed for reproducibility */
	private static final Random RND = new Random();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public WorldFrameFactory()
	{
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @param frameNumber
	 * @param timestamp
	 * @return
	 */
	public WorldFrame createWorldFrame(final long frameNumber, final long timestamp)
	{
		return new WorldFrame(createSimpleWorldFrame(frameNumber, timestamp), ETeamColor.YELLOW, false);
	}
	
	
	/**
	 * Creates a new WorldFrame with random positioned bots.
	 * 
	 * @param frameNumber the id of the {@link WorldFrame}.
	 * @param timestamp
	 * @return
	 */
	public SimpleWorldFrame createSimpleWorldFrame(final long frameNumber, final long timestamp)
	{
		final IBotIDMap<ITrackedBot> bots = new BotIDMap<>();
		
		for (int i = 0; i < 6; i++)
		{
			BotID idF = BotID.createBotId(i, ETeamColor.BLUE);
			bots.put(idF, createBot(idF, ETeamColor.BLUE));
			
			BotID idT = BotID.createBotId(i, ETeamColor.YELLOW);
			bots.put(idT, createBot(idT, ETeamColor.YELLOW));
		}
		
		final TrackedBall ball = TrackedBall.defaultInstance();
		
		SimpleWorldFrame swf = new SimpleWorldFrame(bots, ball, frameNumber, timestamp);
		return swf;
	}
	
	
	/**
	 * Creates a new WorldFrame without bots
	 * 
	 * @param frameNumber the id of the {@link WorldFrame}.
	 * @param timestamp
	 * @return
	 */
	public static SimpleWorldFrame createEmptyWorldFrame(final long frameNumber, final long timestamp)
	{
		return SimpleWorldFrame.createEmptyWorldFrame(frameNumber, timestamp);
	}
	
	
	/**
	 * Create bot with random positions
	 * 
	 * @param id
	 * @param color
	 * @return bot
	 */
	public ITrackedBot createBot(final BotID id, final ETeamColor color)
	{
		double x = (RND.nextDouble() * Geometry.getFieldLength())
				- (Geometry.getFieldLength() / 2.0);
		double y = (RND.nextDouble() * Geometry.getFieldWidth())
				- (Geometry.getFieldWidth() / 2.0);
		final IVector2 pos = new Vector2(x, y);
		
		TrackedBot tBot = new TrackedBot(id);
		tBot.setPos(pos);
		return tBot;
	}
}
