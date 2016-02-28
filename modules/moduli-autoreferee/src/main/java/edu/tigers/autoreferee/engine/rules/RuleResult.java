/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 7, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.rules;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.RefCommand;
import edu.tigers.autoreferee.engine.violations.IRuleViolation;
import edu.tigers.sumatra.Referee.SSL_Referee.Command;


/**
 * Encapsulates the return value of a rule evaluation
 * 
 * @author "Lukas Magel"
 */
public class RuleResult
{
	private final List<RefCommand>	commands;
	private final FollowUpAction		followUp;
	private final IRuleViolation		violation;
	
	
	/**
	 * @param refCommand
	 * @param followUp
	 * @param violation
	 */
	public RuleResult(final Command refCommand, final FollowUpAction followUp, final IRuleViolation violation)
	{
		this(new RefCommand(refCommand), followUp, violation);
	}
	
	
	/**
	 * @param command
	 * @param followUp
	 * @param violation
	 */
	public RuleResult(final RefCommand command, final FollowUpAction followUp, final IRuleViolation violation)
	{
		this(Arrays.asList(command), followUp, violation);
	}
	
	
	/**
	 * @param commands
	 * @param followUp
	 * @param violation
	 */
	public RuleResult(final List<RefCommand> commands, final FollowUpAction followUp, final IRuleViolation violation)
	{
		if (commands == null)
		{
			this.commands = Collections.emptyList();
		} else
		{
			this.commands = commands;
		}
		this.followUp = followUp;
		this.violation = violation;
	}
	
	
	/**
	 * @return the refCommand
	 */
	public List<RefCommand> getCommands()
	{
		return commands;
	}
	
	
	/**
	 * @return the followUp
	 */
	public Optional<FollowUpAction> getFollowUp()
	{
		return Optional.ofNullable(followUp);
	}
	
	
	/**
	 * @return the violation
	 */
	public Optional<IRuleViolation> getViolation()
	{
		return Optional.ofNullable(violation);
	}
}
