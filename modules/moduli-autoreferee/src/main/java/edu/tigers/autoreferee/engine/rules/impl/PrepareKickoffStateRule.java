/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 7, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.rules.impl;

import java.util.Arrays;
import java.util.Optional;

import com.github.g3force.configurable.Configurable;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Command;
import edu.tigers.autoreferee.engine.IRuleEngineFrame;
import edu.tigers.autoreferee.engine.rules.RuleResult;
import edu.tigers.sumatra.wp.data.EGameStateNeutral;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;


/**
 * This rule waits for the teams to settle and take their positions before a kickoff. When all bots are on their sides
 * and the ball/bots are stationary the kickoff command is issued.
 * 
 * @author "Lukas Magel"
 */
public class PrepareKickoffStateRule extends APreparingGameRule
{
	private static int	priority		= 1;
	
	@Configurable(comment = "The minimum time to wait before sending the kickoff signal in ms")
	private int				minWaitTime	= 5_000;
	
	@Configurable(comment = "The maximum time to wait before sending the kickoff signal in ms")
	private int				maxWaitTime	= 20_000;
	
	private long			entryTime;
	
	static
	{
		AGameRule.registerClass(PrepareKickoffStateRule.class);
	}
	
	
	/**
	 *
	 */
	public PrepareKickoffStateRule()
	{
		super(Arrays.asList(EGameStateNeutral.PREPARE_KICKOFF_BLUE, EGameStateNeutral.PREPARE_KICKOFF_YELLOW));
	}
	
	
	@Override
	public int getPriority()
	{
		return priority;
	}
	
	
	@Override
	protected void prepare(final IRuleEngineFrame frame)
	{
		entryTime = frame.getTimestamp();
	}
	
	
	@Override
	public Optional<RuleResult> doUpdate(final IRuleEngineFrame frame)
	{
		if ((frame.getTimestamp() - entryTime) < (minWaitTime * 1_000_000))
		{
			return Optional.empty();
		}
		
		SimpleWorldFrame wFrame = frame.getWorldFrame();
		boolean ballStationary = ballIsStationary(wFrame.getBall());
		boolean botsStationary = botsAreStationary(wFrame.getBots().values());
		boolean botPosCorrect = botsAreOnCorrectSide(wFrame.getBots().values());
		boolean maxWaitTimeElapsed = (frame.getTimestamp() - entryTime) > (maxWaitTime * 1_000_000);
		
		if ((ballStationary && botsStationary && botPosCorrect) || maxWaitTimeElapsed)
		{
			return Optional.of(new RuleResult(Command.NORMAL_START, null, null));
		}
		return Optional.empty();
		
	}
	
}
