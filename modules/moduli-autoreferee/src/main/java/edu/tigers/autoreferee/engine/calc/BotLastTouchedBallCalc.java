/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 11, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.calc;

import java.awt.Color;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.autoreferee.AutoRefFrame;
import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.NGeometry.BotDistanceComparator;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.IBotIDMap;
import edu.tigers.sumatra.math.AVector2;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.ILine;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;
import edu.tigers.sumatra.wp.data.TrackedBall;
import edu.tigers.sumatra.wp.vis.EWpShapesLayer;


/**
 * This calculator tries to determine the bot that last touched the ball.
 * Currently, there is only the vision data available, so the information may not be accurate
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BotLastTouchedBallCalc implements IRefereeCalc
{
	/**
	 * @author "Lukas Magel"
	 */
	public enum CalcMode
	{
		/**  */
		VICINITY,
		/**  */
		BALL_HEADING
	}
	
	@Configurable(comment = "The algorithm to use for ball touch detection")
	private static CalcMode	mode		= CalcMode.BALL_HEADING;
	private BotPosition		lastBot	= new BotPosition();
	
	static
	{
		ConfigRegistration.registerClass("autoreferee", BotLastTouchedBallCalc.class);
	}
	
	
	/**
	  * 
	  */
	public BotLastTouchedBallCalc()
	{
		
	}
	
	
	@Override
	public void process(final AutoRefFrame frame)
	{
		BotPosition theChosenOne = null;
		if (mode == CalcMode.BALL_HEADING)
		{
			theChosenOne = processHeading(frame);
		} else
		{
			theChosenOne = processVicinity(frame);
		}
		
		if (theChosenOne != null)
		{
			frame.setBotTouchedBall(theChosenOne);
			lastBot = theChosenOne;
			addMark(frame, theChosenOne.getId(), Color.PINK);
		}
		
		
		addMark(frame, lastBot.getId(), Color.ORANGE);
		frame.setBotLastTouchedBall(lastBot);
	}
	
	
	private void addMark(final IAutoRefFrame frame, final BotID id, final Color color)
	{
		List<IDrawableShape> shapes = frame.getShapes().get(EWpShapesLayer.AUTOREFEREE);
		ITrackedBot bot = frame.getWorldFrame().getBot(id);
		if (bot != null)
		{
			shapes.add(new DrawableCircle(bot.getPos(), Geometry.getBotRadius() * 1.5d, color));
		}
	}
	
	
	private static final double	MIN_DIST			= Geometry.getBotRadius() + Geometry.getBallRadius() + 10;
	private static final double	EXTENDED_DIST	= MIN_DIST + 25;
	private static final double	ANGLE_EPSILON	= 0.1;
	
	
	private BotPosition processVicinity(final IAutoRefFrame frame)
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
				foundBotTouchedBall = true;
				continue;
			}
			// if ball is too fast calculate with position from prev frame
			double preDist = GeoMath.distancePP(prevBall, bot.getPos());
			if ((preDist <= MIN_DIST) && (preDist < smallestDist))
			{
				smallestDist = preDist;
				theChosenOne = new BotPosition(ts, bot);
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
						foundBotTouchedBall = true;
					}
				}
			}
			
		}
		
		if (foundBotTouchedBall)
		{
			return theChosenOne;
		}
		return null;
	}
	
	@Configurable
	private static double	ANGLE_THRESHOLD_DEGREE	= 10;
	
	/** in mm */
	@Configurable
	private static double	MIN_SEARCH_RADIUS			= 300;
	
	@Configurable
	private static double	BOT_RADIUS_MARGIN			= 30;
	
	
	private BotPosition processHeading(final IAutoRefFrame frame)
	{
		
		TrackedBall curBall = frame.getWorldFrame().getBall();
		TrackedBall prevBall = frame.getPreviousFrame().getWorldFrame().getBall();
		
		
		IVector2 prevHeading = prevBall.getVel();
		IVector2 curHeading = curBall.getVel();
		
		double radAngle = GeoMath.angleBetweenVectorAndVector(prevHeading, curHeading);
		if (radAngle > Math.toRadians(ANGLE_THRESHOLD_DEGREE))
		{
			IVector2 ballPos = curBall.getPos();
			ILine ballHeadingLine = new Line(ballPos, curBall.getVel().multiplyNew(-1.0d));
			List<ITrackedBot> closeBots = getCloseBots(frame, ballPos);
			
			
			Optional<ITrackedBot> optTouchedBot = closeBots.stream()
					.filter(bot -> {
						IVector2 leadPoint = GeoMath.leadPointOnLine(bot.getPos(), ballHeadingLine);
						double lineToBotDist = GeoMath.distancePP(bot.getPos(), leadPoint);
						if ((lineToBotDist < (Geometry.getBotRadius() + BOT_RADIUS_MARGIN)) &&
								ballHeadingLine.isPointInFront(bot.getPos()))
						{
							return true;
						}
						return false;
					})
					.sorted(new BotDistanceComparator(ballPos))
					.findFirst();
			
			if (optTouchedBot.isPresent())
			{
				ITrackedBot touchedBot = optTouchedBot.get();
				return new BotPosition(frame.getTimestamp(), touchedBot);
			}
		}
		return null;
	}
	
	
	private List<ITrackedBot> getCloseBots(final IAutoRefFrame frame, final IVector2 pos)
	{
		IBotIDMap<ITrackedBot> bots = frame.getWorldFrame().getBots();
		TrackedBall ball = frame.getWorldFrame().getBall();
		long timeDelta_ns = frame.getWorldFrame().getTimestamp()
				- frame.getPreviousFrame().getWorldFrame().getTimestamp();
		
		/*
		 * Velocity in [m/s] is equal to velocity in [mm/ms]
		 */
		double ballTravelDist = ball.getVel().getLength() * TimeUnit.NANOSECONDS.toMillis(timeDelta_ns);
		double radius = Math.max(ballTravelDist, MIN_SEARCH_RADIUS);
		
		return bots.values().stream()
				.filter(bot -> GeoMath.distancePP(bot.getPos(), pos) < radius)
				.collect(Collectors.toList());
	}
}
