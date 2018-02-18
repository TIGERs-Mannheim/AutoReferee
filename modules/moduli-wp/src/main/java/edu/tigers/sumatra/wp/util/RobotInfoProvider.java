/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.util;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import edu.tigers.sumatra.bot.ERobotMode;
import edu.tigers.sumatra.bot.RobotInfo;
import edu.tigers.sumatra.botmanager.BotProvider;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.botparams.BotParamsProvider;
import edu.tigers.sumatra.botparams.EBotParamLabel;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.EAiType;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.trajectory.ITrajectory;
import edu.tigers.sumatra.trajectory.TrajectoryWithTime;
import edu.tigers.sumatra.trajectory.TrajectoryWrapper;


public class RobotInfoProvider
{
	private final BotProvider botProvider;
	private final BotParamsProvider botParamsProvider;
	private final Map<BotID, EAiType> botToAiMap = new IdentityHashMap<>();
	
	private long lastWFTimestamp = -1;
	
	
	public RobotInfoProvider(final BotProvider botProvider, BotParamsProvider botParamsProvider)
	{
		this.botProvider = botProvider;
		this.botParamsProvider = botParamsProvider;
	}
	
	
	public RobotInfo getRobotInfo(final BotID botID)
	{
		return botProvider.getBot(botID)
				.map(this::botToRobotInfo)
				.orElseGet(() -> unknownRobotInfo(botID));
	}
	
	
	private ITrajectory<IVector3> trajectoryOfBot(final ABot bot)
	{
		return bot.getCurrentTrajectory()
				.map(t -> synchronizeTrajectoryToNow(bot, t))
				.orElse(null);
	}
	
	
	private ITrajectory<IVector3> synchronizeTrajectoryToNow(final ABot bot,
			final TrajectoryWithTime<IVector3> trajectoryWithTime)
	{
		assert lastWFTimestamp > 0;
		double age = (lastWFTimestamp - trajectoryWithTime.gettStart()) / 1e9;
		ITrajectory<IVector3> traj = trajectoryWithTime.getTrajectory();
		ITrajectory<IVector3> trajectoryWrapper = new TrajectoryWrapper<>(traj, age, traj.getTotalTime());
		if (bot.getColor() != Geometry.getNegativeHalfTeam())
		{
			return trajectoryWrapper.mirrored();
		}
		return trajectoryWrapper;
	}
	
	
	private RobotInfo unknownRobotInfo(final BotID botID)
	{
		assert lastWFTimestamp > 0;
		return RobotInfo.stubBuilder(botID, lastWFTimestamp)
				.withBotParams(botParamsProvider.get(EBotParamLabel.OPPONENT))
				.build();
	}
	
	
	public Set<BotID> getConnectedBotIds()
	{
		return botProvider.getBots().values().stream()
				.filter(bot -> bot.getRobotMode() == ERobotMode.READY)
				.map(ABot::getBotId)
				.collect(Collectors.toSet());
	}
	
	
	private RobotInfo botToRobotInfo(ABot bot)
	{
		assert lastWFTimestamp > 0;
		return RobotInfo.newBuilder()
				.withTimestamp(lastWFTimestamp)
				.withAiType(botToAiMap.getOrDefault(bot.getBotId(), EAiType.PRIMARY))
				.withBotId(bot.getBotId())
				.withBarrierInterrupted(bot.isBarrierInterrupted())
				.withBattery(bot.getBatteryRelative())
				.withBotFeatures(bot.getBotFeatures())
				.withBotParams(bot.getBotParams())
				.withChip(bot.getMatchCtrl().getSkill().getDevice().equals(EKickerDevice.CHIP))
				.withDribbleRpm(bot.getMatchCtrl().getSkill().getDribbleSpeed())
				.withHardwareId(bot.getHardwareId())
				.withInternalState(bot.getSensoryState(lastWFTimestamp).orElse(null))
				.withKickerVoltage(bot.getKickerLevel())
				.withKickSpeed(bot.getMatchCtrl().getSkill().getKickSpeed())
				.withType(bot.getType())
				.withRobotMode(bot.getRobotMode())
				.withOk(bot.isOK())
				.withTrajectory(trajectoryOfBot(bot))
				.build();
	}
	
	
	public void setLastWFTimestamp(final long lastWFTimestamp)
	{
		this.lastWFTimestamp = lastWFTimestamp;
	}
	
	
	public void updateBot2AiAssignment(final BotID botID, final EAiType aiTeam)
	{
		botToAiMap.put(botID, aiTeam);
	}
	
	
	public Map<BotID, EAiType> getBotToAiMap()
	{
		return botToAiMap;
	}
}
