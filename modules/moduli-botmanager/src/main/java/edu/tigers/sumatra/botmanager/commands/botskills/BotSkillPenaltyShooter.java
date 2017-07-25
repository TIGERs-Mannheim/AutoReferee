/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 * @author: Arne
 */

package edu.tigers.sumatra.botmanager.commands.botskills;

import static java.lang.Math.abs;

import org.apache.commons.lang.Validate;

import edu.tigers.sumatra.botmanager.commands.EBotSkill;
import edu.tigers.sumatra.botmanager.commands.botskills.data.DriveLimits;
import edu.tigers.sumatra.botmanager.serial.SerialData;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;


/**
 * Penalty Shooter Bot Skill
 */
public class BotSkillPenaltyShooter extends ABotSkill
{
	
	private final int[]	ballPos						= new int[2];
	
	@SerialData(type = SerialData.ESerialDataType.INT16)
	private int				targetAngle					= 0;
	
	@SerialData(type = SerialData.ESerialDataType.UINT8)
	private int				timeToShoot					= 0;
	
	@SerialData(type = SerialData.ESerialDataType.UINT16)
	private int				approachSpeed				= 0;
	
	@SerialData(type = SerialData.ESerialDataType.UINT16)
	private int				translationalPushInTurn	= 0;
	
	@SerialData(type = SerialData.ESerialDataType.INT16)
	private int				rotationSpeed				= 0;
	
	@SerialData(type = SerialData.ESerialDataType.UINT8)
	private int				penaltyKickSpeed			= 250;
	
	@SerialData(type = SerialData.ESerialDataType.UINT8)
	private int				dribblerSpeed				= 100;
	
	@SerialData(type = SerialData.ESerialDataType.UINT8)
	private int				accMax						= 0;
	
	@SerialData(type = SerialData.ESerialDataType.UINT8)
	private int				accMaxW						= 0;
	
	@SerialData(type = SerialData.ESerialDataType.UINT8)
	private int				jerkMax						= 0;
	
	@SerialData(type = SerialData.ESerialDataType.UINT8)
	private int				jerkMaxW						= 0;
	
	
	/**
	 * Create PenaltyShooter Bot Skill
	 * 
	 * @param ballPos Position of the ball
	 * @param angle Angle to turn
	 * @param timeToShoot Time to wait to confuse opposing keeper
	 * @param approachSpeed local speed to approach the ball
	 * @param rotationSpeed rotation speed
	 * @param penaltyKickSpeed kick speed
	 * @param translationalPushInTurn additional translation velocity component when turning with ball
     * @param accMax maximum acceleration in translational direction
     * @param accMaxW maximum acceleration in rotational direction
     * @param jerkMax maximum jerk in translational direction
     * @param jerkMaxW maximum jerk in rotational direction
     * @param dribbleSpeed
	 */
	@SuppressWarnings("squid:S00107")
	public BotSkillPenaltyShooter(final IVector2 ballPos, double angle, double timeToShoot, double approachSpeed,
			double rotationSpeed, double penaltyKickSpeed, double translationalPushInTurn, double accMax, double accMaxW,
			double jerkMax, double jerkMaxW, double dribbleSpeed)
	{
		this();
		setBallPos(ballPos);
		setTimeToShoot(timeToShoot);
		setTargetAngle(angle);
		setApproachSpeed(approachSpeed);
		setRotationSpeed(rotationSpeed);
		setTranslationalPushInTurn(translationalPushInTurn);
		setPenaltyKickSpeed(penaltyKickSpeed);
		setAccMax(accMax);
		setAccMaxW(accMaxW);
		setJerkMax(jerkMax);
		setJerkMaxW(jerkMaxW);
		setDribblerSpeed(dribbleSpeed);
	}
	
	
	/**
	 * Create a new penalty shooter
	 */
	public BotSkillPenaltyShooter()
	{
		super(EBotSkill.PENALTY_SHOOTER_SKILL);
	}

	public void setRotationSpeed(final double rotationSpeed)
	{
		this.rotationSpeed = (int) (1e3 * rotationSpeed);
	}
	
	
	/**
	 * Max: 10m/s²
	 *
	 * @param val
	 */
	public final void setAccMax(final double val)
	{
		accMax = DriveLimits.toUInt8(val, DriveLimits.MAX_ACC);
	}
	
	
	/**
	 * Max: 100rad/s²
	 *
	 * @param val
	 */
	public final void setAccMaxW(final double val)
	{
		accMaxW = DriveLimits.toUInt8(val, DriveLimits.MAX_ACC_W);
	}
	
	
	/**
	 * Max: 10m/s²
	 *
	 * @param val
	 */
	public final void setJerkMax(final double val)
	{
		jerkMax = DriveLimits.toUInt8(val, DriveLimits.MAX_JERK);
	}
	
	
	/**
	 * Max: 100rad/s²
	 *
	 * @param val
	 */
	public final void setJerkMaxW(final double val)
	{
		jerkMaxW = DriveLimits.toUInt8(val, DriveLimits.MAX_JERK_W);
	}


    /**
     * Set dribbler speed in rpm
     * @param translationalPushInTurn
     */
	public void setTranslationalPushInTurn(double translationalPushInTurn)
	{
		this.translationalPushInTurn = (int) (1e3 * translationalPushInTurn);
	}
	
	
	public void setDribblerSpeed(final double dribblerSpeed)
	{
		this.dribblerSpeed = (int) (1e-3 * dribblerSpeed);
	}
	
	
	public double getTranslationalPushInTurn()
	{
		return translationalPushInTurn * 1e-3;
	}
	
	
	public IVector2 getBallPos()
	{
		return Vector2.fromXY(ballPos[0], ballPos[1]);
	}
	
	
	public void setBallPos(final IVector2 ballPos)
	{
		this.ballPos[0] = (int) ballPos.x();
		this.ballPos[1] = (int) ballPos.y();
	}
	
	
	public void setPenaltyKickSpeed(double penaltyKickSpeed)
	{
		this.penaltyKickSpeed = (int) ((penaltyKickSpeed / 0.04) + 0.5);
	}
	
	
	public double getTimeToShoot()
	{
		return timeToShoot * 0.1;
	}
	
	
	public void setTimeToShoot(final double timeToShoot)
	{
		this.timeToShoot = (int) (timeToShoot * 10);
	}
	
	
	public double getTargetAngle()
	{
		return targetAngle * 1e-3;
	}
	
	
	public void setTargetAngle(double targetAngle)
	{
		this.targetAngle = (int) (1e3 * targetAngle);
	}
	
	
	public void setApproachSpeed(double approachSpeed)
	{
		this.approachSpeed = (int) (1e3 * approachSpeed);
	}
	
	
	public double getApproachSpeed()
	{
		return approachSpeed * 1e-3;
	}
	
	
	public double getRotationSpeed()
	{
		return 1e-3 * rotationSpeed;
	}
	
	
	public double getPenaltyKickSpeed()
	{
		return penaltyKickSpeed * 0.04;
	}
	
	/**
	 * Builder for penalty shooter bot skill
	 */
	public static final class Builder
	{
		private IVector2	ballPos;
		private double		timeToShoot;
		private double		targetAngle;
		private double		approachSpeed;
		private double		rotationSpeed;
		private double		penaltyKickSpeed;
		private double		translationalVelocityOffset;
		private double		accMax	= 3.0;
		private double		accMaxW	= 50;
		private double		jerkMax	= 30;
		private double		jerkMaxW = 500;
		private double      dribbleSpeed = 1500;
		
		
		/**
		 * create a builder
		 * 
		 * @return
		 */
		public static Builder create()
		{
			return new Builder();
		}
		
		
		/**
		 * ball position
		 * 
		 * @param ballPos
		 * @return
		 */
		public Builder ballPos(final IVector2 ballPos)
		{
			this.ballPos = ballPos;
			return this;
		}


        /**
         * dribble speed
         * @param dribbleSpeed
         * @return
         */
		public Builder dribbleSpeed(final double dribbleSpeed)
        {
            this.dribbleSpeed = dribbleSpeed;
            return this;
        }
		
		
		/**
		 * time to shoot
		 * 
		 * @param timeToShoot
		 * @return
		 */
		public Builder timeToShoot(final double timeToShoot)
		{
			this.timeToShoot = timeToShoot;
			return this;
		}
		
		
		/**
		 * target angle
		 * 
		 * @param targetAngle
		 * @return
		 */
		public Builder targetAngle(final double targetAngle)
		{
			this.targetAngle = targetAngle;
			return this;
		}
		
		
		/**
		 * apprach speed
		 * 
		 * @param approachSpeed
		 * @return
		 */
		public Builder approachSpeed(final double approachSpeed)
		{
			this.approachSpeed = approachSpeed;
			return this;
		}
		
		
		/**
		 * rotation speed
		 * 
		 * @param rotationSpeed
		 * @return
		 */
		public Builder rotationSpeed(final double rotationSpeed)
		{
			this.rotationSpeed = rotationSpeed;
			return this;
		}
		
		
		/**
		 * penalty kick speed
		 * 
		 * @param penaltyKickSpeed
		 * @return
		 */
		public Builder penaltyKickSpeed(final double penaltyKickSpeed)
		{
			this.penaltyKickSpeed = penaltyKickSpeed;
			return this;
		}
		
		
		/**
		 * additional translation velocity component when turning with ball
		 * 
		 * @param velocity
		 * @return
		 */
		public Builder translationalTurnVelocityOffset(final double velocity)
		{
			this.translationalVelocityOffset = velocity;
			return this;
		}
		
		
		/**
		 * limit for translational acceleration
		 * 
		 * @param accMax
		 * @return
		 */
		public Builder accMax(double accMax)
		{
			this.accMax = accMax;
			return this;
		}
		
		
		/**
		 * limit for angular acceleration
		 * 
		 * @param accMaxW
		 * @return
		 */
		public Builder accMaxW(double accMaxW)
		{
			this.accMaxW = accMaxW;
			return this;
		}
		
		
		/**
		 * limit for translational jerk
		 * 
		 * @param jerkMax
		 * @return
		 */
		public Builder jerkMax(double jerkMax)
		{
			this.jerkMax = jerkMax;
			return this;
		}
		
		
		/**
		 * limit for rotational jerk
		 * 
		 * @param jerkMaxW
		 * @return
		 */
		public Builder jerkMaxW(double jerkMaxW)
		{
			this.jerkMaxW = jerkMaxW;
			return this;
		}
		
		
		/**
		 * validate
		 * 
		 * @return
		 */
		private void validate()
		{
			Validate.notNull(ballPos);
			Validate.isTrue(timeToShoot > 0);
			Validate.isTrue(targetAngle > 0);
			Validate.isTrue(approachSpeed > 0);
			Validate.isTrue(abs(rotationSpeed) > 0);
			Validate.isTrue(penaltyKickSpeed > 0);
		}
		
		
		/**
		 * build a new penalty shooter bot skill
		 * 
		 * @return
		 */
		public BotSkillPenaltyShooter build()
		{
			validate();
			return new BotSkillPenaltyShooter(ballPos, targetAngle, timeToShoot, approachSpeed,
					rotationSpeed, penaltyKickSpeed, translationalVelocityOffset,
					accMax, accMaxW, jerkMax, jerkMaxW, dribbleSpeed);
		}
	}
}
