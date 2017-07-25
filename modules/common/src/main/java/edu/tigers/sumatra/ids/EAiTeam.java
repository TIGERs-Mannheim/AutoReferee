/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ids;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public enum EAiTeam
{
	/** */
	YELLOW_PRIMARY(ETeamColor.YELLOW, EAiType.PRIMARY, true),
	/** */
	BLUE_PRIMARY(ETeamColor.BLUE, EAiType.PRIMARY, true),
	
	/** */
	YELLOW_SECONDARY(ETeamColor.YELLOW, EAiType.SECONDARY, false),
	/** */
	BLUE_SECONDARY(ETeamColor.BLUE, EAiType.SECONDARY, false),;
	
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
			return BLUE_PRIMARY;
		} else if (teamColor == ETeamColor.YELLOW)
		{
			return YELLOW_PRIMARY;
		}
		throw new IllegalArgumentException("Can not map team color: " + teamColor);
	}
	
	
	/**
	 * Get the secondary team of the given team color
	 *
	 * @param teamColor
	 * @return
	 */
	public static EAiTeam secondary(ETeamColor teamColor)
	{
		if (teamColor == ETeamColor.BLUE)
		{
			return BLUE_SECONDARY;
		} else if (teamColor == ETeamColor.YELLOW)
		{
			return YELLOW_SECONDARY;
		}
		throw new IllegalArgumentException("Can not map team color: " + teamColor);
	}

	/**
	 * Create EAiTeam from color and type
	 *
	 * @param color
	 * @param type
	 * @return
	 */
	public static EAiTeam ofTypeAndColor(ETeamColor color, EAiType type) {
		if (color == ETeamColor.YELLOW && type == EAiType.PRIMARY) {
			return EAiTeam.YELLOW_PRIMARY;
		} else if (color == ETeamColor.YELLOW && type == EAiType.SECONDARY) {
			return EAiTeam.YELLOW_SECONDARY;
		} else if (color == ETeamColor.BLUE && type == EAiType.PRIMARY) {
			return EAiTeam.BLUE_PRIMARY;
		} else if (color == ETeamColor.BLUE && type == EAiType.SECONDARY) {
			return EAiTeam.BLUE_SECONDARY;
		} else {
			throw new IllegalArgumentException("Combination " + color.toString() + " and " + type.toString() + " not supported.");
		}
	}
	
	
	/**
	 * @return true, if it is a primary AI
	 */
	public boolean isPrimary()
	{
		return this == YELLOW_PRIMARY || this == BLUE_PRIMARY;
	}
	
	
	/**
	 * @return true, if it is a secondary AI
	 */
	public boolean isSecondary()
	{
		return this == YELLOW_SECONDARY || this == BLUE_SECONDARY;
	}
}
