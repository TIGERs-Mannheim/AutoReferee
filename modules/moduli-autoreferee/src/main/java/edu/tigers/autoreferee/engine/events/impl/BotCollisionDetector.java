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
import edu.tigers.autoreferee.engine.events.GameEvent;
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
	
	@Configurable(comment = "Adjust the bot to bot distance that is considered a contact: dist * factor", defValue = "0.9")
	private static double minDistanceFactor = 0.9;
	
	private Map<BotID, Long> lastViolators = new HashMap<>();
	
	static
	{
		AGameEventDetector.registerClass(BotCollisionDetector.class);
	}
	
	
	/**
	 * 
	 */
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
			
			IVector2 blueVel = pair.blueBot.getVel();
			IVector2 yellowVel = pair.yellowBot.getVel();
			double velDiff = blueVel.getLength() - yellowVel.getLength();
			
			if (Math.abs(velDiff) < minSpeedDiff)
			{
				return Optional.of(createCrashViolationGameStateForBothTeams(frame, pair, crashVel, velDiff,
						calcCenterOfTwoPoints(pair.blueBot.getPos(), pair.yellowBot.getPos())));
			}
			return Optional.of(createSingleResponsibleCrash(frame, crashVel, velDiff, pair));
		}
		return Optional.empty();
	}
	
	
	private GameEvent createSingleResponsibleCrash(IAutoRefFrame frame, double collisionSpeed, double velDiff,
			BotPair pair)
	{
		BotID violatorID;
		IVector2 kickPos;
		if (velDiff > 0)
		{
			violatorID = pair.blueBot.getBotId();
			kickPos = pair.blueBot.getPos();
		} else
		{
			violatorID = pair.yellowBot.getBotId();
			kickPos = pair.yellowBot.getPos();
		}
		return createCrashViolationGameState(frame, violatorID, collisionSpeed, velDiff, kickPos);
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
	
	
	private GameEvent createCrashViolationGameState(final IAutoRefFrame frame, final BotID violatorID,
			final double violatorSpeed, final double velDiff, IVector2 kickPos)
	{
		kickPos = AutoRefMath.getClosestFreekickPos(kickPos, violatorID.getTeamColor().opposite());
		
		FollowUpAction followUp = new FollowUpAction(EActionType.DIRECT_FREE, violatorID.getTeamColor()
				.opposite(), kickPos);
		return new CrashViolation(EGameEvent.BOT_COLLISION, frame.getTimestamp(),
				violatorID, violatorSpeed, velDiff, followUp);
	}
	
	
	private GameEvent createCrashViolationGameStateForBothTeams(final IAutoRefFrame frame, final BotPair pair,
			final double violatorSpeed, final double velDiff, IVector2 pointOfCollision)
	{
		
		pointOfCollision = AutoRefMath.getClosestFreekickPos(pointOfCollision, pair.blueBot.getTeamColor().opposite());
		pointOfCollision = AutoRefMath.getClosestFreekickPos(pointOfCollision, pair.yellowBot.getTeamColor().opposite());
		
		FollowUpAction followUp = new FollowUpAction(EActionType.FORCE_START, ETeamColor.NEUTRAL, pointOfCollision);
		return new CrashViolation(EGameEvent.BOT_COLLISION, frame.getTimestamp(),
				pair.blueBot.getBotId(), pair.yellowBot.getBotId(), violatorSpeed, velDiff, followUp);
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
		IVector2 bot1Vel = bot1.getVel();
		IVector2 bot2Vel = bot2.getVel();
		IVector2 velDiff = bot1Vel.subtractNew(bot2Vel);
		IVector2 center = calcCenterOfTwoPoints(bot1.getPos(), bot2.getPos());
		IVector2 crashVelReferencePoint = center.addNew(velDiff);
		ILine collisionReferenceLine = Lines.lineFromPoints(bot1.getPos(), bot2.getPos());
		IVector2 projectedCrashVelReferencePoint = collisionReferenceLine.closestPointOnLine(crashVelReferencePoint);
		
		return projectedCrashVelReferencePoint.distanceTo(center);
	}
	
	
	private IVector2 calcCenterOfTwoPoints(IVector2 pos1, IVector2 pos2)
	{
		return pos1.subtractNew(pos2)
				.multiply(0.5)
				.add(pos2);
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
