/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.exporter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import edu.tigers.moduli.AModule;
import edu.tigers.sumatra.MessagesRobocupSslDetectionTracked;
import edu.tigers.sumatra.MessagesRobocupSslDetectionTracked.TrackedFrame;
import edu.tigers.sumatra.MessagesRobocupSslWrapperTracked.TrackerWrapperPacket;
import edu.tigers.sumatra.cam.SSLVisionCam;
import edu.tigers.sumatra.cam.TimeSync;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.network.MulticastUDPTransmitter;
import edu.tigers.sumatra.vision.data.IKickEvent;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.BallKickFitState;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * Export standardized vision tracking data.
 */
public class VisionTrackerSender extends AModule implements IWorldFrameObserver
{
	private static final String SOURCE_NAME = "TIGERs";
	private static final Set<MessagesRobocupSslDetectionTracked.Capability> CAPABILITIES = new HashSet<>();

	static
	{
		CAPABILITIES.add(MessagesRobocupSslDetectionTracked.Capability.CAPABILITY_DETECT_FLYING_BALLS);
		CAPABILITIES.add(MessagesRobocupSslDetectionTracked.Capability.CAPABILITY_DETECT_KICKED_BALLS);
	}

	private MulticastUDPTransmitter transmitter;
	private int frameNumber = 0;
	private TimeSync timeSync;
	private String uuid = "";

	@Override
	public void startModule()
	{
		String address = getSubnodeConfiguration().getString("address", "224.5.23.2");
		int port = getSubnodeConfiguration().getInt("port", 10010);
		transmitter = new MulticastUDPTransmitter(address, port);

		uuid = UUID.randomUUID().toString();

		SumatraModel.getInstance().getModule(AWorldPredictor.class).addObserver(this);
		timeSync = SumatraModel.getInstance().getModuleOpt(SSLVisionCam.class).map(SSLVisionCam::getTimeSync)
				.orElseGet(TimeSync::new);
	}


	@Override
	public void stopModule()
	{
		SumatraModel.getInstance().getModule(AWorldPredictor.class).removeObserver(this);
		timeSync = null;
	}


	@Override
	public void onNewWorldFrame(final WorldFrameWrapper wfw)
	{
		TrackedFrame.Builder frame = TrackedFrame.newBuilder();
		frame.setFrameNumber(frameNumber++);
		frame.setTimestamp(buildTimestamp(wfw.getTimestamp()));
		frame.addBalls(buildBall(wfw.getSimpleWorldFrame().getBall()));
		frame.addAllRobots(buildRobots(wfw.getSimpleWorldFrame().getBots().values()));
		wfw.getSimpleWorldFrame().getKickFitState()
				.map(s -> buildKickEvent(wfw.getSimpleWorldFrame(), s))
				.ifPresent(frame::setKickedBall);
		frame.addAllCapabilities(CAPABILITIES);
		TrackerWrapperPacket.Builder wrapper = TrackerWrapperPacket.newBuilder();
		wrapper.setUuid(uuid);
		wrapper.setSourceName(SOURCE_NAME);
		wrapper.setTrackedFrame(frame);
		transmitter.send(wrapper.build().toByteArray());
	}


	private MessagesRobocupSslDetectionTracked.KickedBall buildKickEvent(final SimpleWorldFrame wFrame,
			final BallKickFitState ballKickFitState)
	{
		final IVector2 stopPos = wFrame.getBall().getTrajectory().getPosByVel(0.0).getXYVector();
		final double time2Stop = wFrame.getBall().getTrajectory().getTimeByPos(stopPos);
		final long stopTimestamp = wFrame.getTimestamp() + (Double.isFinite(time2Stop) ? ((long) (time2Stop * 1e9)) : 0);
		final MessagesRobocupSslDetectionTracked.KickedBall.Builder kickedBall = MessagesRobocupSslDetectionTracked.KickedBall
				.newBuilder()
				.setPos(buildVector2(ballKickFitState.getKickPos().multiplyNew(1e-3)))
				.setVel(buildVector3(ballKickFitState.getKickVel().multiplyNew(1e-3)))
				.setStartTimestamp(buildTimestamp(ballKickFitState.getKickTimestamp()))
				.setStopTimestamp(buildTimestamp(stopTimestamp))
				.setStopPos(buildVector2(stopPos.multiplyNew(1e-3)));

		wFrame.getKickEvent()
				.map(IKickEvent::getKickingBot)
				.map(this::buildRobotId)
				.ifPresent(kickedBall::setRobotId);
		return kickedBall.build();
	}


	private Iterable<? extends MessagesRobocupSslDetectionTracked.TrackedRobot> buildRobots(
			final Collection<ITrackedBot> values)
	{
		return values.stream().map(this::buildRobot).collect(Collectors.toList());
	}


	private MessagesRobocupSslDetectionTracked.TrackedRobot buildRobot(final ITrackedBot bot)
	{
		return MessagesRobocupSslDetectionTracked.TrackedRobot.newBuilder()
				.setRobotId(buildRobotId(bot.getBotId()))
				.setPos(buildVector2(bot.getPos().multiplyNew(1e-3)))
				.setOrientation((float) bot.getOrientation())
				.setVel(buildVector2(bot.getVel()))
				.setVelAngular((float) bot.getAngularVel())
				.setVisibility((float) bot.getQuality())
				.build();
	}


	private MessagesRobocupSslDetectionTracked.RobotId buildRobotId(final BotID botId)
	{
		return MessagesRobocupSslDetectionTracked.RobotId.newBuilder()
				.setId(botId.getNumber())
				.setTeamColor(buildTeamColor(botId.getTeamColor()))
				.build();
	}


	private MessagesRobocupSslDetectionTracked.TeamColor buildTeamColor(final ETeamColor teamColor)
	{
		switch (teamColor)
		{
			case YELLOW:
				return MessagesRobocupSslDetectionTracked.TeamColor.TEAM_COLOR_YELLOW;
			case BLUE:
				return MessagesRobocupSslDetectionTracked.TeamColor.TEAM_COLOR_BLUE;
			case NEUTRAL:
			default:
				return MessagesRobocupSslDetectionTracked.TeamColor.TEAM_COLOR_UNKNOWN;
		}
	}


	private MessagesRobocupSslDetectionTracked.TrackedBall buildBall(final ITrackedBall ball)
	{
		return MessagesRobocupSslDetectionTracked.TrackedBall
				.newBuilder()
				.setPos(buildVector3(ball.getPos3().multiplyNew(1e-3)))
				.setVel(buildVector3(ball.getVel3()))
				.setVisibility((float) ball.getQuality())
				.build();
	}


	private MessagesRobocupSslDetectionTracked.Vector3 buildVector3(final IVector3 pos3)
	{
		return MessagesRobocupSslDetectionTracked.Vector3.newBuilder()
				.setX((float) pos3.x())
				.setY((float) pos3.y())
				.setZ((float) pos3.z())
				.build();
	}


	private MessagesRobocupSslDetectionTracked.Vector2 buildVector2(final IVector2 pos2)
	{
		return MessagesRobocupSslDetectionTracked.Vector2.newBuilder()
				.setX((float) pos2.x())
				.setY((float) pos2.y())
				.build();
	}


	private double buildTimestamp(long timestamp)
	{
		return timeSync.reverseSync(timestamp);
	}
}
