/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.moduli.modules;

import edu.tigers.moduli.AModule;


public abstract class TestModule extends AModule
{
	public abstract boolean isConstructed();
	
	
	public abstract boolean isInitialized();
	
	
	public abstract boolean isStarted();
	
	
	public abstract boolean isStopped();
	
	
	public abstract boolean isDeinitialized();
}
