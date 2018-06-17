/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.geometry;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;


/**
 * Configuration object for rule parameters.
 *
 * @author Stefan Schneyer
 */
public class RuleConstraints
{
	@Configurable(comment = "Bots per team in standard game", defValue = "8")
	private static int botsPerTeam = 8;
	@Configurable(comment = "Max allowed ball speed", defValue = "6.5")
	private static double maxBallSpeed = 6.5;
	@Configurable(comment = "Stop radius around ball", defValue = "500.0")
	private static double stopRadius = 500.0;
	@Configurable(comment = "Bots must be behind this line on penalty shot", defValue = "400.0")
	private static double distancePenaltyMarkToPenaltyLine = 400;
	@Configurable(comment = "Bot speed in stop phases", defValue = "1.5")
	private static double stopSpeed = 1.5;
	@Configurable(comment = "Distance between bots and penalty area in standard situations", defValue = "200.0")
	private static double botToPenaltyAreaDistanceStandard = 200;
	@Configurable(comment = "Ball placement accuracy tolerance of referee", defValue = "100.0")
	private static double ballPlacementTolerance = 100;
	@Configurable(comment = "The max allowed robot height", defValue = "150.0")
	private static double maxRobotHeight = 150;
	@Configurable(defValue = "100.0", comment = "[mm] throw-in distance from field border")
	private static double throwInDistance = 100;
	@Configurable(defValue = "600.0", comment = "[mm] distance to goalLine if freeKick was awarded to defending team inside "
			+ "20mm radius of its own defense area")
	private static double distanceToGoalLine = 600;
	@Configurable(defValue = "700.0", comment = "[mm] distance to defense area when freekick was awarded to attacking team "
			+ "within 700mm distance of opposing defense area")
	private static double offenseFreeKickDistance = 700;
	@Configurable(defValue = "500.0", comment = "[mm] distance to goalLine after offensive team kicked ball over goaLine of defending team")
	private static double goalKickDistance = 500;
	
	static
	{
		ConfigRegistration.registerClass("ruleConst", RuleConstraints.class);
	}
	
	
	private RuleConstraints()
	{
	}
	
	
	/**
	 * @return the allowed number of bots per team
	 */
	public static int getBotsPerTeam()
	{
		return botsPerTeam;
	}
	
	
	/**
	 * @return the stopSpeed
	 */
	public static double getStopSpeed()
	{
		return stopSpeed;
	}
	
	
	/**
	 * @return distance from penalty mark to penalty line
	 */
	public static double getDistancePenaltyMarkToPenaltyLine()
	{
		return distancePenaltyMarkToPenaltyLine;
	}
	
	
	/**
	 * distance between ball and bot required during stop signal (without ball and bot radius!)
	 *
	 * @return distance
	 */
	public static double getStopRadius()
	{
		return stopRadius;
	}
	
	
	/**
	 * Additional margin to opponents penalty area in our standard situations
	 *
	 * @return margin
	 */
	public static double getBotToPenaltyAreaMarginStandard()
	{
		return botToPenaltyAreaDistanceStandard;
	}
	
	
	/**
	 * Maximal speed allowed for kicking the ball
	 *
	 * @return The maximum allowed ball velocity in m/s
	 */
	public static double getMaxBallSpeed()
	{
		return maxBallSpeed;
	}
	
	
	public static double getBallPlacementTolerance()
	{
		return ballPlacementTolerance;
	}
	
	
	public static double getMaxRobotHeight()
	{
		return maxRobotHeight;
	}
	
	
	public static double getThrowInDistance()
	{
		return throwInDistance;
	}
	
	
	public static double getDistanceToGoalLine()
	{
		return distanceToGoalLine;
	}
	
	
	public static double getOffenseFreeKickDistance()
	{
		return offenseFreeKickDistance;
	}
	
	
	public static double getGoalKickDistance()
	{
		return goalKickDistance;
	}
}
