/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.vision;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.bot.RobotInfo;
import edu.tigers.sumatra.bot.State;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.DrawableTube;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.tube.ITube;
import edu.tigers.sumatra.math.tube.Tube;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.math.vector.Vector3f;
import edu.tigers.sumatra.vision.data.FilteredVisionBot;
import edu.tigers.sumatra.vision.data.FilteredVisionFrame;
import edu.tigers.sumatra.vision.data.VirtualBall;
import edu.tigers.sumatra.vision.data.VirtualBallCandidate;
import lombok.Getter;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Used by {@link CamFilter} to create "virtual" balls from non-vision sources.
 * E.g. robot barrier or onboard camera.
 */
public class VirtualBallProducer
{
	private IVector2 lastKnownBallPosition = Vector2f.ZERO_VECTOR;
	private final Map<BotID, Long> lastBarrierInterruptedMap = new HashMap<>();
	@Getter
	private List<VirtualBall> virtualBalls = new CopyOnWriteArrayList<>();

	private List<ITube> shadows = new ArrayList<>();

	@Configurable(defValue = "1000.0", comment = "Max. distance to last know ball location to create virtual balls")
	private static double maxDistanceToLastKnownPos = 1000.0;

	@Configurable(defValue = "500.0", comment = "Max. distance of ball to observing robot")
	private static double maxDistanceToObserver = 500.0;

	@Configurable(defValue = "0.05", comment = "Time in [s] after which virtual balls are generated from robot info")
	private static double delayToVirtualBalls = 0.05;

	@Configurable(defValue = "10.0", comment = "Expand shadows by this amount in [mm] in all directions")
	private static double shadowMargin = 10.0;

	@Configurable(defValue = "true", comment = "Use virtual balls which are detected in camera shadows.")
	private static boolean useBallsInShadows = true;

	@Configurable(defValue = "true", comment = "Use virtual balls which are close to the observer.")
	private static boolean useBallsNearby = true;

	@Configurable(defValue = "false", comment = "Always add virtual balls, even if there are balls detected by vision.")
	private static boolean alwaysAddVirtualBalls = false;

	@Configurable(defValue = "0.5", comment = "How long to treat barrier as interrupted after loosing contact in [s]. Only applies if not on bot cam")
	private static double keepBarrierInterruptedTime = 0.5;

	static
	{
		ConfigRegistration.registerClass("vision", VirtualBallProducer.class);
	}

	public void update(final FilteredVisionFrame frame, final Map<BotID, RobotInfo> robotInfoMap,
			final Collection<CamFilter> cams)
	{
		long timestamp = frame.getTimestamp();

		virtualBalls.clear();
		lastKnownBallPosition = frame.getBall().getPos().getXYVector();

		shadows = frame.getBots().stream()
				.flatMap(bot -> cams.stream()
						.filter(cam -> cam.getValidRobots().containsKey(bot.getBotID()))
						.map(cam -> computeBotShadow(cam.getCameraPosition().orElse(null), bot, robotInfoMap.get(bot.getBotID())))
						.flatMap(Optional::stream))
				.toList();

		boolean ballsVisibleOnCam = cams.stream()
				.anyMatch(c -> (timestamp - c.getLastBallOnCamTimestamp()) * 1e-9 < delayToVirtualBalls);

		if (ballsVisibleOnCam && !alwaysAddVirtualBalls)
			return;

		var ballCandidates = frame.getBots().stream()
				.map(b -> getBallCandidate(timestamp, b, robotInfoMap.get(b.getBotID())))
				.flatMap(Optional::stream)
				.filter(this::isCandidateValid)
				.toList();

		if (ballCandidates.isEmpty())
			return;

		var meanPos = ballCandidates.stream().map(VirtualBallCandidate::getBallPosition)
				.reduce(Vector3f.zero(), IVector3::addNew)
				.multiplyNew(1.0 / ballCandidates.size());

		virtualBalls.add(new VirtualBall(timestamp, meanPos, ballCandidates));
	}

	private Optional<ITube> computeBotShadow(IVector3 cameraPos, FilteredVisionBot bot, RobotInfo info)
	{
		if(cameraPos == null || cameraPos.z() <= 150)
			return Optional.empty();

		IVector2 botPos = Optional.ofNullable(info)
				.map(RobotInfo::getInternalState)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.map(State::getPos)
				.orElse(bot.getPos());

		// compute shadow area
		IVector2 farBotEdge = LineMath.stepAlongLine(botPos, cameraPos.getXYVector(), -Geometry.getBotRadius());
		IVector2 projectedFarEdge = Vector3.from2d(farBotEdge, 150).projectToGroundNew(cameraPos);
		IVector2 projectedCenter = Vector3.from2d(botPos, 150).projectToGroundNew(cameraPos);

		double projectedRadius = projectedFarEdge.distanceTo(projectedCenter);

		return Optional.of(Tube.create(botPos, projectedCenter, projectedRadius + shadowMargin));
	}


	private boolean isCandidateValid(VirtualBallCandidate candidate)
	{
		boolean isInShadow = shadows.stream().anyMatch(s -> s.isPointInShape(candidate.getBallPosition().getXYVector()));
		boolean isCloseToObserver = candidate.getBallPosition().getXYVector().distanceTo(candidate.getObservedFromPosition().getXYVector()) < maxDistanceToObserver;
		boolean isCloseToLastKnownPosition = lastKnownBallPosition.distanceTo(candidate.getBallPosition().getXYVector()) < maxDistanceToLastKnownPos;

		if (!isCloseToLastKnownPosition)
			return false;

		return (isInShadow && useBallsInShadows) || (isCloseToObserver && useBallsNearby);
	}


	private Optional<VirtualBallCandidate> getBallCandidate(final long timestamp, final FilteredVisionBot bot,
			final RobotInfo info)
	{
		if(info == null)
			return Optional.empty();

		boolean hasOnboardBallInfo = info.isBarrierInterrupted() || info.getBallState().isPresent();
		if (!hasOnboardBallInfo)
			return Optional.empty();

		// FilteredVisionBot data is from a past frame, need to extrapolate to current timestamp
		FilteredVisionBot extrapolatedBot = bot.extrapolate(bot.getTimestamp(), timestamp);
		IVector2 curBotPos = extrapolatedBot.getPos();
		double curBotOrient = extrapolatedBot.getOrientation();

		IVector2 centerDribblerPos = curBotPos.addNew(Vector2.fromAngle(curBotOrient).scaleTo(info.getCenter2DribblerDist()));
		IVector2 ballAtDribblerPos = curBotPos.addNew(Vector2.fromAngle(curBotOrient).scaleTo(info.getCenter2DribblerDist() + Geometry.getBallRadius()));

		if (info.isBarrierInterrupted())
		{
			lastBarrierInterruptedMap.put(info.getBotId(), info.getTimestamp());
		}

		boolean barrierInterrupted = Math.abs(timestamp - lastBarrierInterruptedMap.getOrDefault(info.getBotId(), 0L)) / 1e9 < keepBarrierInterruptedTime;

		if (barrierInterrupted && info.getBallState().isEmpty())
			return Optional.of(new VirtualBallCandidate(bot, centerDribblerPos, Vector3.from2d(ballAtDribblerPos, 0)));

		return info.getBallState().map(s -> new VirtualBallCandidate(bot, centerDribblerPos, Vector3.from2d(s.getPos(), 0)));
	}


	public List<IDrawableShape> getVirtualBallShapes()
	{
		List<IDrawableShape> shapes = new ArrayList<>();

		for (var ball : virtualBalls)
		{
			DrawableCircle ballPos = new DrawableCircle(ball.getPosition().getXYVector(), 20, Color.ORANGE.darker());
			shapes.add(ballPos);

			for (var candidates : ball.getUsedCandidates())
			{
				DrawableLine toBall = new DrawableLine(candidates.getObservedFromPosition(), candidates.getBallPosition().getXYVector(), Color.GRAY);
				shapes.add(toBall);

				DrawableCircle ballCandidatePos = new DrawableCircle(candidates.getBallPosition().getXYVector(), 25, Color.GRAY);
				shapes.add(ballCandidatePos);
			}
		}

		shadows.forEach(s -> shapes.add(new DrawableTube(s, Color.MAGENTA)));

		return shapes;
	}
}
