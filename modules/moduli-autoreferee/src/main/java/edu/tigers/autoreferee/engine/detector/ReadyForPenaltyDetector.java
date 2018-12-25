package edu.tigers.autoreferee.engine.detector;

import java.awt.Color;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import edu.tigers.autoreferee.AutoRefUtil;
import edu.tigers.autoreferee.EAutoRefShapesLayer;
import edu.tigers.autoreferee.engine.NGeometry;
import edu.tigers.autoreferee.engine.events.IGameEvent;
import edu.tigers.autoreferee.engine.events.Prepared;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableRectangle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.Goal;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.rectangle.Rectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Check if all conditions are satisfied to continue with a penalty kick.
 */
public class ReadyForPenaltyDetector extends AGameEventDetector
{
	private static final Logger log = Logger.getLogger(ReadyForPenaltyDetector.class);
	private static final Color AREA_COLOR = new Color(0, 0, 255, 150);
	private static final double BALL_PLACEMENT_TOLERANCE = 200;
	
	private Rectangle keeperArea;
	private ETeamColor shooterTeam;
	
	private long tStart;
	private boolean eventRaised;
	
	
	public ReadyForPenaltyDetector()
	{
		super(EGameEventDetectorType.READY_FOR_PENALTY, EGameState.PREPARE_PENALTY);
	}
	
	
	@Override
	protected void doPrepare()
	{
		tStart = frame.getTimestamp();
		GameState gamestate = frame.getGameState();
		shooterTeam = gamestate.getForTeam();
		keeperArea = calcKeeperArea(shooterTeam.opposite());
		eventRaised = false;
	}
	
	
	@Override
	protected Optional<IGameEvent> doUpdate()
	{
		frame.getShapes().get(EAutoRefShapesLayer.ENGINE).add(new DrawableRectangle(keeperArea, AREA_COLOR));
		
		if (!eventRaised && ballPlaced() && allPositionsCorrect())
		{
			double timeTaken = (frame.getTimestamp() - tStart) / 1e9;
			eventRaised = true;
			return Optional.of(new Prepared(timeTaken));
		}
		return Optional.empty();
	}
	
	
	private boolean allPositionsCorrect()
	{
		return keeperPosCorrect() && kickingTeamCorrect() && defendingTeamCorrect();
	}
	
	
	private boolean ballPlaced()
	{
		return NGeometry.getPenaltyMark(shooterTeam.opposite())
				.distanceTo(getBall().getPos()) < BALL_PLACEMENT_TOLERANCE;
	}
	
	
	private boolean keeperPosCorrect()
	{
		BotID keeperID = frame.getRefereeMsg().getKeeperBotID(shooterTeam.opposite());
		ITrackedBot keeper = frame.getWorldFrame().getBots().getWithNull(keeperID);
		if (keeper == null)
		{
			log.debug("Keeper not present on the field");
			return false;
		}
		boolean keeperInsideGoal = keeperArea.isPointInShape(keeper.getPos());
		
		if (!keeperInsideGoal)
		{
			frame.getShapes().get(EAutoRefShapesLayer.ENGINE).add(
					new DrawableCircle(keeper.getPos(), Geometry.getBotRadius() * 2, Color.RED));
		}
		return keeperInsideGoal;
	}
	
	
	private boolean kickingTeamCorrect()
	{
		List<ITrackedBot> possibleKicker = frame.getWorldFrame().getBots().values().stream()
				.filter(AutoRefUtil.ColorFilter.get(shooterTeam))
				.collect(Collectors.toList());
		
		if (possibleKicker.size() > 1)
		{
			possibleKicker.forEach(bot -> frame.getShapes().get(EAutoRefShapesLayer.ENGINE).add(
					new DrawableCircle(bot.getPos(), Geometry.getBotRadius() * 2, Color.RED)));
			return false;
		}
		
		return true;
	}
	
	
	private boolean defendingTeamCorrect()
	{
		BotID keeperID = frame.getRefereeMsg().getKeeperBotID(shooterTeam.opposite());
		List<ITrackedBot> defender = frame.getWorldFrame().getBots().values().stream()
				.filter(AutoRefUtil.ColorFilter.get(shooterTeam.opposite()))
				.filter(bot -> !bot.getBotId().equals(keeperID))
				.collect(Collectors.toList());
		
		defender.forEach(bot -> frame.getShapes().get(EAutoRefShapesLayer.ENGINE).add(
				new DrawableCircle(bot.getPos(), Geometry.getBotRadius() * 2, Color.RED)));
		
		return defender.isEmpty();
	}
	
	
	private Rectangle calcKeeperArea(final ETeamColor color)
	{
		Goal goal = NGeometry.getGoal(color);
		IVector2 topLeft = goal.getLeftPost().addNew(Vector2f.fromXY(Geometry.getBotRadius(), 0));
		IVector2 bottomRight = goal.getRightPost().addNew(Vector2f.fromXY(-Geometry.getBotRadius(), 0));
		return Rectangle.fromPoints(topLeft, bottomRight);
	}
}
