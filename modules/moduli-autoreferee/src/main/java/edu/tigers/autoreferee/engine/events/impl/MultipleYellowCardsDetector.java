/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.events.impl;

import java.util.Optional;

import com.github.g3force.configurable.Configurable;

import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.NGeometry;
import edu.tigers.autoreferee.engine.events.EGameEvent;
import edu.tigers.autoreferee.engine.events.EGameEventDetectorType;
import edu.tigers.autoreferee.engine.events.GameEvent;
import edu.tigers.autoreferee.engine.events.IGameEvent;
import edu.tigers.autoreferee.generic.TeamData;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.referee.data.EGameState;


/**
 * Detect if a team has received three yellow cards in a row and give a penalty kick to the opposing team in this case
 */
public class MultipleYellowCardsDetector extends AGameEventDetector
{
	private static final int PRIORITY = 2;
	
	@Configurable(defValue = "3", comment = "Number of yellow cards until a penalty kick is given to the opposing team")
	private static int numberOfYellowCards = 3;
	
	private boolean penaltyGivenInThisStopPhase = false;
	
	
	public MultipleYellowCardsDetector()
	{
		super(EGameEventDetectorType.MULTIPLE_YELLOW_CARDS, EGameState.STOP);
	}
	
	
	@Override
	public int getPriority()
	{
		return PRIORITY;
	}
	
	
	@Override
	public Optional<IGameEvent> update(final IAutoRefFrame frame)
	{
		if (penaltyGivenInThisStopPhase)
		{
			return Optional.empty();
		}
		
		int blueCards = frame.getRefereeMsg().getTeamInfoBlue().getYellowCards()
				- frame.getTeamInfo().stream().filter(data -> data.getTeamColor() == ETeamColor.BLUE)
						.mapToInt(TeamData::getYellowCards).findFirst().orElse(0);
		int yellowCards = frame.getRefereeMsg().getTeamInfoYellow().getYellowCards()
				- frame.getTeamInfo().stream().filter(data -> data.getTeamColor() == ETeamColor.YELLOW)
						.mapToInt(TeamData::getYellowCards).findFirst().orElse(0);
		
		ETeamColor team;
		if (blueCards >= numberOfYellowCards && yellowCards >= numberOfYellowCards)
		{
			team = Math.random() < 0.5 ? ETeamColor.YELLOW : ETeamColor.BLUE;
		} else if (blueCards >= numberOfYellowCards)
		{
			team = ETeamColor.BLUE;
		} else if (yellowCards >= numberOfYellowCards)
		{
			team = ETeamColor.YELLOW;
		} else
		{
			return Optional.empty();
		}
		
		frame.getTeamInfo().stream().filter(data -> data.getTeamColor() == team)
				.forEach(data -> data.addYellowCards(numberOfYellowCards));
		penaltyGivenInThisStopPhase = true;
		
		FollowUpAction followUp = new FollowUpAction(FollowUpAction.EActionType.PENALTY, team.opposite(),
				NGeometry.getPenaltyMark(team));
		
		return Optional.of(new GameEvent(EGameEvent.MULTIPLE_YELLOW_CARDS, frame.getTimestamp(), team, followUp));
	}
	
	
	@Override
	public void reset()
	{
		penaltyGivenInThisStopPhase = false;
	}
}
