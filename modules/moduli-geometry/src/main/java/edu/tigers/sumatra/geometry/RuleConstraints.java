/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.geometry;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

/**
 * Configuration object for rule parameters.
 *
 * @author Stefan Schneyer
 */
public class RuleConstraints {

    /**
     * Rules
     */

    @Configurable
    private static double maxBallSpeed = 6.5;
    @Configurable
    private static double stopRadius = 500;
    @Configurable(comment = "Bots must be behind this line on penalty shot")
    private static double distancePenaltyMarkToPenaltyLine = 400;
    @Configurable
    private static double stopSpeed = 1.5;
    @Configurable
    private static double botToPenaltyAreaDistanceStandard = 200;

    static
    {
        ConfigRegistration.registerClass("ruleConst", RuleConstraints.class);
    }

    private RuleConstraints()
    { }

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
}
