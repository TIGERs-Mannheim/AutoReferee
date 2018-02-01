/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ids;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public enum EAiTeam
{
	/** */
	YELLOW(ETeamColor.YELLOW, EAiType.PRIMARY, true),
	/** */
	BLUE(ETeamColor.BLUE, EAiType.PRIMARY, true),

	;
	
	private ETeamColor teamColor;
	private EAiType aiType;
	private boolean activeByDefault;
	
	
	/**
	 * @param teamColor the associated team color
	 * @param aiType the ai type
	 * @param activeByDefault active by default
	 */
	EAiTeam(ETeamColor teamColor, EAiType aiType, boolean activeByDefault)
	{
		this.teamColor = teamColor;
		this.aiType = aiType;
		this.activeByDefault = activeByDefault;
	}
	
	
	public ETeamColor getTeamColor()
	{
		return teamColor;
	}
	
	
	public boolean isActiveByDefault()
	{
		return activeByDefault;
	}
	
	
	public EAiType getAiType()
	{
		return aiType;
	}
	
	
	/**
	 * Get the primary team of the given team color
	 * 
	 * @param teamColor
	 * @return
	 */
	public static EAiTeam primary(ETeamColor teamColor)
	{
		if (teamColor == ETeamColor.BLUE)
		{
			return BLUE;
		} else if (teamColor == ETeamColor.YELLOW)
		{
			return YELLOW;
		}
		throw new IllegalArgumentException("Can not map team color: " + teamColor);
	}

	
	/**
	 * @return true, if it is a primary AI
	 */
	public boolean isPrimary()
	{
		return aiType == EAiType.PRIMARY;
	}


    /**
     * Check if color of aiteam matches color
     *
     * @param color
     * @return
     */
    public boolean matchesColor(ETeamColor color)
    {
        return color == teamColor;
    }


    /**
     * Check if type of aiteam matches aitype
     *
     * @param type
     * @return
     */
    public boolean matchesType(EAiType type)
    {
        return type == aiType;
    }


    /**
     * Check if type and color match
     *
     * @param color
     * @param type
     * @return
     */
    public boolean matchesTypeAndColor(ETeamColor color, EAiType type)
    {
        return matchesColor(color) && matchesType(type);
    }
}
