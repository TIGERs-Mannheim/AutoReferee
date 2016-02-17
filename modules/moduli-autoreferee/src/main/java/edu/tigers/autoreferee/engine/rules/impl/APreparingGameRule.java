/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 14, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.rules.impl;

import java.util.List;
import java.util.Optional;

import edu.tigers.autoreferee.engine.IRuleEngineFrame;
import edu.tigers.autoreferee.engine.rules.RuleResult;
import edu.tigers.sumatra.wp.data.EGameStateNeutral;


/**
 * Abstract implementation which provides a prepare method to do initial work
 * 
 * @author "Lukas Magel"
 */
public abstract class APreparingGameRule extends AGameRule
{
	
	private boolean	firstUpdate	= true;
	
	
	/**
	 * @param gamestate
	 */
	public APreparingGameRule(final EGameStateNeutral gamestate)
	{
		super(gamestate);
	}
	
	
	/**
	 * @param gamestates
	 */
	public APreparingGameRule(final List<EGameStateNeutral> gamestates)
	{
		super(gamestates);
	}
	
	
	@Override
	public Optional<RuleResult> update(final IRuleEngineFrame frame)
	{
		if (firstUpdate)
		{
			prepare(frame);
			firstUpdate = false;
		}
		return doUpdate(frame);
	}
	
	
	protected abstract void prepare(IRuleEngineFrame frame);
	
	
	protected abstract Optional<RuleResult> doUpdate(IRuleEngineFrame frame);
	
	
	@Override
	public void reset()
	{
		firstUpdate = true;
		doReset();
	}
	
	
	protected void doReset()
	{
	}
	
}
