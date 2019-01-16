/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.detector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.g3force.configurable.Configurable;
import com.google.common.collect.Sets;

import edu.tigers.autoreferee.AutoRefUtil;
import edu.tigers.autoreferee.engine.NGeometry;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.IPenaltyArea;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.ids.IBotIDMap;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.gameevent.AttackerTouchedKeeper;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * This Rule detects attackers that touch the GoalKeeper
 */
public class AttackerTouchedKeeperDetector extends AGameEventDetector
{
	@Configurable(comment = "[mm] The minimal distance that is not considered a contact", defValue = "10.0")
	private static double touchDistance = 10.0;
	
	@Configurable(comment = "[s] The amount of time before a violation is reported again for the same bot", defValue = "1.5")
	private static double violatorCoolDownTime = 1.5;
	
	static
	{
		registerClass(AttackerTouchedKeeperDetector.class);
	}
	
	private Map<BotID, Long> oldViolators = new HashMap<>();
	
	
	public AttackerTouchedKeeperDetector()
	{
		super(EGameEventDetectorType.ATTACKER_TOUCHED_KEEPER, EGameState.RUNNING);
	}
	
	
	@Override
	public Optional<IGameEvent> doUpdate()
	{
		IBotIDMap<ITrackedBot> bots = frame.getWorldFrame().getBots();
		long timestamp = frame.getTimestamp();
		
		Set<BotID> keepers = new HashSet<>();
		keepers.add(frame.getRefereeMsg().getKeeperBotID(ETeamColor.BLUE));
		keepers.add(frame.getRefereeMsg().getKeeperBotID(ETeamColor.YELLOW));
		
		Set<BotID> violators = new HashSet<>();
		
		for (BotID keeperID : keepers)
		{
			if (!bots.containsKey(keeperID))
			{
				/*
				 * The keeper is omitted by the world predictor in this frame (maybe due to vision problems) or not located
				 * on the field. In this case it is skipped until it reappears on the field.
				 */
				continue;
			}
			ITrackedBot keeper = bots.getWithNull(keeperID);
			
			// Only check for violators if the keeper is positioned inside his own penalty area
			IPenaltyArea penArea = NGeometry.getPenaltyArea(keeperID.getTeamColor());
			if (penArea.isPointInShape(keeper.getPos()))
			{
				violators.addAll(getViolators(bots, keeper));
			}
		}
		
		/*
		 * Update the timestamp of all violators for which a violation has already been generated but which are still
		 * violating the rule
		 */
		Sets.intersection(violators, oldViolators.keySet()).forEach(bot -> oldViolators.put(bot, timestamp));
		
		/*
		 * Remove all old violators which have reached the cool down time
		 */
		oldViolators.entrySet()
				.removeIf(entry -> (timestamp - entry.getValue()) / 1e9 > violatorCoolDownTime);
		
		// get the first new violator
		Optional<BotID> violatorID = Sets.difference(violators, oldViolators.keySet()).stream().findFirst();
		
		if (violatorID.isPresent() && bots.containsKey(violatorID.get()))
		{
			ITrackedBot violator = bots.getWithNull(violatorID.get());
			
			// add current validator to old validator
			oldViolators.put(violatorID.get(), timestamp);
			
			return Optional.of(new AttackerTouchedKeeper(violatorID.get(), violator.getPos()));
			
		}
		return Optional.empty();
	}
	
	
	private Set<BotID> getViolators(final IBotIDMap<ITrackedBot> bots, final ITrackedBot target)
	{
		ETeamColor targetColor = target.getBotId().getTeamColor();
		
		IPenaltyArea penArea = NGeometry.getPenaltyArea(targetColor);
		ICircle circle = Circle.createCircle(target.getPos(), touchDistance + (Geometry.getBotRadius() * 2));
		
		List<ITrackedBot> attackingBots = AutoRefUtil.filterByColor(bots, targetColor.opposite());
		
		/*
		 * We only consider a contact to be a violation if the contact occurs inside the defense area
		 */
		return attackingBots.stream()
				.filter(bot -> circle.isPointInShape(bot.getPos(), 0))
				.filter(bot -> {
					IVector2 contactPoint = calcTwoPointCenter(bot.getPos(), target.getPos());
					return penArea.isPointInShape(contactPoint);
				})
				.map(ITrackedBot::getBotId)
				.collect(Collectors.toSet());
	}
	
	
	private IVector2 calcTwoPointCenter(final IVector2 a, final IVector2 b)
	{
		Vector2 ab = b.subtractNew(a);
		return ab.multiply(0.5d).add(a);
	}
	
	
	@Override
	public void doReset()
	{
		oldViolators.clear();
	}
}
