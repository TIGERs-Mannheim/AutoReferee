/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.vision.data;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import lombok.Value;


@Value
public class VirtualBallCandidate
{
	FilteredVisionBot observingRobot;
	IVector2 observedFromPosition;
	IVector3 ballPosition;
}
