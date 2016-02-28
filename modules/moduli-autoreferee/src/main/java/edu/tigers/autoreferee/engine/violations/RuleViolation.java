/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 8, 2015
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.violations;

import java.util.Optional;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;


/**
 * @author "Lukas Magel"
 */
public class RuleViolation implements IRuleViolation
{
	private final ERuleViolation	violationType;
	private final long				timestamp;						// ns
	private final ETeamColor		teamAtFault;
	private final BotID				botAtFault;
	
	private String						cachedLogString	= null;
	
	
	/**
	 * @param violationType
	 * @param timestamp in ns
	 * @param botAtFault
	 */
	public RuleViolation(final ERuleViolation violationType, final long timestamp,
			final BotID botAtFault)
	{
		this.violationType = violationType;
		this.timestamp = timestamp;
		this.botAtFault = botAtFault;
		teamAtFault = botAtFault.getTeamColor();
	}
	
	
	/**
	 * @param violationType
	 * @param timestamp
	 * @param teamAtFault
	 */
	public RuleViolation(final ERuleViolation violationType, final long timestamp, final ETeamColor teamAtFault)
	{
		this.violationType = violationType;
		this.timestamp = timestamp;
		this.teamAtFault = teamAtFault;
		botAtFault = null;
	}
	
	
	/**
	 * @return
	 */
	@Override
	public ERuleViolation getType()
	{
		return violationType;
	}
	
	
	/**
	 * @return timestamp in ns
	 */
	@Override
	public long getTimestamp()
	{
		return timestamp;
	}
	
	
	/**
	 * @return
	 */
	@Override
	public ETeamColor getTeamAtFault()
	{
		return teamAtFault;
	}
	
	
	@Override
	public Optional<BotID> getBotAtFault()
	{
		return Optional.ofNullable(botAtFault);
	}
	
	
	@Override
	public String buildLogString()
	{
		if (cachedLogString == null)
		{
			synchronized (this)
			{
				if (cachedLogString == null)
				{
					cachedLogString = generateLogString();
				}
			}
		}
		return cachedLogString;
	}
	
	
	protected String generateLogString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Violation: ");
		builder.append(getType());
		if (botAtFault != null)
		{
			builder.append(" | Bot: ");
			builder.append(botAtFault);
		} else
		{
			builder.append(" | Team: ");
			builder.append(teamAtFault);
		}
		return builder.toString();
	}
	
	
	@Override
	public String toString()
	{
		return buildLogString();
	}
}
