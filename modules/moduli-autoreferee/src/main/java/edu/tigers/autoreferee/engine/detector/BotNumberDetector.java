/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine.detector;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import edu.tigers.autoreferee.AutoRefUtil.ColorFilter;
import edu.tigers.autoreferee.engine.events.IGameEvent;
import edu.tigers.autoreferee.engine.events.TooManyRobots;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * This rule compares the number of bots that are located inside the field with the maximum allowed number of bots. It
 * also consults the team info sent by the referee box to detect if a team did not decrease their bot number after a
 * yellow card.
 */
public class BotNumberDetector extends AGameEventDetector
{
	private final Map<ETeamColor, Integer> lastDiff = new EnumMap<>(ETeamColor.class);
	
	
	public BotNumberDetector()
	{
		super(EGameEventDetectorType.BOT_NUMBER, EGameState.RUNNING);
	}
	
	
	@Override
	protected void doPrepare()
	{
		lastDiff.put(ETeamColor.YELLOW, 0);
		lastDiff.put(ETeamColor.BLUE, 0);
	}
	
	
	@Override
	public Optional<IGameEvent> doUpdate()
	{
		final Optional<IGameEvent> yellow = checkTeam(ETeamColor.YELLOW);
		if (yellow.isPresent())
		{
			return yellow;
		}
		return checkTeam(ETeamColor.BLUE);
	}
	
	
	private Optional<IGameEvent> checkTeam(final ETeamColor teamColor)
	{
		Collection<ITrackedBot> bots = frame.getWorldFrame().getBots().values();
		
		int allowedCount = frame.getRefereeMsg().getTeamInfo(teamColor).getMaxAllowedBots();
		int actualCount = getTeamOnFieldBotCount(bots, teamColor);
		
		int diff = actualCount - allowedCount;
		
		if ((diff > lastDiff.get(teamColor)) && (diff > 0))
		{
			lastDiff.put(teamColor, diff);
			return Optional.of(new TooManyRobots(teamColor));
		}
		return Optional.empty();
	}
	
	
	private int getTeamOnFieldBotCount(final Collection<ITrackedBot> bots, final ETeamColor color)
	{
		/*
		 * The filter mechanism uses the extended field to also catch bots which might be positioned partially outside the
		 * regular field
		 */
		return (int) bots.stream()
				.filter(ColorFilter.get(color))
				.filter(bot -> Geometry.getFieldWBorders().isPointInShape(bot.getPos()))
				.count();
	}
}
