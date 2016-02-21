/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 7, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.rules.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.github.g3force.configurable.ConfigRegistration;

import edu.tigers.autoreferee.AutoRefConfig;
import edu.tigers.autoreferee.engine.rules.IGameRule;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.wp.data.EGameStateNeutral;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;
import edu.tigers.sumatra.wp.data.TrackedBall;


/**
 * Abstract base class that contains common operations for the game rules
 * 
 * @author "Lukas Magel"
 */
public abstract class AGameRule implements IGameRule
{
	private final List<EGameStateNeutral>	activeStates;
	
	
	/**
	 * @param gamestate The gamestate this rule will be active in
	 */
	public AGameRule(final EGameStateNeutral gamestate)
	{
		this(Arrays.asList(gamestate));
	}
	
	
	/**
	 * @param activeStates the list of game states that the rule will be active in
	 */
	public AGameRule(final List<EGameStateNeutral> activeStates)
	{
		this.activeStates = activeStates;
	}
	
	
	@Override
	public boolean isActiveIn(final EGameStateNeutral state)
	{
		return activeStates.contains(state);
	}
	
	
	protected static void registerClass(final Class<?> clazz)
	{
		ConfigRegistration.registerClass("autoreferee", clazz);
	}
	
	
	protected static boolean botsAreStationary(final Collection<ITrackedBot> bots)
	{
		return bots.stream().allMatch(
				bot -> bot.getVelByTime(0).getLength() < AutoRefConfig.getStationarySpeedThreshold());
	}
	
	
	protected static boolean ballIsPlaced(final TrackedBall ball, final IVector2 destPos)
	{
		double dist = GeoMath.distancePP(ball.getPos(), destPos);
		double velocity = ball.getVel().getLength();
		if ((dist < AutoRefConfig.getBallPlacementAccuracy()) && (velocity < AutoRefConfig.getStationarySpeedThreshold()))
		{
			return true;
		}
		return false;
	}
	
	
	protected static boolean ballIsStationary(final TrackedBall ball)
	{
		return ball.getVel().getLength() < AutoRefConfig.getStationarySpeedThreshold();
	}
	
	
	protected static boolean botStopDistanceIsCorrect(final SimpleWorldFrame frame)
	{
		Collection<ITrackedBot> bots = frame.getBots().values();
		IVector2 ballPos = frame.getBall().getPos();
		
		return bots.stream().allMatch(
				bot -> GeoMath.distancePP(bot.getPosByTime(0), ballPos) > Geometry.getBotToBallDistanceStop());
	}
}
