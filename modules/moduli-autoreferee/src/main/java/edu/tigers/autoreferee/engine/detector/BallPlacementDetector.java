package edu.tigers.autoreferee.engine.detector;

import java.awt.Color;
import java.util.Optional;

import com.github.g3force.configurable.Configurable;

import edu.tigers.autoreferee.EAutoRefShapesLayer;
import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.sumatra.Referee;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.filter.iir.ExponentialMovingAverageFilter2D;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;
import edu.tigers.sumatra.referee.gameevent.PlacementFailed;
import edu.tigers.sumatra.referee.gameevent.PlacementSucceeded;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.util.BotDistanceComparator;


/**
 * Detect successful and failed ball placements
 */
public class BallPlacementDetector extends AGameEventDetector
{
	@Configurable(defValue = "150.0", comment = "Minimum distance [mm] to placement pos to accept the placement")
	private static double ballPlacementTolerance = 150.0;
	
	@Configurable(defValue = "0.97", comment = "Alpha value for the exponential moving average filter on the ball pos that decides if a ball is still moving")
	private static double ballMovingFilterAlpha = 0.97;
	
	@Configurable(defValue = "50.0", comment = "Min distance [mm] between ball and bot, before ball placement is considered successful, if next command is a free kick")
	private static double minDistanceToBallForFreeKick = 50.0;
	
	@Configurable(defValue = "50.0", comment = "Min distance [mm] between ball and bot, before ball placement is considered successful, if next command is a force start")
	private static double minDistanceToBallForForceStart = 50.0;
	
	@Configurable(defValue = "2.0", comment = "Minimum time [s] that the ball placement must take to allow robots to move to valid positions")
	private static double minBallPlacementDuration = 2.0;
	
	static
	{
		registerClass(BallPlacementDetector.class);
	}
	
	
	private long tStart = 0;
	private IVector2 initialBallPos;
	private ExponentialMovingAverageFilter2D ballPosFilter;
	private boolean eventRaised = false;
	private long lastCommandCounter = Integer.MAX_VALUE;
	
	
	public BallPlacementDetector()
	{
		super(EGameEventDetectorType.BALL_PLACEMENT, EGameState.BALL_PLACEMENT);
	}
	
	
	@Override
	protected void doPrepare()
	{
		resetState(frame);
		ballPosFilter = new ExponentialMovingAverageFilter2D(ballMovingFilterAlpha, initialBallPos);
	}
	
	
	private void resetState(final IAutoRefFrame frame)
	{
		tStart = frame.getTimestamp();
		initialBallPos = frame.getWorldFrame().getBall().getPos();
		eventRaised = false;
		lastCommandCounter = frame.getRefereeMsg().getCommandCounter();
	}
	
	
	@Override
	protected Optional<IGameEvent> doUpdate()
	{
		if (frame.getRefereeMsg().getCommandCounter() != lastCommandCounter)
		{
			// still in ball placement state, but with a new command (probably other team)
			resetState(frame);
		}
		
		if (eventRaised || frame.getGameState().getBallPlacementPositionNeutral() == null)
		{
			return Optional.empty();
		}
		
		final IVector2 ballPos = frame.getWorldFrame().getBall().getPos();
		final ETeamColor forTeam = frame.getGameState().getForTeam();
		final double remainingDistance = frame.getGameState().getBallPlacementPositionNeutral().distanceTo(ballPos);
		final double elapsedTime = (frame.getTimestamp() - tStart) / 1e9;
		
		ballPosFilter.setAlpha(ballMovingFilterAlpha);
		ballPosFilter.update(ballPos);
		
		final boolean ballStill = ballPosFilter.getState().getXYVector().distanceTo(ballPos) < 5;
		Color color = ballStill ? Color.green : Color.red;
		frame.getShapes().get(EAutoRefShapesLayer.ENGINE)
				.add(new DrawableCircle(Circle.createCircle(ballPosFilter.getState().getXYVector(), 50), color));
		
		if (remainingDistance <= ballPlacementTolerance
				&& ballStill
				&& botsHaveSufficientDistanceToBall()
				&& elapsedTime > minBallPlacementDuration)
		{
			eventRaised = true;
			double movedDistance = initialBallPos.distanceTo(ballPos);
			return Optional.of(new PlacementSucceeded(forTeam, elapsedTime, remainingDistance, movedDistance));
		}
		
		if (frame.getRefereeMsg().getCurrentActionTimeRemaining() < 0)
		{
			eventRaised = true;
			return Optional.of(new PlacementFailed(forTeam, remainingDistance));
		}
		
		return Optional.empty();
	}
	
	
	private boolean botsHaveSufficientDistanceToBall()
	{
		final Optional<ITrackedBot> nearestBot = frame.getWorldFrame().getBots().values().stream()
				.min(new BotDistanceComparator(frame.getWorldFrame().getBall().getPos()));
		if (nearestBot.isPresent())
		{
			double minDistance = isNextCommandForPlacingTeam() ? minDistanceToBallForFreeKick
					: minDistanceToBallForForceStart;
			double curDistance = nearestBot.get().getPos().distanceTo(frame.getWorldFrame().getBall().getPos());
			return curDistance > minDistance + Geometry.getBallRadius() + Geometry.getBotRadius();
		}
		// no bots -> no bot can be too close :)
		return true;
	}
	
	
	private boolean isNextCommandForPlacingTeam()
	{
		if (frame.getRefereeMsg().getCommand() == Referee.SSL_Referee.Command.BALL_PLACEMENT_BLUE)
		{
			return frame.getRefereeMsg().getNextCommand() == Referee.SSL_Referee.Command.DIRECT_FREE_BLUE
					|| frame.getRefereeMsg().getNextCommand() == Referee.SSL_Referee.Command.INDIRECT_FREE_BLUE;
		} else if (frame.getRefereeMsg().getCommand() == Referee.SSL_Referee.Command.BALL_PLACEMENT_YELLOW)
		{
			return frame.getRefereeMsg().getNextCommand() == Referee.SSL_Referee.Command.DIRECT_FREE_YELLOW
					|| frame.getRefereeMsg().getNextCommand() == Referee.SSL_Referee.Command.INDIRECT_FREE_YELLOW;
		}
		return false;
	}
}
