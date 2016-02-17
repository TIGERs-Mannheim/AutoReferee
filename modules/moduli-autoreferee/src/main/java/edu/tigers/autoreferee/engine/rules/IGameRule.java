/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 7, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.rules;

import java.util.Comparator;
import java.util.Optional;

import edu.tigers.autoreferee.engine.IRuleEngineFrame;
import edu.tigers.sumatra.wp.data.EGameStateNeutral;


/**
 * Interface that encapsulates a rule which is evaluated with each frame during the execution of the autoref.
 * With each call of the update method a rule can signal back its wish to send a command to the refbox, queue a follow
 * up action that is to be executed after the command has been sent, report a rule violation that is logged.
 * A rule is active in certain game states (see {@link EGameStateNeutral}). The rule will only be evaluated if the call
 * to {@link IGameRule#isActiveIn(EGameStateNeutral)} evaluates to true. The {@link IGameRule#reset()} method is called
 * when the rule is evaluated for the first time after the game state changes to a state that the rule is active in.
 * 
 * @author "Lukas Magel"
 */
public interface IGameRule
{
	/**
	 * @author "Lukas Magel"
	 */
	public static class GameRuleComparator implements
			Comparator<IGameRule>
	{
		
		/**  */
		public static GameRuleComparator	INSTANCE	= new GameRuleComparator();
		
		
		@Override
		public int compare(final IGameRule o1, final IGameRule o2)
		{
			int prio1 = o1.getPriority();
			int prio2 = o2.getPriority();
			if (prio1 > prio2)
			{
				return -1;
			} else if (prio1 < prio2)
			{
				return 1;
			} else
			{
				return 0;
			}
		}
	}
	
	
	/**
	 * @param state
	 * @return
	 */
	public boolean isActiveIn(EGameStateNeutral state);
	
	
	/**
	 * @return
	 */
	public int getPriority();
	
	
	/**
	 * @param frame
	 * @return
	 */
	Optional<RuleResult> update(IRuleEngineFrame frame);
	
	
	/**
	 *
	 */
	public void reset();
	
	
}
