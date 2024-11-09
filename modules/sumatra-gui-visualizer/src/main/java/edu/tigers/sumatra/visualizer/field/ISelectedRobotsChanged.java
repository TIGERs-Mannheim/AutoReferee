/*
 * Copyright (c) 2009 - 2024, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.visualizer.field;

import edu.tigers.sumatra.ids.BotID;

import java.util.List;


public interface ISelectedRobotsChanged
{
	void selectedRobotsChanged(List<BotID> selectedBots);
}
