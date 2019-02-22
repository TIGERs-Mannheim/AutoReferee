package edu.tigers.autoreferee.engine.detector;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.g3force.configurable.Configurable;

import edu.tigers.autoreferee.AutoRefUtil;
import edu.tigers.autoreferee.EAutoRefShapesLayer;
import edu.tigers.sumatra.drawable.DrawableTube;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.tube.ITube;
import edu.tigers.sumatra.math.tube.Tube;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.gameevent.BotInterferedPlacement;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Detect ball placement interference by the opponent team
 */
public class BallPlacementInterferenceDetector extends AGameEventDetector
{
	@Configurable(defValue = "2.0", comment = "The time [s] that a robot is allowed to stay within the forbidden area")
	private static double violationTime = 2.0;
	
	private final Map<BotID, ITrackedBot> violators = new HashMap<>();
	
	
	public BallPlacementInterferenceDetector()
	{
		super(EGameEventDetectorType.BALL_PLACEMENT_INTERFERENCE, EGameState.BALL_PLACEMENT);
	}
	
	
	@Override
	public void doReset()
	{
		violators.clear();
	}
	
	
	@Override
	protected Optional<IGameEvent> doUpdate()
	{
		Map<BotID, ITrackedBot> violatingBots = violatingBots();
		violators.keySet().removeIf(b -> !violatingBots.keySet().contains(b));
		violatingBots.forEach(violators::putIfAbsent);
		
		return violators.values().stream()
				.filter(this::keepsViolating)
				.findAny()
				.map(this::createEvent);
	}
	
	
	private Map<BotID, ITrackedBot> violatingBots()
	{
		IVector2 ballPos = frame.getWorldFrame().getBall().getPos();
		IVector2 placePos = frame.getGameState().getBallPlacementPositionNeutral();
		ITube placementTube = Tube.create(ballPos, placePos, RuleConstraints.getStopRadius() + Geometry.getBotRadius());
		ETeamColor placingTeam = frame.getGameState().getForTeam();
		
		frame.getShapes().get(EAutoRefShapesLayer.ALLOWED_DISTANCES)
				.add(new DrawableTube(placementTube.withMargin(-Geometry.getBotRadius()), Color.red));
		
		return frame.getWorldFrame().getBots().values().stream()
				.filter(AutoRefUtil.ColorFilter.get(placingTeam.opposite()))
				.filter(bot -> placementTube.isPointInShape(bot.getPos()))
				.collect(Collectors.toMap(ITrackedBot::getBotId, b -> b));
	}
	
	
	private IGameEvent createEvent(ITrackedBot bot)
	{
		// remove it from the violators such that the time is reset
		// if the bot keeps violating the distance, it get punished again after violationTime
		violators.remove(bot.getBotId());
		return new BotInterferedPlacement(bot.getBotId(), frame.getWorldFrame().getBot(bot.getBotId()).getPos());
	}
	
	
	private boolean keepsViolating(ITrackedBot bot)
	{
		return (frame.getTimestamp() - bot.getTimestamp()) / 1e9 > violationTime;
	}
}
