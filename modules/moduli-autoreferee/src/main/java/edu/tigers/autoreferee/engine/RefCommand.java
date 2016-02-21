/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 9, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine;

import java.util.Optional;

import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.math.IVector2;


/**
 * This class is a wrapper around the {@link Command} enum. It stores an additional position for the ball placement
 * commands.
 * 
 * @author "Lukas Magel"
 */
public class RefCommand
{
	private final Command	command;
	private final IVector2	kickPos;
	
	
	/**
	 * @param command
	 */
	public RefCommand(final Command command)
	{
		this(command, null);
	}
	
	
	/**
	 * @param command
	 * @param kickPos
	 */
	public RefCommand(final Command command, final IVector2 kickPos)
	{
		this.command = command;
		this.kickPos = kickPos;
	}
	
	
	/**
	 * @return the command
	 */
	public Command getCommand()
	{
		return command;
	}
	
	
	/**
	 * @return the kickPos
	 */
	public Optional<IVector2> getKickPos()
	{
		return Optional.ofNullable(kickPos);
	}
	
	
	@Override
	public boolean equals(final Object obj)
	{
		if (obj == null)
		{
			return false;
		}
		if (this == obj)
		{
			return true;
		}
		if (obj instanceof RefCommand)
		{
			RefCommand other = (RefCommand) obj;
			if (command != other.command)
			{
				return false;
			}
			if (kickPos == null)
			{
				if (other.kickPos == null)
				{
					return true;
				}
				return false;
			}
			return kickPos.equals(other.kickPos);
		}
		return false;
	}
	
	
	@Override
	public int hashCode()
	{
		int prime = 31;
		int result = 1;
		result = (prime * result) + command.hashCode();
		if (kickPos != null)
		{
			result = (prime * result) + kickPos.hashCode();
		}
		return result;
	}
}
