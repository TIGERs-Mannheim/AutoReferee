/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.geometry;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import lombok.Getter;


/**
 * All parameters required for ball models.
 */
@Getter
public class BallParameters
{
	@Configurable(
			comment = "Ball sliding acceleration [mm/s^2]",
			defValue = "-3600.0",
			spezis = { "SUMATRA", "LAB", "TISCH", "ROBOCUP", "ANDRE", "NICOLAI" }
	)
	private double accSlide = -3600.0;

	@Configurable(
			comment = "Ball rolling acceleration [mm/s^2]",
			defValue = "-400.0",
			spezis = { "SUMATRA", "LAB", "TISCH", "ROBOCUP", "ANDRE", "NICOLAI" }
	)
	private double accRoll = -400.0;

	@Configurable(
			comment = "Fraction of the initial velocity where the ball starts to roll",
			defValue = "0.64",
			spezis = { "SUMATRA", "LAB", "TISCH", "ROBOCUP", "ANDRE", "NICOLAI" }
	)
	private double kSwitch = 0.64;

	@Configurable(
			comment = "Fixed velocity where the ball starts to roll [mm/s]",
			defValue = "2000.0",
			spezis = { "SUMATRA", "LAB", "TISCH", "ROBOCUP", "ANDRE", "NICOLAI" }
	)
	private double vSwitch = 2000.0;

	@Configurable(
			comment = "Chip kick velocity damping factor in XY direction for the first hop",
			defValue = "0.75",
			spezis = { "SUMATRA", "LAB", "TISCH", "ROBOCUP", "ANDRE", "NICOLAI" }
	)
	private double chipDampingXYFirstHop = 0.75;

	@Configurable(
			comment = "Chip kick velocity damping factor in XY direction for all following hops",
			defValue = "0.95",
			spezis = { "SUMATRA", "LAB", "TISCH", "ROBOCUP", "ANDRE", "NICOLAI" }
	)
	private double chipDampingXYOtherHops = 0.95;

	@Configurable(
			comment = "Chip kick velocity damping factor in Z direction",
			defValue = "0.6",
			spezis = { "SUMATRA", "LAB", "TISCH", "ROBOCUP", "ANDRE", "NICOLAI" }
	)
	private double chipDampingZ = 0.6;

	@Configurable(
			comment = "If a chipped ball does not reach this height it is considered rolling [mm]",
			defValue = "10.0"
	)
	private double minHopHeight = 10;

	@Configurable(
			comment = "Max. ball height that can be intercepted by robots [mm]",
			defValue = "150.0"
	)
	private double maxInterceptableHeight = 150.0;

	static
	{
		ConfigRegistration.registerClass("geom", BallParameters.class);
	}
}
