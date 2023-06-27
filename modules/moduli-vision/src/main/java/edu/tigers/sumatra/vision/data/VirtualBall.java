/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.vision.data;

import edu.tigers.sumatra.cam.data.CamBall;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2f;
import lombok.Value;

import java.util.List;


@Value
public class VirtualBall
{
	long timestamp;
	IVector3 position;
	List<VirtualBallCandidate> usedCandidates;


	public CamBall toCamBall(final int camId, final long frameId)
	{
		return new CamBall(0, 0, position, Vector2f.ZERO_VECTOR,
				timestamp, camId, frameId);
	}
}
