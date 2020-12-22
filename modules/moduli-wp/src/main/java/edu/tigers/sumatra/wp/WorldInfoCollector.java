/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.moduli.exceptions.StartModuleException;
import edu.tigers.sumatra.bot.BotState;
import edu.tigers.sumatra.bot.RobotInfo;
import edu.tigers.sumatra.bot.State;
import edu.tigers.sumatra.cam.ACam;
import edu.tigers.sumatra.cam.ICamFrameObserver;
import edu.tigers.sumatra.cam.data.CamBall;
import edu.tigers.sumatra.cam.data.CamDetectionFrame;
import edu.tigers.sumatra.data.TimestampBasedBuffer;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.drawable.ShapeMapSource;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.persistence.RecordManager;
import edu.tigers.sumatra.referee.IRefereeObserver;
import edu.tigers.sumatra.referee.Referee;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.referee.data.RefereeMsg;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage;
import edu.tigers.sumatra.referee.source.ERefereeMessageSource;
import edu.tigers.sumatra.vision.AVisionFilter;
import edu.tigers.sumatra.vision.IVisionFilterObserver;
import edu.tigers.sumatra.vision.data.FilteredVisionBall;
import edu.tigers.sumatra.vision.data.FilteredVisionBot;
import edu.tigers.sumatra.vision.data.FilteredVisionFrame;
import edu.tigers.sumatra.vision.data.IKickEvent;
import edu.tigers.sumatra.wp.data.BallContact;
import edu.tigers.sumatra.wp.data.BallKickFitState;
import edu.tigers.sumatra.wp.data.ExtendedCamDetectionFrame;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;
import edu.tigers.sumatra.wp.data.TrackedBall;
import edu.tigers.sumatra.wp.data.TrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;
import edu.tigers.sumatra.wp.util.BallContactCalculator;
import edu.tigers.sumatra.wp.util.BotStateTrajectorySync;
import edu.tigers.sumatra.wp.util.CurrentBallDetector;
import edu.tigers.sumatra.wp.util.DefaultRobotInfoProvider;
import edu.tigers.sumatra.wp.util.GameStateCalculator;
import edu.tigers.sumatra.wp.util.IRobotInfoProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * This module collects some AI-independent world information, like filtered camera data and vision data
 */
public class WorldInfoCollector extends AWorldPredictor
		implements IRefereeObserver, IVisionFilterObserver, ICamFrameObserver
{
	private static final Logger log = LogManager.getLogger(WorldInfoCollector.class.getName());

	@Configurable(comment = "Use robot feedback for position and velocity.", defValue = "true")
	private static boolean preferRobotFeedback = true;
	@Configurable(comment = "Add a faked ball. Set pos,vel,acc in code.", defValue = "false")
	private static boolean fakeBall = false;

	private final BerkeleyAutoPauseHook berkeleyAutoPauseHook = new BerkeleyAutoPauseHook();
	private GameStateCalculator gameStateCalculator;
	private WorldFrameVisualization worldFrameVisualization;
	private BallContactCalculator ballContactCalculator;
	private CurrentBallDetector currentBallDetector;
	private Map<BotID, BotStateTrajectorySync> botStateFromTraj = new HashMap<>();
	private AVisionFilter visionFilter;
	private IRobotInfoProvider robotInfoProvider = new DefaultRobotInfoProvider();
	private Referee referee;
	private CiGameControllerConnector ciGameControllerConnector;

	private long lastWFTimestamp;
	private RefereeMsg latestRefereeMsg;
	private IKickEvent lastKickEvent;
	private final TimestampBasedBuffer<ITrackedBall> ballBuffer = new TimestampBasedBuffer<>(0.3);

	static
	{
		ConfigRegistration.registerClass("wp", WorldInfoCollector.class);
	}


	private Map<BotID, BotState> getFilteredBotStates(final Collection<FilteredVisionBot> visionBots)
	{
		return visionBots.stream()
				.collect(Collectors.toMap(
						FilteredVisionBot::getBotID,
						FilteredVisionBot::toBotState));
	}


	private Map<BotID, FilteredVisionBot> getFilteredBots(final Collection<FilteredVisionBot> visionBots)
	{
		return visionBots.stream()
				.collect(Collectors.toMap(
						FilteredVisionBot::getBotID,
						Function.identity()));
	}


	private Map<BotID, BotState> getInternalBotStates(final Collection<RobotInfo> robotInfo)
	{
		return robotInfo.stream()
				.map(ri -> ri.getInternalState().map(s -> estimate(s, ri.getTimestamp())))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toMap(
						BotState::getBotID,
						Function.identity()));
	}


	private BotState estimate(BotState state, long timestamp)
	{
		double dt = (lastWFTimestamp - timestamp) / 1e9;
		IVector2 pos = state.getPos().addNew(state.getVel2().multiplyNew(1000 * dt));
		double orientation = AngleMath.normalizeAngle(state.getOrientation() + state.getAngularVel() * dt);
		Pose pose = Pose.from(pos, orientation);
		return BotState.of(state.getBotID(), State.of(pose, state.getVel3()));
	}


	private boolean useInternalState(BotState filterState, BotState internalState)
	{
		if (filterState == null)
		{
			return true;
		} else if (internalState == null)
		{
			return false;
		}
		return preferRobotFeedback;
	}


	private BallContact getLastBallContact(final RobotInfo robotInfo, final Pose pose)
	{
		return ballContactCalculator.ballContact(robotInfo, pose, robotInfo.getCenter2DribblerDist());
	}


	private TrackedBot createTrackedBot(final RobotInfo robotInfo,
			final Map<BotID, BotState> filteredBotStates,
			final BotState filterState,
			final BotState internalState,
			final FilteredVisionBot filteredVisionBot)
	{
		boolean useInternalState = useInternalState(filterState, internalState);
		BotState currentBotState = useInternalState ? internalState : filterState;
		if (currentBotState == null)
		{
			return null;
		}
		BotStateTrajectorySync sync = botStateFromTraj.computeIfAbsent(robotInfo.getBotId(),
				b -> new BotStateTrajectorySync());
		if (botCollidingWithOtherBot(filteredBotStates, currentBotState))
		{
			sync.reset();
		} else
		{
			robotInfo.getTrajectory().ifPresentOrElse(t -> sync.add(t, lastWFTimestamp), sync::reset);
		}

		var feedbackDelay = useInternalState ? 0.0 : Geometry.getFeedbackDelay();
		var trackedState = sync.updateState(lastWFTimestamp, feedbackDelay, currentBotState);

		Optional<State> bufferedState = sync.getLatestState();
		BotState botState = BotState.of(
				robotInfo.getBotId(),
				bufferedState.map(s -> State.of(Pose.from(s.getPos(), currentBotState.getOrientation()),
						Vector3.from2d(s.getVel2(), currentBotState.getAngularVel())))
						.orElse(currentBotState));

		return TrackedBot.newBuilder()
				.withBotId(botState.getBotID())
				.withTimestamp(lastWFTimestamp)
				.withState(botState)
				.withFilteredState(filterState)
				.withBufferedTrajState(trackedState)
				.withTrackingQuality(sync.getTrajTrackingQuality())
				.withBotInfo(robotInfo)
				.withLastBallContact(getLastBallContact(robotInfo, botState.getPose()))
				.withQuality(filteredVisionBot != null ? filteredVisionBot.getQuality() : 0)
				.build();
	}


	private boolean botCollidingWithOtherBot(final Map<BotID, BotState> filteredBotStates, final BotState trajState)
	{
		return filteredBotStates.values().stream()
				.filter(b -> !b.getBotID().equals(trajState.getBotID()))
				.anyMatch(s -> s.getPos().distanceTo(trajState.getPos()) < Geometry.getBotRadius() * 2 + 10);
	}


	private Map<BotID, ITrackedBot> collectTrackedBots(
			final List<FilteredVisionBot> filteredVisionBots,
			final Collection<RobotInfo> robotInfo)
	{
		Map<BotID, BotState> filteredBotStates = getFilteredBotStates(filteredVisionBots);
		Map<BotID, BotState> internalBotStates = getInternalBotStates(robotInfo);
		Map<BotID, FilteredVisionBot> filteredVisionBotMap = getFilteredBots(filteredVisionBots);

		Map<BotID, ITrackedBot> trackedBots = robotInfo.stream()
				.map(r -> createTrackedBot(r, filteredBotStates, filteredBotStates.get(r.getBotId()),
						internalBotStates.get(r.getBotId()),
						filteredVisionBotMap.get(r.getBotId())))
				.filter(Objects::nonNull)
				.collect(Collectors.toMap(ITrackedBot::getBotId, Function.identity()));
		return new IdentityHashMap<>(trackedBots);
	}


	private Map<BotID, RobotInfo> collectRobotInfo(final List<FilteredVisionBot> filteredVisionBots)
	{
		Set<BotID> allRelevantBots = new HashSet<>(robotInfoProvider.getConnectedBotIds());
		filteredVisionBots.stream().map(FilteredVisionBot::getBotID).forEach(allRelevantBots::add);

		return allRelevantBots.stream()
				.map(robotInfoProvider::getRobotInfo)
				.collect(Collectors.toMap(
						RobotInfo::getBotId,
						Function.identity()));
	}


	private IKickEvent getKickEvent(final FilteredVisionFrame filteredVisionFrame)
	{
		final Optional<IKickEvent> kickEvent = filteredVisionFrame.getKickEvent();
		if (kickEvent.isPresent())
		{
			lastKickEvent = kickEvent.get();
		} else if (ballBuffer.getData().stream().allMatch(b -> b.getVel().getLength2() < 0.1))
		{
			lastKickEvent = null;
		}
		return lastKickEvent;
	}


	private void visualize(final WorldFrameWrapper wfw)
	{
		ShapeMap shapeMap = new ShapeMap();
		worldFrameVisualization.process(wfw, shapeMap);
		notifyNewShapeMap(lastWFTimestamp, shapeMap, ShapeMapSource.of("World Frame"));
	}


	private BallKickFitState getKickFitState(final FilteredVisionFrame filteredVisionFrame)
	{
		return filteredVisionFrame.getKickFitState()
				.map(f -> f.getTrajectory(filteredVisionFrame.getTimestamp()))
				.map(t -> new BallKickFitState(t.getKickPos(), t.getKickVel(), t.getKickTimestamp()))
				.orElse(null);

	}


	private void processFilteredVisionFrame(final FilteredVisionFrame filteredVisionFrame)
	{
		lastWFTimestamp = filteredVisionFrame.getTimestamp();
		robotInfoProvider.setLastWFTimestamp(lastWFTimestamp);

		ballContactCalculator.setBallPos(filteredVisionFrame.getBall().getPos().getXYVector());

		Map<BotID, RobotInfo> robotInfo = collectRobotInfo(filteredVisionFrame.getBots());
		visionFilter.setRobotInfoMap(robotInfo);

		Map<BotID, ITrackedBot> bots = collectTrackedBots(filteredVisionFrame.getBots(), robotInfo.values());

		ITrackedBall ball = getTrackedBall(filteredVisionFrame);
		ballBuffer.add(ball);

		IKickEvent kickEvent = getKickEvent(filteredVisionFrame);
		BallKickFitState kickFitState = getKickFitState(filteredVisionFrame);

		long frameNumber = filteredVisionFrame.getId();
		SimpleWorldFrame swf = new SimpleWorldFrame(frameNumber, lastWFTimestamp, bots, ball, kickEvent, kickFitState);

		if (ciGameControllerConnector != null)
		{
			ciGameControllerConnector.process(swf, referee.flushChanges()).forEach(referee::onNewRefereeMessage);
		}

		GameState gameState = gameStateCalculator.getNextGameState(latestRefereeMsg, ball.getPos());

		WorldFrameWrapper wfw = new WorldFrameWrapper(swf, latestRefereeMsg, gameState);
		consumers.forEach(c -> c.onNewWorldFrame(wfw));
		observers.forEach(c -> c.onNewWorldFrame(wfw));

		visualize(wfw);
	}


	private ITrackedBall getTrackedBall(final FilteredVisionFrame filteredVisionFrame)
	{
		FilteredVisionBall filteredVisionBall = fakeBall ? fakeBall() : filteredVisionFrame.getBall();
		return TrackedBall.fromFilteredVisionBall(lastWFTimestamp, filteredVisionBall);
	}


	private FilteredVisionBall fakeBall()
	{
		return FilteredVisionBall.builder()
				.withTimestamp(lastWFTimestamp)
				.withBallTrajectoryState(edu.tigers.sumatra.vision.data.BallTrajectoryState.builder()
						.withPos(Vector3.fromXYZ(1500, 500, 0))
						.withVel(Vector3.from2d(Vector2.fromXY(-1500, -900).scaleToNew(2000), 0))
						.withAcc(Vector3.zero())
						.build())
				.withLastVisibleTimestamp(lastWFTimestamp)
				.build();
	}


	@Override
	public final void initModule()
	{
		Geometry.refresh();
		clearObservers();

		reset();

		registerToVisionFilterModule();
		registerToRefereeModule();
		registerToCamModule();
		registerToRecordManagerModule();

		ShapeMap.setPersistDebugShapes(!SumatraModel.getInstance().isProductive());
	}


	@Override
	public final void deinitModule()
	{
		// nothing to do
	}


	@Override
	public void startModule() throws StartModuleException
	{
		super.startModule();

		if (referee.getActiveSource().getType() == ERefereeMessageSource.CI)
		{
			int port = getSubnodeConfiguration().getInt("ci-port", 11009);
			ciGameControllerConnector = new CiGameControllerConnector(port);
			ciGameControllerConnector.start();
		}
	}


	@Override
	public final void stopModule()
	{
		unregisterFromVisionFilterModule();
		unregisterFromRefereeModule();
		unregisterFromCamModule();
		unregisterToRecordManagerModule();

		if (ciGameControllerConnector != null)
		{
			ciGameControllerConnector.stop();
			ciGameControllerConnector = null;
		}
	}


	private void registerToRecordManagerModule()
	{
		if (SumatraModel.getInstance().isModuleLoaded(RecordManager.class))
		{
			RecordManager recordManager = SumatraModel.getInstance().getModule(RecordManager.class);
			recordManager.addHook(berkeleyAutoPauseHook);
		}
	}


	private void unregisterToRecordManagerModule()
	{
		if (SumatraModel.getInstance().isModuleLoaded(RecordManager.class))
		{
			RecordManager recordManager = SumatraModel.getInstance().getModule(RecordManager.class);
			recordManager.removeHook(berkeleyAutoPauseHook);
		}
	}


	private void registerToVisionFilterModule()
	{
		visionFilter = SumatraModel.getInstance().getModule(AVisionFilter.class);
		visionFilter.addObserver(this);
	}


	private void unregisterFromVisionFilterModule()
	{
		if (visionFilter != null)
		{
			visionFilter.removeObserver(this);
		}
	}


	private void registerToRefereeModule()
	{
		referee = SumatraModel.getInstance().getModule(Referee.class);
		referee.addObserver(this);
	}


	private void unregisterFromRefereeModule()
	{
		referee.removeObserver(this);
	}


	private void registerToCamModule()
	{
		if (!SumatraModel.getInstance().isSimulation())
		{
			ACam cam = SumatraModel.getInstance().getModule(ACam.class);
			cam.addObserver(this);
		}
	}


	private void unregisterFromCamModule()
	{
		if (!SumatraModel.getInstance().isSimulation())
		{
			ACam cam = SumatraModel.getInstance().getModule(ACam.class);
			cam.removeObserver(this);
		}
	}


	@Override
	public void reset()
	{
		gameStateCalculator = new GameStateCalculator();
		worldFrameVisualization = new WorldFrameVisualization();
		ballContactCalculator = new BallContactCalculator();
		currentBallDetector = new CurrentBallDetector();
		botStateFromTraj.clear();
		lastWFTimestamp = 0;
		latestRefereeMsg = new RefereeMsg();
		lastKickEvent = null;
	}


	private void clearObservers()
	{
		if (!observers.isEmpty())
		{
			log.warn("There were observers left: {}", observers);
			observers.clear();
		}
		if (!consumers.isEmpty())
		{
			log.warn("There were consumers left: {}", consumers);
			consumers.clear();
		}
	}


	@Override
	public void onNewFilteredVisionFrame(final FilteredVisionFrame filteredVisionFrame)
	{
		processFilteredVisionFrame(filteredVisionFrame);
		notifyNewShapeMap(lastWFTimestamp, filteredVisionFrame.getShapeMap(), ShapeMapSource.of("Vision Filter"));
	}


	@Override
	public void onNewRefereeMsg(final SslGcRefereeMessage.Referee refMsg)
	{
		long ts = lastWFTimestamp;
		if (refMsg.getCommandCounter() == latestRefereeMsg.getCmdCounter())
		{
			ts = latestRefereeMsg.getFrameTimestamp();
		}
		latestRefereeMsg = new RefereeMsg(ts, refMsg);
		updateTeamOnPositiveHalf(latestRefereeMsg);
	}


	private void updateTeamOnPositiveHalf(final RefereeMsg refMsg)
	{
		if (refMsg.getNegativeHalfTeam().isNonNeutral())
		{
			Geometry.setNegativeHalfTeam(refMsg.getNegativeHalfTeam());
		}
	}


	@Override
	public void onNewCamDetectionFrame(final CamDetectionFrame camDetectionFrame)
	{
		CamBall ball = currentBallDetector.findCurrentBall(camDetectionFrame.getBalls());
		ExtendedCamDetectionFrame eFrame = new ExtendedCamDetectionFrame(camDetectionFrame, ball);
		observers.forEach(o -> o.onNewCamDetectionFrame(eFrame));
	}


	@Override
	public void onClearCamFrame()
	{
		observers.forEach(IWorldFrameObserver::onClearCamDetectionFrame);
		lastWFTimestamp = 0;
		currentBallDetector.reset();
		gameStateCalculator.reset();
		worldFrameVisualization.reset();
		latestRefereeMsg = new RefereeMsg();
	}


	@Override
	public void setRobotInfoProvider(final IRobotInfoProvider robotInfoProvider)
	{
		this.robotInfoProvider = robotInfoProvider;
	}
}
