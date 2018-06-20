/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.calc;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import edu.tigers.autoreferee.AutoRefFrame;
import edu.tigers.autoreferee.generic.TeamData;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.referee.data.TeamInfo;


/**
 * @author Ulrike Leipscher <ulrike.leipscher@dlr.de>.
 */
public class TeamInfoCalc implements IRefereeCalc
{
	
	@Override
	public void process(final AutoRefFrame frame)
	{
		Set<TeamData> lastTeamInfo = frame.getPreviousFrame().getTeamInfo();
		if (lastTeamInfo.isEmpty())
		{
			Set<TeamData> teams = new HashSet<>();
			teams.add(new TeamData(frame.getRefereeMsg().getTeamInfoYellow(), ETeamColor.YELLOW));
			teams.add(new TeamData(frame.getRefereeMsg().getTeamInfoBlue(), ETeamColor.BLUE));
			frame.setTeamInfo(teams);
			return;
		}
		
		boolean yellowSet = false;
		int yellowID = 0;
		boolean blueSet = false;
		int blueID = 0;
		TeamInfo yellowTeam = frame.getRefereeMsg().getTeamInfoYellow();
		TeamInfo blueTeam = frame.getRefereeMsg().getTeamInfoBlue();
		Set<TeamData> currentTeamInfo = new HashSet<>();
		
		if (!"".equals(yellowTeam.getName()))
		{
			List<TeamData> data = lastTeamInfo.stream()
					.filter(teamData -> teamData.getTeam().equals(yellowTeam.getName())).collect(Collectors.toList());
			if (data.size() == 1)
			{
				currentTeamInfo.add(updateTeamData(data.get(0), yellowTeam, ETeamColor.YELLOW));
				yellowSet = true;
				yellowID = data.get(0).getId();
			}
		}
		
		if (!"".equals(blueTeam.getName()))
		{
			List<TeamData> data = lastTeamInfo.stream()
					.filter(teamData -> teamData.getTeam().equals(blueTeam.getName())).collect(Collectors.toList());
			if (data.size() == 1)
			{
				currentTeamInfo.add(updateTeamData(data.get(0), blueTeam, ETeamColor.BLUE));
				blueSet = true;
				blueID = data.get(0).getId();
			}
		}
		
		if (!yellowSet)
		{
			if (blueSet)
			{
				final int finalBlueID = blueID;
				List<TeamData> yellowData = lastTeamInfo.stream()
						.filter(teamData -> teamData.getId() != finalBlueID)
						.collect(Collectors.toList());
				currentTeamInfo.add(updateTeamData(yellowData.get(0), yellowTeam, ETeamColor.YELLOW));
				yellowID = yellowData.get(0).getId();
			} else
			{
				List<TeamData> yellowData = lastTeamInfo.stream()
						.filter(teamData -> teamData.getTeamColor() == ETeamColor.YELLOW)
						.collect(Collectors.toList());
				currentTeamInfo.add(updateTeamData(yellowData.get(0), yellowTeam, ETeamColor.YELLOW));
				yellowID = yellowData.get(0).getId();
			}
		}
		
		if (!blueSet)
		{
			final int finalYellowID = yellowID;
			List<TeamData> blueData = lastTeamInfo.stream()
					.filter(teamData -> teamData.getId() != finalYellowID)
					.collect(Collectors.toList());
			currentTeamInfo.add(updateTeamData(blueData.get(0), blueTeam, ETeamColor.BLUE));
		}
		
		frame.setTeamInfo(currentTeamInfo);
	}
	
	
	private TeamData updateTeamData(TeamData teamData, TeamInfo info, ETeamColor color)
	{
		teamData.setTeam(info.getName());
		teamData.setTeamColor(color);
		return teamData;
	}
}
