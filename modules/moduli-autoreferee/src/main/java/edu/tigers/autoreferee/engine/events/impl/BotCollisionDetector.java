/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.events.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.github.g3force.configurable.Configurable;

import edu.tigers.autoreferee.AutoRefUtil;
import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.AutoRefMath;
import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.FollowUpAction.EActionType;
import edu.tigers.autoreferee.engine.events.CrashViolation;
import edu.tigers.autoreferee.engine.events.EGameEvent;
import edu.tigers.autoreferee.engine.events.IGameEvent;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.line.v2.ILine;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * @author Lukas Magel
 */
public class BotCollisionDetector extends AGameEventDetector
{
	private static final int PRIORITY = 1;
	
	@Configurable(comment = "[m/s] The velocity threshold above with a bot contact is considered a crash", defValue = "1.5")
	private static double crashVelThreshold = 1.5;
	
	@Configurable(comment = "[m/s] The contact is only considered a crash if the speed of both bots differ by at least this value", defValue = "0.3")
	private static double minSpeedDiff = 0.3;
	
	@Configurable(comment = "[ms] Wait time before reporting a crash with a robot again", defValue = "1000")
	private static double crashCooldownTimeMs = 1_000;
	
	@Configurable(comment = "Adjust the bot to bot distance that is considered a contact: dist * factor", defValue = "1.1")
	private static double minDistanceFactor = 1.1;
	
	@Configurable(comment = "The lookahead [s] that is used to estimate the brake amount of each bot", defValue = "0.1")
	private static double botBrakeLookahead = 0.1;
	
	private Map<BotID, Long> lastViolators = new HashMap<>();
	
	static
	{
		AGameEventDetector.registerClass(BotCollisionDetector.class);
	}
	
	
	public BotCollisionDetector()
	{
		super(EGameState.RUNNING);
	}
	
	
	@Override
	public int getPriority()
	{
		return PRIORITY;
	}
	
	
	@Override
	public Optional<IGameEvent> update(final IAutoRefFrame frame, final List<IGameEvent> violations)
	{
		Collection<ITrackedBot> bots = frame.getWorldFrame().getBots().values();
		List<ITrackedBot> yellowBots = AutoRefUtil.filterByColor(bots, ETeamColor.YELLOW);
		List<ITrackedBot> blueBots = AutoRefUtil.filterByColor(bots, ETeamColor.BLUE);
		
		List<BotPair> consideredBotPairs = calcConsideredBots(yellowBots, blueBots, frame.getTimestamp());
		return checkForCrashEvent(consideredBotPairs, frame);
	}
	
	
	private double predictBotVel(final ITrackedBot bot)
	{
		return bot.getVel().getLength2() - bot.getMoveConstraints().getAccMax() * botBrakeLookahead;
	}
	
	
	private Optional<IGameEvent> checkForCrashEvent(List<BotPair> consideredBotPairs, IAutoRefFrame frame)
	{
		long curTS = frame.getTimestamp();
		for (BotPair pair : consideredBotPairs)
		{
			double crashVel = calcCrashVelocity(pair.blueBot, pair.yellowBot);
			if (!isCrashCritical(crashVel))
			{
				continue;
			}
			lastViolators.put(pair.blueBot.getBotId(), curTS);
			lastViolators.put(pair.yellowBot.getBotId(), curTS);
			
			double blueVel = predictBotVel(pair.blueBot);
			double yellowVel = predictBotVel(pair.yellowBot);
			double velDiff = blueVel - yellowVel;
			
			final BotID primaryBot;
			final BotID secondaryBot;
			final double primarySpeed;
			final double secondarySpeed;
			if (velDiff > 0)
			{
				primaryBot = pair.blueBot.getBotId();
				secondaryBot = pair.yellowBot.getBotId();
				primarySpeed = blueVel;
				secondarySpeed = yellowVel;
			} else
			{
				primaryBot = pair.yellowBot.getBotId();
				secondaryBot = pair.blueBot.getBotId();
				primarySpeed = yellowVel;
				secondarySpeed = blueVel;
			}
			
			IVector2 kickPos = Lines.segmentFromPoints(pair.blueBot.getPos(), pair.yellowBot.getPos()).getCenter();
			kickPos = AutoRefMath.getClosestFreekickPos(kickPos, primaryBot.getTeamColor().opposite());
			
			final FollowUpAction followUp;
			final BotID secondaryViolator;
			if (Math.abs(velDiff) < minSpeedDiff)
			{
				kickPos = AutoRefMath.getClosestFreekickPos(kickPos, secondaryBot.getTeamColor().opposite());
				followUp = new FollowUpAction(EActionType.FORCE_START, ETeamColor.NEUTRAL, kickPos);
				secondaryViolator = secondaryBot;
			} else
			{
				followUp = new FollowUpAction(EActionType.DIRECT_FREE, primaryBot.getTeamColor()
						.opposite(), kickPos);
				secondaryViolator = null;
			}
			
			return Optional.of(new CrashViolation(EGameEvent.BOT_COLLISION, frame.getTimestamp(),
					primaryBot, crashVel, velDiff, followUp)
							.setSecondResponsibleBot(secondaryViolator)
							.setSpeedPrimaryBot(primarySpeed)
							.setSpeedSecondaryBot(secondarySpeed));
		}
		return Optional.empty();
	}
	
	
	private List<BotPair> calcConsideredBots(final List<ITrackedBot> yellowBots, final List<ITrackedBot> blueBots,
			final long curTS)
	{
		List<BotPair> consideredBotPairs = new ArrayList<>();
		for (ITrackedBot blueBot : blueBots)
		{
			if (botStillOnCoolDown(blueBot.getBotId(), curTS))
			{
				continue;
			}
			lastViolators.remove(blueBot.getBotId());
			for (ITrackedBot yellowBot : yellowBots)
			{
				if (botStillOnCoolDown(yellowBot.getBotId(), curTS))
				{
					continue;
				}
				lastViolators.remove(yellowBot.getBotId());
				
				if (isRobotPairConsiderable(blueBot, yellowBot))
				{
					consideredBotPairs.add(new BotPair(blueBot, yellowBot));
				}
				
			}
		}
		return consideredBotPairs;
	}
	
	
	private boolean isRobotPairConsiderable(ITrackedBot blueBot, ITrackedBot yellowBot)
	{
		return VectorMath.distancePP(blueBot.getPos(),
				yellowBot.getPos()) <= (2 * Geometry.getBotRadius() * minDistanceFactor);
	}
	
	
	private boolean isCrashCritical(double crashVel)
	{
		return crashVel > crashVelThreshold;
	}
	
	
	private boolean botStillOnCoolDown(final BotID bot, final long curTS)
	{
		if (lastViolators.containsKey(bot))
		{
			Long ts = lastViolators.get(bot);
			return (curTS - ts) < (crashCooldownTimeMs * 1_000_000);
		}
		return false;
	}
	
	
	private double calcCrashVelocity(final ITrackedBot bot1, final ITrackedBot bot2)
	{
		IVector2 bot1Vel = bot1.getVel().scaleToNew(predictBotVel(bot1));
		IVector2 bot2Vel = bot2.getVel().scaleToNew(predictBotVel(bot2));
		IVector2 velDiff = bot1Vel.subtractNew(bot2Vel);
		IVector2 center = Lines.segmentFromPoints(bot1.getPos(), bot2.getPos()).getCenter();
		IVector2 crashVelReferencePoint = center.addNew(velDiff);
		ILine collisionReferenceLine = Lines.lineFromPoints(bot1.getPos(), bot2.getPos());
		IVector2 projectedCrashVelReferencePoint = collisionReferenceLine.closestPointOnLine(crashVelReferencePoint);
		
		return projectedCrashVelReferencePoint.distanceTo(center);
	}
	
	
	@Override
	public void reset()
	{
		lastViolators = new HashMap<>();
	}
	
	private class BotPair
	{
		ITrackedBot blueBot;
		ITrackedBot yellowBot;
		
		
		BotPair(ITrackedBot blueBot, ITrackedBot yellowBot)
		{
			if (!(blueBot.getTeamColor() == ETeamColor.BLUE && yellowBot.getTeamColor() == ETeamColor.YELLOW))
			{
				throw new IllegalArgumentException("Robots need to be of different team colors");
			}
			this.blueBot = blueBot;
			this.yellowBot = yellowBot;
		}
	}
	
}
