/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.moduli.modules;

import edu.tigers.moduli.AModule;


public class ConfiguredTestModule extends AModule
{
	private String configProperty;


	@Override
	public void initModule()
	{
		configProperty = getSubnodeConfiguration().getString("testProperty");
	}


	public String getConfigProperty()
	{
		return configProperty;
	}
}
