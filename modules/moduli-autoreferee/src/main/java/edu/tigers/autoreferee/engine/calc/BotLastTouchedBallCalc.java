/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 11, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.calc;

import java.util.Collection;
import java.util.LinkedList;

import edu.tigers.autoreferee.AutoRefFrame;
import edu.tigers.sumatra.math.AVector2;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;


/**
 * This calculator tries to determine the bot that last touched the ball.
 * Currently, there is only the vision data available, so the information may not be accurate
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BotLastTouchedBallCalc implements IRefereeCalc
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final double	MIN_DIST			= Geometry.getBotRadius()
																		+ Geometry.getBallRadius() + 10;
	private static final double	EXTENDED_DIST	= MIN_DIST + 25;
	private static final double	ANGLE_EPSILON	= 0.1;
	/** frames to wait, before setting bot. if 60ps: 1sek */
	// private static final int MIN_FRAMES = 0;
	// private int numFrames = 0;
	private BotPosition				lastBot			= new BotPosition();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	  * 
	  */
	public BotLastTouchedBallCalc()
	{
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void process(final AutoRefFrame frame)
	{
		long ts = frame.getTimestamp();
		SimpleWorldFrame wFrame = frame.getWorldFrame();
		BotPosition theChosenOne = frame.getPreviousFrame().getBotLastTouchedBall();
		
		IVector2 ball = wFrame.getBall().getPos();
		IVector2 prevBall = frame.getPreviousFrame().getWorldFrame().getBall().getPos();
		Collection<ITrackedBot> bots = new LinkedList<>();
		bots.addAll(wFrame.getBots().values());
		double smallestDist = Double.MAX_VALUE;
		double smallestAngle = Double.MAX_VALUE;
		boolean foundBotTouchedBall = false;
		for (ITrackedBot bot : bots)
		{
			double dist = GeoMath.distancePP(ball, bot.getPos());
			if ((dist <= MIN_DIST) && (dist < smallestDist))
			{
				smallestDist = dist;
				theChosenOne = new BotPosition(ts, bot);
				lastBot = theChosenOne;
				foundBotTouchedBall = true;
				continue;
			}
			// if ball is too fast calculate with position from prev frame
			double preDist = GeoMath.distancePP(prevBall, bot.getPos());
			if ((preDist <= MIN_DIST) && (preDist < smallestDist))
			{
				smallestDist = preDist;
				theChosenOne = new BotPosition(ts, bot);
				lastBot = theChosenOne;
				foundBotTouchedBall = true;
				continue;
			}
			// if ball is still too fast check if it was kicked (fast acceleration in kicker direction)
			IVector2 ballVel = wFrame.getBall().getVel();
			if (!ballVel.equals(AVector2.ZERO_VECTOR))
			{
				double ballAngle = GeoMath.angleBetweenXAxisAndLine(AVector2.ZERO_VECTOR, ballVel);
				double botAngle = bot.getAngle();
				double angleDiff = Math.abs(AngleMath.difference(ballAngle, botAngle));
				if ((angleDiff < ANGLE_EPSILON) && (angleDiff < smallestAngle))
				{
					if ((dist < EXTENDED_DIST) || (preDist < EXTENDED_DIST))
					{
						smallestAngle = angleDiff;
						theChosenOne = new BotPosition(ts, bot);
						lastBot = theChosenOne;
						foundBotTouchedBall = true;
					}
				}
			}
			
		}
		
		if (foundBotTouchedBall)
		{
			frame.setBotLastTouchedBall(theChosenOne);
			frame.setBotTouchedBall(theChosenOne);
		} else
		{
			frame.setBotLastTouchedBall(lastBot);
			frame.setBotTouchedBall(null);
		}
	}
}
