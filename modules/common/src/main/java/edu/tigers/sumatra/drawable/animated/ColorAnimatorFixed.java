/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - Tigers Mannheim
 */
package edu.tigers.sumatra.drawable.animated;

import java.awt.Color;


/**
 * Actually not an animator. Uses a fixed value.
 * 
 * @author AndreR <andre@ryll.cc>
 */
public class ColorAnimatorFixed implements IColorAnimator
{
	private final Color color;
	
	
	@SuppressWarnings("unused")
	private ColorAnimatorFixed()
	{
		color = null;
	}
	
	
	/**
	 * @param color
	 */
	public ColorAnimatorFixed(final Color color)
	{
		this.color = color;
	}
	
	
	@Override
	public Color getColor()
	{
		return color;
	}
	
}
