/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.referee;

import edu.tigers.sumatra.referee.proto.SslGcEngineConfig;


public interface IGameControllerApiObserver
{
	void onConfigChange(SslGcEngineConfig.Config config);
}
