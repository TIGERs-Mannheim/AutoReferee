/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine.detector;

import java.util.List;
import java.util.Optional;

import com.github.g3force.configurable.Configurable;

import edu.tigers.autoreferee.engine.NGeometry;
import edu.tigers.autoreferee.generic.BotPosition;
import edu.tigers.autoreferee.generic.TimedPosition;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.gameevent.AimlessKick;
import edu.tigers.sumatra.referee.gameevent.BallLeftFieldGoalLine;
import edu.tigers.sumatra.referee.gameevent.BallLeftFieldTouchLine;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;


/**
 * This rule detects when the ball leaves the field. This rule also handles icing (aimless kick).
 */
public class BallLeftFieldDetector extends AGameEventDetector
{
	@Configurable(comment = "[mm] The goal line threshold", defValue = "10.0")
	private static double goalLineThreshold = 10.0;
	
	@Configurable(comment = "[mm] A goalline off is only considered icing if the bot was located more than this value behind the kickoff line", defValue = "200.0")
	private static double icingKickoffLineThreshold = 200.0;
	
	static
	{
		registerClass(BallLeftFieldDetector.class);
	}
	
	private TimedPosition lastBallLeftFieldPos = null;
	
	
	/**
	 * Create new instance of the BallLeftFieldDetector
	 */
	public BallLeftFieldDetector()
	{
		super(EGameEventDetectorType.BALL_LEFT_FIELD, EGameState.RUNNING);
	}
	
	
	@Override
	public Optional<IGameEvent> doUpdate()
	{
		if (frame.getBallLeftFieldPos().isPresent()
				&& !frame.getBallLeftFieldPos().get().similarTo(lastBallLeftFieldPos))
		{
			lastBallLeftFieldPos = frame.getBallLeftFieldPos().get();
			
			BotPosition lastTouched = botThatLastTouchedBall();
			if (!lastTouched.getBotID().isBot())
			{
				// we do not know, which bot touched the ball last.
				return Optional.empty();
			}
			
			boolean exitGoalLineInX = ((Geometry.getFieldLength() / 2)
					- Math.abs(lastBallLeftFieldPos.getPos().x())) < goalLineThreshold;
			boolean exitGoalLineInY = ((Geometry.getFieldWidth() / 2)
					- Math.abs(lastBallLeftFieldPos.getPos().y())) > goalLineThreshold;
			boolean enteredGoalInY = Geometry.getGoalOur().getWidth() / 2
					- Math.abs(lastBallLeftFieldPos.getPos().y()) > goalLineThreshold;
			if (exitGoalLineInX && exitGoalLineInY)
			{
				// The ball exited the field over the goal line
				if (enteredGoalInY)
				{
					// a potential goal
					return Optional.empty();
				}
				return handleGoalLineOff(lastBallLeftFieldPos.getPos(), lastTouched);
			}
			return handleSideLineOff(lastBallLeftFieldPos.getPos(), lastTouched);
		}
		return Optional.empty();
	}
	
	
	private BotPosition botThatLastTouchedBall()
	{
		final List<BotPosition> botLastTouchedBall = frame.getBotsLastTouchedBall();
		if (botLastTouchedBall.size() == 1)
		{
			return botLastTouchedBall.get(0);
		}
		return new BotPosition(frame.getTimestamp(), frame.getWorldFrame().getBall().getPos(), BotID.noBot());
	}
	
	
	private Optional<IGameEvent> handleSideLineOff(final IVector2 ballPos, final BotPosition lastTouched)
	{
		return Optional.of(new BallLeftFieldTouchLine(lastTouched.getBotID(), ballPos));
	}
	
	
	private Optional<IGameEvent> handleGoalLineOff(final IVector2 ballPos, final BotPosition lastTouched)
	{
		if (isIcing(lastTouched, ballPos))
		{
			return Optional.of(new AimlessKick(lastTouched.getBotID(), ballPos, lastTouched.getPos()));
		}
		
		return Optional.of(new BallLeftFieldGoalLine(lastTouched.getBotID(), ballPos));
	}
	
	
	private boolean isIcing(final BotPosition lastTouched, final IVector2 ballPos)
	{
		ETeamColor colorOfGoalLine = NGeometry.getTeamOfClosestGoalLine(ballPos);
		ETeamColor kickerColor = lastTouched.getBotID().getTeamColor();
		
		boolean kickerWasInHisHalf = NGeometry.getFieldSide(kickerColor).isPointInShape(lastTouched.getPos())
				&& (Math.abs(lastTouched.getPos().x()) > icingKickoffLineThreshold);
		boolean crossedOppositeGoalLine = kickerColor != colorOfGoalLine;
		return kickerWasInHisHalf && crossedOppositeGoalLine;
	}
	
	
	@Override
	public void doReset()
	{
		lastBallLeftFieldPos = null;
	}
}
