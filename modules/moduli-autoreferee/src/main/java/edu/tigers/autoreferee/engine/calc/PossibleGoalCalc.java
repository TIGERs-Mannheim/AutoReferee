/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 2, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.calc;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.autoreferee.AutoRefConfig;
import edu.tigers.autoreferee.AutoRefFrame;
import edu.tigers.autoreferee.engine.NGeometry;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.wp.data.TrackedBall;


/**
 * @author "Lukas Magel"
 */
public class PossibleGoalCalc implements IRefereeCalc
{
	/**
	 * @author "Lukas Magel"
	 */
	public static final class PossibleGoal
	{
		private final long			timestamp;
		private final ETeamColor	goalColor;
		
		
		/**
		 * @param timestamp
		 * @param goalColor
		 */
		public PossibleGoal(final long timestamp, final ETeamColor goalColor)
		{
			this.timestamp = timestamp;
			this.goalColor = goalColor;
		}
		
		
		/**
		 * @return the timestamp
		 */
		public long getTimestamp()
		{
			return timestamp;
		}
		
		
		/**
		 * @return the goalColor
		 */
		public ETeamColor getGoalColor()
		{
			return goalColor;
		}
	}
	
	@Configurable(comment = "[degree] The angle by which the ball heading needs to change while inside the goal to count as goal")
	private static double	GOAL_BALL_CHANGE_ANGLE_DEGREE	= 45;
	
	@Configurable(comment = "[mm] The margin around the core zone which the ball must have entered")
	private static double	CORE_AREA_MARGIN					= 50;
	
	private PossibleGoal		detectedGoal						= null;
	
	private long				tsOnGoalEntry						= 0;
	private IVector2			ballHeadingOnGoalEntry			= null;
	private boolean			ballEnteredCoreZone				= false;
	
	static
	{
		ConfigRegistration.registerClass("autoreferee", PossibleGoalCalc.class);
	}
	
	
	@Override
	public void process(final AutoRefFrame frame)
	{
		TrackedBall ball = frame.getWorldFrame().getBall();
		IVector2 ballPos = ball.getPos();
		
		if (ball.isOnCam() && NGeometry.ballInsideGoal(ballPos))
		{
			/*
			 * The ball is inside the goal -> necessary condition
			 */
			if (ballHeadingOnGoalEntry == null)
			{
				ballHeadingOnGoalEntry = ball.getVel();
				tsOnGoalEntry = frame.getTimestamp();
			}
			
			if (!ballEnteredCoreZone)
			{
				ballEnteredCoreZone = NGeometry.ballInsideGoal(ballPos, -CORE_AREA_MARGIN);
			}
			/*
			 * The ball heading has changed by GOAL_BALL_CHANGE_ANGLE_DEGREE degrees --> Sufficient condition
			 * This will avoid false positives if the ball is kicked over the goal
			 */
			double angle = GeoMath.angleBetweenVectorAndVector(ballHeadingOnGoalEntry, ball.getVel());
			boolean ballHeadingChanged = angle > ((GOAL_BALL_CHANGE_ANGLE_DEGREE / 180) * Math.PI);
			/*
			 * Or the ball has come to rest inside the goal --> Sufficient condition
			 */
			boolean ballStationary = ballIsStationary(ball);
			
			if ((ballHeadingChanged || ballStationary) && ballEnteredCoreZone && (detectedGoal == null))
			{
				detectedGoal = new PossibleGoal(tsOnGoalEntry, getGoalColor(ballPos));
			}
			
		} else
		{
			detectedGoal = null;
			ballHeadingOnGoalEntry = null;
			ballEnteredCoreZone = false;
		}
		
		frame.setPossibleGoal(detectedGoal);
	}
	
	
	private ETeamColor getGoalColor(final IVector2 ballPos)
	{
		return NGeometry.getTeamOfClosestGoalLine(ballPos);
	}
	
	
	private static boolean ballIsStationary(final TrackedBall ball)
	{
		return ball.getVel().getLength() < AutoRefConfig.getBallStationarySpeedThreshold();
	}
	
}
