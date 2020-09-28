/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.data;

import edu.tigers.sumatra.bot.RobotInfo;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.BotIDMap;
import edu.tigers.sumatra.ids.BotIDMapConst;
import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.ids.IBotIDMap;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * This frame contains world info for a specific AI.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WorldFrame extends SimpleWorldFrame
{
	private final BotIDMapConst<ITrackedBot> opponentBots;
	private final BotIDMapConst<ITrackedBot> tigerBotsVisible;
	private final IBotIDMap<ITrackedBot> tigerBotsAvailable;
	private final ETeamColor teamColor;
	private final boolean inverted;


	public WorldFrame(final SimpleWorldFrame simpleWorldFrame, final EAiTeam team, final boolean inverted)
	{
		super(simpleWorldFrame);
		this.teamColor = team.getTeamColor();
		this.inverted = inverted;

		opponentBots = BotIDMapConst.unmodifiableBotIDMap(computeOpponentBots(simpleWorldFrame, team));
		tigerBotsAvailable = BotIDMapConst
				.unmodifiableBotIDMap(computeAvailableTigers(simpleWorldFrame, team));
		tigerBotsVisible = BotIDMapConst.unmodifiableBotIDMap(computeTigersVisible(simpleWorldFrame, team));
	}


	/**
	 * Providing a <strong>shallow</strong> copy of original (Thus new collections are created, but filled with the same
	 * values
	 *
	 * @param original
	 */
	public WorldFrame(final WorldFrame original)
	{
		super(original);
		teamColor = original.getTeamColor();
		inverted = original.isInverted();
		opponentBots = BotIDMapConst.unmodifiableBotIDMap(original.getOpponentBots());
		tigerBotsAvailable = original.getTigerBotsAvailable();
		tigerBotsVisible = BotIDMapConst.unmodifiableBotIDMap(original.getTigerBotsVisible());
	}


	private Stream<ITrackedBot> createStreamOfAiTeam(final SimpleWorldFrame swf, EAiTeam aiTeam)
	{
		return swf.getBots().values().stream()
				.filter(bot -> aiTeam.getTeamColor() == bot.getBotId().getTeamColor());
	}


	private BotIDMap<ITrackedBot> computeAvailableTigers(final SimpleWorldFrame simpleWorldFrame,
			final EAiTeam aiTeam)
	{
		Map<BotID, ITrackedBot> availableTigers = createStreamOfAiTeam(simpleWorldFrame, aiTeam)
				.collect(Collectors.toMap(ITrackedBot::getBotId, Function.identity()));
		return new BotIDMap<>(availableTigers);
	}


	private BotIDMap<ITrackedBot> computeOpponentBots(final SimpleWorldFrame simpleWorldFrame, final EAiTeam aiTeam)
	{

		Map<BotID, ITrackedBot> opponents = simpleWorldFrame.getBots().values().stream()
				.filter(bot -> bot.getBotId().getTeamColor() == aiTeam.getTeamColor().opposite())
				.filter(bot -> Geometry.getFieldWBorders().isPointInShape(bot.getPos()))
				.map(bot -> {
					RobotInfo info = RobotInfo.stub(bot.getBotId(), bot.getTimestamp());
					return TrackedBot.newCopyBuilder(bot)
							.withBotInfo(info)
							.withState(bot.getFilteredState().orElse(bot.getBotState()))
							.build();
				})
				.collect(Collectors.toMap(TrackedBot::getBotId, Function.identity()));
		return new BotIDMap<>(opponents);
	}


	private BotIDMap<ITrackedBot> computeTigersVisible(final SimpleWorldFrame simpleWorldFrame, final EAiTeam aiTeam)
	{
		Map<BotID, ITrackedBot> visible = simpleWorldFrame.getBots().values().stream()
				.filter(bot -> aiTeam.matchesColor(bot.getTeamColor()))
				.collect(Collectors.toMap(ITrackedBot::getBotId, Function.identity()));
		return new BotIDMap<>(visible);
	}


	/**
	 * Get {@link ITrackedBot}
	 *
	 * @param botId of the bot
	 * @return tiger {@link TrackedBot}
	 */
	public ITrackedBot getTiger(final BotID botId)
	{
		return getBot(botId);
	}


	/**
	 * Get opponent {@link ITrackedBot}
	 *
	 * @param botId of the bot
	 * @return opponent {@link TrackedBot}
	 */
	public ITrackedBot getOpponentBot(final BotID botId)
	{
		return getBot(botId);
	}
}
