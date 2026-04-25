package edu.tigers.sumatra.wp.data;

import edu.tigers.sumatra.bot.RobotInfo;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.ids.ETeamColor;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Collections;
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
	private final Map<BotID, ITrackedBot> opponentBots;
	private final Map<BotID, ITrackedBot> opponentBotsVisible;
	private final Map<BotID, ITrackedBot> tigerBotsVisible;
	private final Map<BotID, ITrackedBot> tigerBotsInSubstitutionArea;
	private final Map<BotID, ITrackedBot> opponentBotsInSubstitutionArea;
	private final Map<BotID, ITrackedBot> tigerBotsAvailable;
	@Getter(AccessLevel.PRIVATE)
	private final Map<BotID, ITrackedBot> allBots;
	private final ETeamColor teamColor;
	private final boolean inverted;


	public WorldFrame(final SimpleWorldFrame simpleWorldFrame, final EAiTeam team, final boolean inverted)
	{
		super(simpleWorldFrame);
		this.teamColor = team.getTeamColor();
		this.inverted = inverted;

		opponentBotsInSubstitutionArea = computeOpponentInSubstitutionArea(simpleWorldFrame, team);
		opponentBots = computeOpponentBots(simpleWorldFrame, team, opponentBotsInSubstitutionArea);
		opponentBotsVisible = computeOpponentBotsVisible(simpleWorldFrame, team);
		tigerBotsInSubstitutionArea = computeTigersInSubstitutionArea(simpleWorldFrame, team);
		tigerBotsAvailable = computeTigersAvailable(simpleWorldFrame, team);
		tigerBotsVisible = computeTigersVisible(simpleWorldFrame, team);
		allBots = Stream.concat(opponentBotsVisible.entrySet().stream(), tigerBotsVisible.entrySet().stream())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
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
		opponentBotsInSubstitutionArea = original.getOpponentBotsInSubstitutionArea();
		opponentBots = original.getOpponentBots();
		opponentBotsVisible = original.getOpponentBotsVisible();
		tigerBotsAvailable = original.getTigerBotsAvailable();
		tigerBotsInSubstitutionArea = original.getTigerBotsInSubstitutionArea();
		tigerBotsVisible = original.getTigerBotsVisible();
		allBots = original.getAllBots();
	}


	@Override
	public ITrackedBot getBot(BotID botId)
	{
		return allBots.get(botId);
	}


	@Override
	public Map<BotID, ITrackedBot> getBots()
	{
		return allBots;
	}


	private Map<BotID, ITrackedBot> computeOpponentBots(
			final SimpleWorldFrame simpleWorldFrame, final EAiTeam aiTeam,
			final Map<BotID, ITrackedBot> opponentBotsInSubstitutionArea
	)
	{
		Map<BotID, ITrackedBot> opponents = simpleWorldFrame.getBots().values().stream()
				.filter(bot -> aiTeam.matchesColor(bot.getBotId().getTeamColor().opposite()))
				.filter(bot -> Geometry.getFieldWBorders().isPointInShape(bot.getPos()))
				.filter(bot -> !opponentBotsInSubstitutionArea.containsKey(bot.getBotId()))
				.map(bot -> {
					RobotInfo info = RobotInfo.stubBuilder(bot.getBotId(), bot.getTimestamp())
							.withBotParams(bot.getRobotInfo().getBotParams())
							.build();
					return TrackedBot.newCopyBuilder(bot)
							.withBotInfo(info)
							.withState(bot.getFilteredState().orElse(bot.getBotState()))
							.build();
				})
				.collect(Collectors.toMap(TrackedBot::getBotId, Function.identity()));
		return Collections.unmodifiableMap(opponents);
	}


	private Map<BotID, ITrackedBot> computeTigersVisible(final SimpleWorldFrame simpleWorldFrame, final EAiTeam aiTeam)
	{
		Map<BotID, ITrackedBot> visible = simpleWorldFrame.getBots().values().stream()
				.filter(bot -> aiTeam.matchesColor(bot.getTeamColor()))
				.collect(Collectors.toMap(ITrackedBot::getBotId, Function.identity()));
		return Collections.unmodifiableMap(visible);
	}


	private Map<BotID, ITrackedBot> computeOpponentBotsVisible(
			final SimpleWorldFrame simpleWorldFrame, final EAiTeam aiTeam)
	{
		Map<BotID, ITrackedBot> visible = simpleWorldFrame.getBots().values().stream()
				.filter(bot -> aiTeam.opposite().matchesColor(bot.getTeamColor()))
				.collect(Collectors.toMap(ITrackedBot::getBotId, Function.identity()));
		return Collections.unmodifiableMap(visible);
	}


	private Map<BotID, ITrackedBot> computeTigersInSubstitutionArea(
			final SimpleWorldFrame simpleWorldFrame, final EAiTeam aiTeam)
	{
		Map<BotID, ITrackedBot> visible = simpleWorldFrame.getBots().values().stream()
				.filter(bot -> aiTeam.matchesColor(bot.getTeamColor())
						&& Geometry.getGoalSubstitutionAreaOur().withMargin(-Geometry.getBotRadius())
						.isPointInShape(bot.getPos()))
				.collect(Collectors.toMap(ITrackedBot::getBotId, Function.identity()));
		return Collections.unmodifiableMap(visible);
	}


	private Map<BotID, ITrackedBot> computeOpponentInSubstitutionArea(
			final SimpleWorldFrame simpleWorldFrame, final EAiTeam aiTeam)
	{
		Map<BotID, ITrackedBot> visible = simpleWorldFrame.getBots().values().stream()
				.filter(bot -> aiTeam.opposite().matchesColor(bot.getTeamColor())
						&& Geometry.getGoalSubstitutionAreaTheir().withMargin(-Geometry.getBotRadius())
						.isPointInShape(bot.getPos()))
				.collect(Collectors.toMap(ITrackedBot::getBotId, Function.identity()));
		return Collections.unmodifiableMap(visible);
	}


	private Map<BotID, ITrackedBot> computeTigersAvailable(
			final SimpleWorldFrame simpleWorldFrame, final EAiTeam aiTeam
	)
	{
		Map<BotID, ITrackedBot> visible = simpleWorldFrame.getBots().values().stream()
				.filter(bot -> aiTeam.matchesColor(bot.getTeamColor()))
				.filter(bot -> bot.getRobotInfo().isConnected())
				.filter(bot -> bot.getRobotInfo().isAvailableToAi())
				.filter(bot -> !bot.isMalFunctioning())
				.collect(Collectors.toMap(ITrackedBot::getBotId, Function.identity()));
		return Collections.unmodifiableMap(visible);
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
