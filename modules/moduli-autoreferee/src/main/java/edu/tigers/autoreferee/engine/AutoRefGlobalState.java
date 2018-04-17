/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine;

import java.util.EnumMap;
import java.util.Map;

import edu.tigers.sumatra.ids.ETeamColor;


public class AutoRefGlobalState
{
	private final Map<ETeamColor, Integer> failedBallPlacements = new EnumMap<>(ETeamColor.class);
	
	
	public Map<ETeamColor, Integer> getFailedBallPlacements()
	{
		return failedBallPlacements;
	}
}
