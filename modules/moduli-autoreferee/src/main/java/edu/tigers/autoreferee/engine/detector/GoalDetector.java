/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.detector;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.AObjectID;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;
import edu.tigers.sumatra.referee.gameevent.PossibleGoal;
import edu.tigers.sumatra.wp.data.BallLeftFieldPosition;
import edu.tigers.sumatra.wp.data.KickedBall;
import edu.tigers.sumatra.wp.data.TimedPosition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


/**
 * Detect possible goals.
 */
public class GoalDetector extends AGameEventDetector
{
	private final Logger log = LogManager.getLogger(GoalDetector.class.getName());

	private TimedPosition lastBallLeftFieldPos = null;
	private final Map<ETeamColor, Long> lastTouchedMap = new EnumMap<>(ETeamColor.class);
	private final Map<ETeamColor, Double> maxBallHeightMap = new EnumMap<>(ETeamColor.class);

	public GoalDetector()
	{
		super(EGameEventDetectorType.GOAL, Set.of(EGameState.RUNNING, EGameState.PENALTY));
		lastTouchedMap.put(ETeamColor.BLUE, 0L);
		lastTouchedMap.put(ETeamColor.YELLOW, 0L);
		maxBallHeightMap.put(ETeamColor.BLUE, 0.0);
		maxBallHeightMap.put(ETeamColor.YELLOW, 0.0);
	}


	@Override
	protected Optional<IGameEvent> doUpdate()
	{
		for (ETeamColor team : ETeamColor.yellowBlueValues())
		{
			if (frame.getBotsTouchingBall().stream().anyMatch(b -> b.getBotID().getTeamColor() == team))
			{
				lastTouchedMap.put(team, frame.getTimestamp());
				maxBallHeightMap.put(team, 0.0);
			} else
			{
				maxBallHeightMap.compute(team,
						(t, h) -> Math.max(h == null ? 0.0 : h, frame.getWorldFrame().getBall().getHeight()));
			}
		}
		return frame.getBallLeftFieldPos()
				.filter(b -> !b.getPosition().similarTo(lastBallLeftFieldPos))
				.map(this::processBallLeftField);
	}


	@Override
	protected void doReset()
	{
		lastBallLeftFieldPos = null;
	}


	private IGameEvent processBallLeftField(final BallLeftFieldPosition ballLeftFieldPosition)
	{
		lastBallLeftFieldPos = ballLeftFieldPosition.getPosition();

		if (ballLeftFieldPosition.getType() != BallLeftFieldPosition.EBallLeftFieldType.GOAL)
		{
			return null;
		}

		final var kickEvent = frame.getWorldFrame().getKickedBall();
		final IVector2 kickLocation = kickEvent.map(KickedBall::getPosition).orElse(null);
		final BotID kickingBot = kickEvent.map(KickedBall::getKickingBot).filter(AObjectID::isBot).orElse(null);

		warnIfNoKickEventPresent();

		return createEvent(ballLeftFieldPosition, kickLocation, kickingBot);
	}


	private void warnIfNoKickEventPresent()
	{
		if (frame.getWorldFrame().getKickedBall().isEmpty())
		{
			log.warn("Goal detected, but no kick event found.");
		}
	}


	private ETeamColor goalForTeam(final BallLeftFieldPosition ballLeftFieldPosition)
	{
		if (ballLeftFieldPosition.getPosition().getPos().x() < 0)
		{
			// x < 0 -> inside goal of team on the negative side -> goal for the other team
			return Geometry.getNegativeHalfTeam().opposite();
		}
		return Geometry.getNegativeHalfTeam();
	}


	private IGameEvent createEvent(final BallLeftFieldPosition ballLeftFieldPosition,
			final IVector2 kickLocation, final BotID kickingBot)
	{
		final ETeamColor forTeam = goalForTeam(ballLeftFieldPosition);
		final IVector2 location = ballLeftFieldPosition.getPosition().getPos();
		final double maxBallHeight = maxBallHeightMap.get(forTeam);
		final int numRobots = (int) frame.getWorldFrame().getBots().keySet().stream()
				.filter(b -> b.getTeamColor() == forTeam).count();
		final long lastTouched = lastTouchedMap.get(forTeam);
		return new PossibleGoal(forTeam, kickingBot, location, kickLocation, maxBallHeight, numRobots, lastTouched);
	}
}
