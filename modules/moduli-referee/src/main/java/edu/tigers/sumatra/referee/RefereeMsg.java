/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.07.2010
 * Author(s):
 * Bernhard
 * Gunther
 * *********************************************************
 */
package edu.tigers.sumatra.referee;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.Referee.SSL_Referee;
import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.Referee.SSL_Referee.Stage;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;


/**
 * Complete referee command
 */
@Persistent
public class RefereeMsg
{
	/** in nanoseconds */
	private final long			frameTimestamp;
	private final Command		command;
	/** in microseconds */
	private final long			cmdTimestamp;
	private final long			cmdCounter;
	/** in microseconds */
	private final long			packetTimestamp;
	private final Stage			stage;
	private final long			stageTimeLeft;
	private final TeamInfo		teamInfoYellow;
	private final TeamInfo		teamInfoBlue;
										
	private final ETeamColor	leftTeam;
	private final IVector2		ballPlacementPos;
										
										
	/**
	 * Create default referee msg
	 */
	public RefereeMsg()
	{
		frameTimestamp = 0;
		command = Command.HALT;
		cmdTimestamp = 0;
		cmdCounter = -1;
		packetTimestamp = 0;
		stage = Stage.NORMAL_FIRST_HALF;
		stageTimeLeft = 0;
		teamInfoYellow = new TeamInfo();
		teamInfoBlue = new TeamInfo();
		leftTeam = TeamConfig.getLeftTeam();
		ballPlacementPos = new Vector2();
	}
	
	
	/**
	 * @param frameTimestamp
	 * @param sslRefereeMsg
	 */
	public RefereeMsg(final long frameTimestamp, final SSL_Referee sslRefereeMsg)
	{
		this.frameTimestamp = frameTimestamp;
		command = sslRefereeMsg.getCommand();
		cmdTimestamp = sslRefereeMsg.getCommandTimestamp();
		cmdCounter = sslRefereeMsg.getCommandCounter();
		packetTimestamp = sslRefereeMsg.getPacketTimestamp();
		stage = sslRefereeMsg.getStage();
		stageTimeLeft = sslRefereeMsg.getStageTimeLeft();
		
		teamInfoYellow = new TeamInfo(sslRefereeMsg.getYellow());
		teamInfoBlue = new TeamInfo(sslRefereeMsg.getBlue());
		
		if (sslRefereeMsg.hasDesignatedPosition())
		{
			SSL_Referee.Point msgBallPos = sslRefereeMsg.getDesignatedPosition();
			ballPlacementPos = new Vector2(msgBallPos.getX(), msgBallPos.getY());
		} else
		{
			ballPlacementPos = new Vector2();
		}
		
		leftTeam = TeamConfig.getLeftTeam();
	}
	
	
	/**
	 * @param refereeMsg
	 */
	public RefereeMsg(final RefereeMsg refereeMsg)
	{
		frameTimestamp = refereeMsg.getFrameTimestamp();
		command = refereeMsg.command;
		cmdTimestamp = refereeMsg.cmdTimestamp;
		cmdCounter = refereeMsg.cmdCounter;
		packetTimestamp = refereeMsg.packetTimestamp;
		stage = refereeMsg.stage;
		stageTimeLeft = refereeMsg.stageTimeLeft;
		teamInfoYellow = refereeMsg.teamInfoYellow;
		teamInfoBlue = refereeMsg.teamInfoBlue;
		leftTeam = refereeMsg.leftTeam;
		ballPlacementPos = refereeMsg.getBallPlacementPos();
	}
	
	
	/**
	 * @return
	 */
	public final Command getCommand()
	{
		return command;
	}
	
	
	/**
	 * The command timestamp value from the protobuf message
	 * 
	 * @return UNIX timestamp in microseconds
	 */
	public final long getCommandTimestamp()
	{
		return cmdTimestamp;
	}
	
	
	/**
	 * @return
	 */
	public final long getCommandCounter()
	{
		return cmdCounter;
	}
	
	
	/**
	 * The packet timestamp value from the protobuf message
	 * 
	 * @return UNIX timestamp in microseconds
	 */
	public final long getPacketTimestamp()
	{
		return packetTimestamp;
	}
	
	
	/**
	 * @return
	 */
	public final Stage getStage()
	{
		return stage;
	}
	
	
	/**
	 * @return
	 */
	public final long getStageTimeLeft()
	{
		return stageTimeLeft;
	}
	
	
	/**
	 * @return
	 */
	public final TeamInfo getTeamInfoBlue()
	{
		return teamInfoBlue;
	}
	
	
	/**
	 * @return
	 */
	public final TeamInfo getTeamInfoYellow()
	{
		return teamInfoYellow;
	}
	
	
	/**
	 * @return the leftTeam
	 */
	public final ETeamColor getLeftTeam()
	{
		return leftTeam;
	}
	
	
	/**
	 * The position that the ball is to be placed at by the designated team
	 * 
	 * @return the ballPlacementPos
	 */
	public IVector2 getBallPlacementPos()
	{
		return ballPlacementPos;
	}
	
	
	/**
	 * The timestamp of the last world frame when this message was received
	 * 
	 * @return the frameTimestamp in nanoseconds
	 */
	public final long getFrameTimestamp()
	{
		return frameTimestamp;
	}
}
