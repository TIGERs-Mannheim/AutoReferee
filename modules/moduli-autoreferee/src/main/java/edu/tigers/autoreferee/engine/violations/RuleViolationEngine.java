/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 1, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.violations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;

import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.violations.IViolationDetector.EViolationDetectorType;
import edu.tigers.autoreferee.engine.violations.impl.AttackerToDefenseAreaDistanceDetector;
import edu.tigers.autoreferee.engine.violations.impl.AttackerTouchKeeperDetector;
import edu.tigers.autoreferee.engine.violations.impl.BallLeftFieldDetector;
import edu.tigers.autoreferee.engine.violations.impl.BallSpeedingDetector;
import edu.tigers.autoreferee.engine.violations.impl.BotCollisionDetector;
import edu.tigers.autoreferee.engine.violations.impl.BotInDefenseAreaDetector;
import edu.tigers.autoreferee.engine.violations.impl.BotNumberDetector;
import edu.tigers.autoreferee.engine.violations.impl.BotStopSpeedDetector;
import edu.tigers.autoreferee.engine.violations.impl.DefenderToKickPointDistanceDetector;
import edu.tigers.autoreferee.engine.violations.impl.DoubleTouchDetector;
import edu.tigers.autoreferee.engine.violations.impl.DribblingDetector;
import edu.tigers.autoreferee.engine.violations.impl.IndirectGoalDetector;
import edu.tigers.autoreferee.engine.violations.impl.KickTimeoutDetector;
import edu.tigers.sumatra.wp.data.EGameStateNeutral;


/**
 * @author "Lukas Magel"
 */
public class RuleViolationEngine
{
	private Map<EViolationDetectorType, IViolationDetector>	detectors	= new HashMap<>();
	
	
	/**
	 * 
	 */
	public RuleViolationEngine()
	{
		this(new HashSet<>(Arrays.asList(EViolationDetectorType.values())));
	}
	
	
	/**
	 * @param detectorTypes
	 */
	public RuleViolationEngine(final Set<EViolationDetectorType> detectorTypes)
	{
		detectorTypes.forEach(type -> detectors.put(type, createDetector(type)));
	}
	
	
	private static IViolationDetector createDetector(final EViolationDetectorType type)
	{
		switch (type)
		{
			case ATTACKER_TO_DEFENSE_DISTANCE:
				return new AttackerToDefenseAreaDistanceDetector();
			case ATTACKER_TOUCHED_KEEPER:
				return new AttackerTouchKeeperDetector();
			case BALL_LEFT_FIELD_ICING:
				return new BallLeftFieldDetector();
			case BALL_SPEEDING:
				return new BallSpeedingDetector();
			case BOT_COLLISION:
				return new BotCollisionDetector();
			case BOT_IN_DEFENSE_AREA:
				return new BotInDefenseAreaDetector();
			case BOT_NUMBER:
				return new BotNumberDetector();
			case BOT_STOP_SPEED:
				return new BotStopSpeedDetector();
			case DOUBLE_TOUCH:
				return new DoubleTouchDetector();
			case DRIBBLING:
				return new DribblingDetector();
			case DEFENDER_TO_KICK_POINT_DISTANCE:
				return new DefenderToKickPointDistanceDetector();
			case INDIRECT_GOAL:
				return new IndirectGoalDetector();
			case KICK_TIMEOUT:
				return new KickTimeoutDetector();
			default:
				throw new IllegalArgumentException("Please add the new type \"" + type + "\" to this switch case clause!");
		}
	}
	
	
	/**
	 * @param detectorTypes
	 */
	public void setActiveDetectors(final Set<EViolationDetectorType> detectorTypes)
	{
		Set<EViolationDetectorType> toBeRemoved = Sets.difference(detectors.keySet(), detectorTypes).immutableCopy();
		Set<EViolationDetectorType> toBeAdded = Sets.difference(detectorTypes, detectors.keySet()).immutableCopy();
		
		toBeRemoved.forEach(type -> detectors.remove(type));
		toBeAdded.forEach(type -> detectors.put(type, createDetector(type)));
	}
	
	
	/**
	 * @param type
	 */
	public void activateDetector(final EViolationDetectorType type)
	{
		if (!detectors.containsKey(type))
		{
			detectors.put(type, createDetector(type));
		}
	}
	
	
	/**
	 * @param type
	 */
	public void deactivateDetector(final EViolationDetectorType type)
	{
		detectors.remove(type);
	}
	
	
	/**
	 * 
	 */
	public void reset()
	{
		detectors.values().forEach(detector -> detector.reset());
	}
	
	
	/**
	 * @param frame
	 * @return
	 */
	public List<IRuleViolation> update(final IAutoRefFrame frame)
	{
		EGameStateNeutral currentState = frame.getGameState();
		EGameStateNeutral lastState = frame.getPreviousFrame().getGameState();
		
		/*
		 * Retrieve all rules which are active in the current gamestate
		 */
		List<IViolationDetector> activeDetectors = detectors.values().stream()
				.filter(detector -> detector.isActiveIn(currentState))
				.sorted(IViolationDetector.ViolationDetectorComparator.INSTANCE)
				.collect(Collectors.toList());
		
		/*
		 * Reset the detectors which have now become active
		 */
		activeDetectors.stream()
				.filter(detector -> !detector.isActiveIn(lastState))
				.forEach(detector -> detector.reset());
		
		List<IRuleViolation> violations = new ArrayList<>();
		for (IViolationDetector detector : activeDetectors)
		{
			Optional<IRuleViolation> result = detector.update(frame, violations);
			result.ifPresent(val -> violations.add(val));
		}
		
		return violations;
	}
}
