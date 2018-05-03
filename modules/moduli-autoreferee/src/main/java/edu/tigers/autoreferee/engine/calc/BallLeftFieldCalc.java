/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine.calc;

import java.util.LinkedList;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.Configurable;

import edu.tigers.autoreferee.AutoRefFrame;
import edu.tigers.autoreferee.EAutoRefShapesLayer;
import edu.tigers.autoreferee.generic.TimedPosition;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;


/**
 * Save the moment, the ball left the field
 */
public class BallLeftFieldCalc implements IRefereeCalc
{
	private static final Logger log = Logger.getLogger(BallLeftFieldCalc.class.getName());
	
	@Configurable(comment = "Time [s] to wait before using ball positions, invaliding all positions just before a chip kick", defValue = "0.3")
	private static double maxTimeToDetectChipKick = 0.3;
	
	@Configurable(comment = "Time [s] between two ball positions to pass before comparing them in order to check if the ball left the field")
	private static double minComparisonTimeSpan = 0.05;
	
	private final LinkedList<TimedPosition> ballPosBuffer = new LinkedList<>();
	
	
	@Override
	public void process(final AutoRefFrame frame)
	{
		reduceBallPosBuffer(frame.getTimestamp());
		addToBallPosBuffer(frame.getWorldFrame());
		removeFirstChippedBallPositions(frame);
		
		updateDetection(frame);
		
		drawBallLeftFieldPos(frame);
	}
	
	
	private void updateDetection(final AutoRefFrame frame)
	{
		TimedPosition oldPos = ballPosBuffer.peekLast();
		TimedPosition newPos = firstValidBallPos(frame);
		
		TimedPosition ballLeftFieldPos = frame.getPreviousFrame().getBallLeftFieldPos();
		boolean ballInsideField = frame.getPreviousFrame().isBallInsideField();
		
		if (oldPos != null && newPos != null
				&& (newPos.getTimestamp() - oldPos.getTimestamp()) / 1e9 >= minComparisonTimeSpan)
		{
			ballInsideField = Geometry.getField().withMargin(Geometry.getLineWidth() + Geometry.getBallRadius())
					.isPointInShape(newPos.getPos());
			boolean stateChanged = ballInsideField != frame.getPreviousFrame().isBallInsideField();
			if (!ballInsideField && stateChanged)
			{
				ILine line = Line.fromPoints(newPos.getPos(), oldPos.getPos());
				IVector2 pos = newPos.getPos().nearestTo(Geometry.getField().lineIntersections(line));
				ballLeftFieldPos = new TimedPosition(newPos.getTimestamp(), pos);
			}
		}
		
		frame.setBallLeftFieldPos(ballLeftFieldPos);
		frame.setBallInsideField(ballInsideField);
	}
	
	
	private void drawBallLeftFieldPos(final AutoRefFrame frame)
	{
		if (frame.getBallLeftFieldPos() != null)
		{
			frame.getShapes().get(EAutoRefShapesLayer.BALL_LEFT_FIELD)
					.add(new DrawableCircle(Circle.createCircle(frame.getBallLeftFieldPos().getPos(), 100)));
		}
	}
	
	
	private TimedPosition firstValidBallPos(final AutoRefFrame frame)
	{
		for (TimedPosition timedPosition : ballPosBuffer)
		{
			if (timedPosition.getAge(frame.getTimestamp()) > maxTimeToDetectChipKick)
			{
				return timedPosition;
			}
		}
		return null;
	}
	
	
	private void removeFirstChippedBallPositions(final AutoRefFrame frame)
	{
		if (frame.getWorldFrame().getBall().isChipped())
		{
			if (frame.getWorldFrame().getKickEvent().isPresent())
			{
				double age = (frame.getTimestamp() - frame.getWorldFrame().getKickEvent().get().getTimestamp()) / 1e9;
				if (age < maxTimeToDetectChipKick)
				{
					ballPosBuffer.clear();
				}
			} else
			{
				log.warn("Chipped ball has no kick event!");
			}
		}
	}
	
	
	private void addToBallPosBuffer(final SimpleWorldFrame frame)
	{
		TimedPosition pos = new TimedPosition(frame.getTimestamp(), frame.getBall().getPos());
		ballPosBuffer.offerFirst(pos);
	}
	
	
	private void reduceBallPosBuffer(final long currentTimestamp)
	{
		ballPosBuffer.removeIf(t -> t.getTimestamp() > currentTimestamp);
		while (!ballPosBuffer.isEmpty())
		{
			double age = (currentTimestamp - ballPosBuffer.get(ballPosBuffer.size() - 1).getTimestamp()) / 1e9;
			if (age > maxTimeToDetectChipKick + minComparisonTimeSpan + 0.2)
			{
				ballPosBuffer.remove(ballPosBuffer.size() - 1);
			} else
			{
				break;
			}
		}
	}
}
