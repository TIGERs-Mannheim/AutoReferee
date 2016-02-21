/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 19, 2016
 * Author(s): Lukas Magel
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.rules.impl.violations;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.g3force.configurable.Configurable;

import edu.tigers.autoreferee.engine.IRuleEngineFrame;
import edu.tigers.autoreferee.engine.rules.RuleResult;
import edu.tigers.autoreferee.engine.rules.impl.AGameRule;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.wp.data.EGameStateNeutral;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * @author Lukas Magel
 */
public class BotCollisionRule extends AGameRule
{
	private static final int	priority					= 1;
	
	@Configurable(comment = "The velocity threshold above with a bot contact is considered a crash")
	private static double		CRASH_VEL_THRESHOLD	= 4.0;
	
	static
	{
		AGameRule.registerClass(BotCollisionRule.class);
	}
	
	
	/**
	 * 
	 */
	public BotCollisionRule()
	{
		super(EGameStateNeutral.RUNNING);
	}
	
	
	@Override
	public int getPriority()
	{
		return priority;
	}
	
	
	@Override
	public Optional<RuleResult> update(final IRuleEngineFrame frame)
	{
		Collection<ITrackedBot> bots = frame.getWorldFrame().getBots().values();
		List<ITrackedBot> yellowBots = filterByColor(bots, ETeamColor.YELLOW);
		List<ITrackedBot> blueBots = filterByColor(bots, ETeamColor.BLUE);
		
		for (ITrackedBot blueBot : blueBots)
		{
			for (ITrackedBot yellowBot : yellowBots)
			{
				
				if (GeoMath.distancePP(blueBot.getPos(), yellowBot.getPos()) <= (2 * Geometry.getBotRadius()))
				{
					double crashVel = calcCrashVelocity(blueBot.getVel(), yellowBot.getVel());
				}
				
			}
		}
		
		return Optional.empty();
	}
	
	
	/**
	 * The function splits each vector into its perpendicular parts (x|y). The x values represent the parts of the
	 * vectors that point in the same direction and the y values represent the parts of the vectors that point towards
	 * each other.
	 * The x values are subtracted from each other to calculate the relative velocity with which both robots travel in
	 * the same direction. The y values are added up two calculate the relative velocity with which the robots travel
	 * towards each other:
	 * Result = |ax - bx| + (ay + by)
	 * Result = |cos(alpha/2) * |va| - cos(alpha/2) * |vb|| + (sin(alpha/2) * |va| + sin(alpha/2) * |vb|)
	 * Result = cos(alpha/2) * ||va| - |vb|| + sin(alpha/2) * (|va| + |vb|)
	 * 
	 * <pre>
	 * 
	 *   va----x----vb
	 *    :\   |   /:
	 * ax : \  |  / : bx
	 *    :  \^|^/  :
	 *    :   \|/<--alpha / 2
	 *    :....*....:
	 *     ay    by
	 * </pre>
	 * 
	 * @param va
	 * @param vb
	 * @return
	 */
	private double calcCrashVelocity(final IVector2 va, final IVector2 vb)
	{
		double angle = GeoMath.angleBetweenVectorAndVector(va, vb);
		double a = va.getLength();
		double b = vb.getLength();
		return (Math.sin(angle / 2) * (a + b)) + (Math.cos(angle / 2) * Math.abs(a - b));
	}
	
	
	private List<ITrackedBot> filterByColor(final Collection<ITrackedBot> bots, final ETeamColor color)
	{
		return bots.stream().filter(bot -> bot.getBotId().getTeamColor() == color).collect(Collectors.toList());
	}
	
	
	@Override
	public void reset()
	{
	}
	
}
