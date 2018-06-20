/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.generic;

import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.referee.data.TeamInfo;


/**
 * @author Ulrike Leipscher <ulrike.leipscher@dlr.de>.
 */
public class TeamData
{
	
	private final int id;
	private String team;
	private ETeamColor teamColor;
	
	private int botCollisions = 0;
	private int botStopSpeeding = 0;
	private int yellowCards = 0;
	
	
	public TeamData(final int id)
	{
		this.id = id;
		this.team = "unknown";
		this.teamColor = ETeamColor.NEUTRAL;
	}
	
	
	public TeamData(final int id, ETeamColor color)
	{
		this.id = id;
		this.teamColor = color;
		this.team = "unknown";
	}
	
	
	public TeamData(final int id, String team, ETeamColor color)
	{
		this.id = id;
		this.team = team;
		this.teamColor = color;
	}
	
	
	public TeamData(final TeamInfo teamInfo, ETeamColor color)
	{
		id = color == ETeamColor.YELLOW ? 1 : 2;
		team = teamInfo.getName();
		teamColor = color;
	}
	
	
	public void changeTeamColor()
	{
		teamColor = teamColor.opposite();
	}
	
	
	public void botCollision()
	{
		botCollisions++;
	}
	
	
	public void botStopSpeeding()
	{
		botStopSpeeding++;
	}
	
	
	public void setBotStopSpeeding(int botStopSpeeding)
	{
		this.botStopSpeeding = botStopSpeeding;
	}
	
	
	public void addYellowCards(int yellowCards)
	{
		this.yellowCards += yellowCards;
	}
	
	
	public void removeBotCollision()
	{
		botCollisions = botCollisions > 0 ? botCollisions-- : 0;
	}
	
	
	public void removeBotStopSpeed()
	{
		botStopSpeeding = botStopSpeeding > 0 ? botStopSpeeding-- : 0;
	}
	
	
	public String getTeam()
	{
		return team;
	}
	
	
	public void setTeam(String team)
	{
		this.team = team;
	}
	
	
	public int getId()
	{
		return id;
	}
	
	
	public ETeamColor getTeamColor()
	{
		return teamColor;
	}
	
	
	public void setTeamColor(ETeamColor color)
	{
		this.teamColor = color;
	}
	
	
	public int getBotCollisions()
	{
		return botCollisions;
	}
	
	
	public int getBotStopSpeeding()
	{
		return botStopSpeeding;
	}
	
	
	public int getYellowCards()
	{
		return yellowCards;
	}
	
	
	public void setYellowCards(int yellowCards)
	{
		this.yellowCards = yellowCards;
	}
}
