/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.tigers.sumatra.bot.EFeature;
import edu.tigers.sumatra.bot.EFeatureState;
import edu.tigers.sumatra.bot.RobotInfo;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.botshape.BotShape;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBall;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BallContactCalculator
{
	private static final double BALL_POSS_TOLERANCE_HAS = 60;
	private static final double BALL_POSS_TOLERANCE_GET = 20;
	
	private final Map<BotID, Boolean> ballContactLastFrame = new HashMap<>();
	private ITrackedBall trackedBall;
	private final Map<BotID, List<BarrierState>> barrierInterruptedBufferMap = new HashMap<>();
	
	
	public boolean ballContact(final RobotInfo robotInfo, final Pose pose, final double center2Dribbler)
	{
		List<BarrierState> barrierInterruptedBuffer = barrierInterruptedBufferMap.computeIfAbsent(robotInfo.getBotId(), b -> new LinkedList<>());
		if (robotInfo.getBotFeatures().get(EFeature.BARRIER) == EFeatureState.WORKING)
		{
			barrierInterruptedBuffer.add(new BarrierState(robotInfo.getTimestamp(), robotInfo.isBarrierInterrupted()));
		}
		long minTimestamp = robotInfo.getTimestamp() - (long) 5e8;
		barrierInterruptedBuffer.removeIf(state -> state.timestamp < minTimestamp);
		if (!barrierInterruptedBuffer.isEmpty())
		{
			return barrierInterruptedBuffer.stream().anyMatch(b -> b.interrupted);
		}

		double ballPossTolerance;
		if (ballContactLastFrame.containsKey(robotInfo.getBotId()) && ballContactLastFrame.get(robotInfo.getBotId()))
		{
			ballPossTolerance = BALL_POSS_TOLERANCE_HAS;
		} else
		{
			ballPossTolerance = BALL_POSS_TOLERANCE_GET;
		}
		
		final IVector2 optimalBallPossPos = BotShape.getKickerCenterPos(pose.getPos(), pose.getOrientation(),
				center2Dribbler + Geometry.getBallRadius());
		ICircle circle = Circle.createCircle(optimalBallPossPos, ballPossTolerance);
		
		boolean ballContact = circle.isPointInShape(trackedBall.getPos());
		ballContactLastFrame.put(robotInfo.getBotId(), ballContact);
		return ballContact;
	}
	
	
	public void setTrackedBall(final ITrackedBall trackedBall)
	{
		this.trackedBall = trackedBall;
	}
	
	private static class BarrierState
	{
		long timestamp;
		boolean interrupted;
		
		
		public BarrierState(final long timestamp, final boolean interrupted)
		{
			this.timestamp = timestamp;
			this.interrupted = interrupted;
		}
	}
}
