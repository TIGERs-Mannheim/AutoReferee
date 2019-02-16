package edu.tigers.autoreferee.engine.detector;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.github.g3force.configurable.Configurable;

import edu.tigers.autoreferee.EAutoRefShapesLayer;
import edu.tigers.sumatra.drawable.DrawableArrow;
import edu.tigers.sumatra.drawable.DrawableTube;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.IPenaltyArea;
import edu.tigers.sumatra.geometry.NGeometry;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.tube.Tube;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.gameevent.BotPushedBot;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Detect if a robot pushes an opponent robot by more than 0.2m or into the opponent penalty area
 */
public class PushingDetector extends AGameEventDetector
{
	@Configurable(defValue = "10.0", comment = "Extra margin [mm] to allow between two bots such that they are detected as touching")
	private static double botExtraMargin = 10;
	
	@Configurable(defValue = "200.0", comment = "Maximum allowed distance [mm] that a bot is allowed to push an opponent bot")
	private static double maxAllowedPushDistance = 200;
	
	@Configurable(defValue = "0.7", comment = "Maximum absolute angle [rad] between pushed direction and attacker to opponent direction for counting violation")
	private static double maxPushAngle = 0.7;
	
	private List<RobotPair> firstRobotPairs = new ArrayList<>();
	
	
	public PushingDetector()
	{
		super(EGameEventDetectorType.PUSHING, EnumSet.of(EGameState.RUNNING));
	}
	
	
	@Override
	protected Optional<IGameEvent> doUpdate()
	{
		final List<RobotPair> latestRobotPairs = frame.getWorldFrame().getBots().values().stream()
				.map(this::touchingOpponents)
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
		
		firstRobotPairs = merge(firstRobotPairs, latestRobotPairs);
		if (firstRobotPairs.size() != latestRobotPairs.size())
		{
			throw new IllegalStateException(
					"Lists should have same size: " + firstRobotPairs + " vs. " + latestRobotPairs);
		}
		
		firstRobotPairs.forEach(this::drawBotPair);
		
		final List<PushedDistance> pushedDistances = latestRobotPairs.stream()
				.map(this::pushedDistance)
				.collect(Collectors.toList());
		pushedDistances.forEach(this::drawPushedDistance);
		
		final Optional<PushedDistance> ruleViolation = pushedDistances.stream()
				.filter(this::violatesRules)
				.findFirst();
		if (ruleViolation.isPresent())
		{
			final PushedDistance pd = ruleViolation.get();
			firstRobotPairs.remove(pd.firstPair);
			return Optional.of(new BotPushedBot(
					pd.firstPair.bot.getBotId(),
					pd.firstPair.opponentBot.getBotId(),
					pd.start(),
					pd.distance()));
		}
		return Optional.empty();
	}
	
	
	private boolean violatesRules(PushedDistance pushedDistance)
	{
		final Optional<Double> pushAngle = pushedDistance.pushDirection().angleToAbs(pushedDistance.moveDirection());
		if (pushAngle.isPresent() && pushAngle.get() > maxPushAngle)
		{
			return false;
		}
		
		final IPenaltyArea opponentPenArea = NGeometry
				.getPenaltyArea(pushedDistance.firstPair.opponentBot.getTeamColor());
		
		return pushedDistance.distance() > maxAllowedPushDistance
				|| opponentPenArea.withMargin(Geometry.getBotRadius()).isPointInShape(pushedDistance.end());
	}
	
	
	private void drawPushedDistance(final PushedDistance pushedDistance)
	{
		final DrawableArrow arrow = new DrawableArrow(
				pushedDistance.start(), pushedDistance.pushDirection(),
				Color.red);
		frame.getShapes().get(EAutoRefShapesLayer.PUSHING)
				.add(arrow);
	}
	
	
	private PushedDistance pushedDistance(final RobotPair latestPair)
	{
		for (RobotPair firstPair : firstRobotPairs)
		{
			if (firstPair.equals(latestPair))
			{
				return new PushedDistance(firstPair, latestPair);
			}
		}
		throw new IllegalStateException("Expected a match: " + latestPair);
	}
	
	
	private List<RobotPair> merge(List<RobotPair> oldPairs, List<RobotPair> newPairs)
	{
		// take all old
		final List<RobotPair> mergedPairs = new ArrayList<>(oldPairs);
		// but remove the vanished ones
		mergedPairs.removeIf(b -> !newPairs.contains(b));
		// and add new bots
		newPairs.stream().filter(p -> !oldPairs.contains(p)).forEach(mergedPairs::add);
		
		return mergedPairs;
	}
	
	
	private void drawBotPair(RobotPair robotPair)
	{
		double tubeRadius = Geometry.getBotRadius() + 30 + (robotPair.bot.getTeamColor() == ETeamColor.YELLOW ? 0 : 10);
		Tube tube = Tube.create(robotPair.bot.getPos(), robotPair.opponentBot.getPos(), tubeRadius);
		Color tubeColor = robotPair.bot.getTeamColor().getColor();
		frame.getShapes().get(EAutoRefShapesLayer.PUSHING).add(new DrawableTube(tube, tubeColor));
	}
	
	
	private List<RobotPair> touchingOpponents(ITrackedBot bot)
	{
		return frame.getWorldFrame().getBots().values().stream()
				.filter(b -> bot.getTeamColor() != b.getTeamColor())
				.filter(b -> touching(bot, b))
				.map(b -> new RobotPair(bot, b))
				.collect(Collectors.toList());
	}
	
	
	private boolean touching(ITrackedBot bot1, ITrackedBot bot2)
	{
		double minDist = Geometry.getBotRadius() * 2 + botExtraMargin;
		minDist *= minDist;
		return bot1.getPos().distanceToSqr(bot2.getPos()) < minDist;
	}
	
	private static class RobotPair
	{
		ITrackedBot bot;
		ITrackedBot opponentBot;
		
		
		public RobotPair(final ITrackedBot bot, final ITrackedBot opponentBot)
		{
			this.bot = bot;
			this.opponentBot = opponentBot;
		}
		
		
		public ITrackedBot getBot()
		{
			return bot;
		}
		
		
		@Override
		public boolean equals(final Object o)
		{
			if (this == o)
				return true;
			
			if (o == null || getClass() != o.getClass())
				return false;
			
			final RobotPair robotPair = (RobotPair) o;
			
			return new EqualsBuilder()
					.append(bot.getBotId(), robotPair.bot.getBotId())
					.append(opponentBot.getBotId(), robotPair.opponentBot.getBotId())
					.isEquals();
		}
		
		
		@Override
		public int hashCode()
		{
			return new HashCodeBuilder(17, 37)
					.append(bot.getBotId())
					.append(opponentBot.getBotId())
					.toHashCode();
		}
	}
	
	private static class PushedDistance
	{
		RobotPair firstPair;
		RobotPair latestPair;
		
		
		public PushedDistance(final RobotPair firstPair, final RobotPair latestPair)
		{
			this.firstPair = firstPair;
			this.latestPair = latestPair;
		}
		
		
		IVector2 start()
		{
			return firstPair.opponentBot.getPos();
		}
		
		
		IVector2 end()
		{
			return latestPair.opponentBot.getPos();
		}
		
		
		IVector2 pushDirection()
		{
			return end().subtractNew(start());
		}
		
		
		IVector2 moveDirection()
		{
			return firstPair.opponentBot.getPos().subtractNew(firstPair.bot.getPos());
		}
		
		
		double distance()
		{
			return pushDirection().getLength2();
		}
	}
}
