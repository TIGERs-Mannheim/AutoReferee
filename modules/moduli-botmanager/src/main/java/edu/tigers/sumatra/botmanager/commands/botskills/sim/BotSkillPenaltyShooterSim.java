/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.botmanager.commands.botskills.sim;

import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillPenaltyShooter;
import edu.tigers.sumatra.botmanager.commands.botskills.data.BotSkillInput;
import edu.tigers.sumatra.botmanager.commands.botskills.data.BotSkillOutput;
import edu.tigers.sumatra.botmanager.commands.botskills.data.EDriveMode;
import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.botmanager.commands.other.EKickerMode;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.math.vector.VectorN;


/**
 * @author arne
 */
public class BotSkillPenaltyShooterSim implements IBotSkillSim
{
	
	private double										currentAngleDifference	= 0;
	private long										startTime					= 0;
	private BotSkillPenaltyShooter				penaltyShooter;
	
	private EBotSkillPenaltyShooterSimState	state;
	
	private enum EBotSkillPenaltyShooterSimState
	{
		SLOW_MOVE,
		THREATENING_REST,
		TURN,
		KICK,
		FINISHED
	}
	
	
	@Override
	public BotSkillOutput execute(final BotSkillInput input)
	{
		BotSkillOutput.Builder outputBuilder = BotSkillOutput.Builder.create();
		outputBuilder.kickDevice(EKickerDevice.STRAIGHT);
		outputBuilder.kickMode(EKickerMode.NONE);
		outputBuilder.driveLimits(penaltyShooter.getMoveConstraints());
		
		switch (state)
		{
			case SLOW_MOVE:
				performSlowMoveState(input, outputBuilder);
				break;
			case THREATENING_REST:
				performThreateningRestState(input, outputBuilder);
				break;
			case TURN:
				performTurnState(input, outputBuilder);
				break;
			case KICK:
				performKick(outputBuilder);
				break;
			case FINISHED:
				break;
		}
		return outputBuilder.build();
	}
	
	
	private void performTurnState(final BotSkillInput input, final BotSkillOutput.Builder outputBuilder)
	{
		outputBuilder.modeXY(EDriveMode.LOCAL_VEL);
		outputBuilder.modeW(EDriveMode.LOCAL_VEL);
		outputBuilder.targetVelLocal(
				Vector3.fromXYZ(0, penaltyShooter.getTranslationalPushInTurn(), penaltyShooter.getRotationSpeed()));
		double nextAngleDifference = AngleMath
				.normalizeAngle(penaltyShooter.getTargetAngle() - input.getCurPos().z());
		if (nextAngleDifference * currentAngleDifference < .0)
		{
			state = EBotSkillPenaltyShooterSimState.KICK;
		}
		currentAngleDifference = nextAngleDifference;
	}
	
	
	private void performThreateningRestState(final BotSkillInput input, final BotSkillOutput.Builder outputBuilder)
	{
		outputBuilder.modeXY(EDriveMode.WHEEL_VEL);
		outputBuilder.modeW(EDriveMode.WHEEL_VEL);
		outputBuilder.targetWheelVel(VectorN.from(0, 0, 0, 0));
		if (input.gettNow() - startTime > penaltyShooter.getTimeToShoot())
		{
			state = EBotSkillPenaltyShooterSimState.TURN;
		}
	}
	
	
	private void performSlowMoveState(final BotSkillInput input, final BotSkillOutput.Builder outputBuilder)
	{
		outputBuilder.modeXY(EDriveMode.LOCAL_VEL);
		outputBuilder.modeW(EDriveMode.LOCAL_VEL);
		outputBuilder.targetVelLocal(Vector3.fromXYZ(0, penaltyShooter.getApproachSpeed(), 0));
		// TODO: use sensors from skill input, not yet available
		if (isBallReached(input))
		{
			state = EBotSkillPenaltyShooterSimState.THREATENING_REST;
			startTime = input.gettNow();
		}
	}
	
	
	/**
	 * remove as soon as sensors are available in skill input
	 */
	private boolean isBallReached(final BotSkillInput botSkillInput)
	{
		double distance = penaltyShooter.getBallPos().subtractNew(botSkillInput.getCurPos()).getLength2();
		return distance <= 87;
	}
	
	
	private void performKick(BotSkillOutput.Builder builder)
	{
		builder.modeXY(EDriveMode.OFF);
		builder.modeW(EDriveMode.OFF);
		builder.kickDevice(EKickerDevice.STRAIGHT);
		builder.kickMode(EKickerMode.FORCE);
		builder.kickSpeed(penaltyShooter.getPenaltyKickSpeed());
	}
	
	
	@Override
	public void init(final BotSkillInput input)
	{
		penaltyShooter = (BotSkillPenaltyShooter) input.getSkill();
		currentAngleDifference = AngleMath.normalizeAngle(penaltyShooter.getTargetAngle() - input.getCurPos().z());
		state = EBotSkillPenaltyShooterSimState.SLOW_MOVE;
	}
}
