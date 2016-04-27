/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 21, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.bot;

/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class MoveConstraints
{
	private double	velLimitDefault	= -1;
	private double	accLimitDefault	= -1;
	private double	velLimit				= 0;
	private double	accLimit				= 0;
	
	
	/**
	 * @return the velLimit
	 */
	public double getVelLimit()
	{
		return velLimit;
	}
	
	
	/**
	 * @return the accLimit
	 */
	public double getAccLimit()
	{
		return accLimit;
	}
	
	
	/**
	 * @param velLimit the velLimit to set
	 * @return this for chaining
	 */
	public MoveConstraints setVelLimit(final double velLimit)
	{
		this.velLimit = velLimit;
		return this;
	}
	
	
	/**
	 * @param accLimit the accLimit to set
	 * @return this for chaining
	 */
	public MoveConstraints setAccLimit(final double accLimit)
	{
		this.accLimit = accLimit;
		return this;
	}
	
	
	/**
	 * 
	 */
	public void setDefaultVelLimit()
	{
		velLimit = velLimitDefault;
	}
	
	
	/**
	 * 
	 */
	public void setDefaultAccLimit()
	{
		accLimit = accLimitDefault;
	}
	
	
	/**
	 * @param velLimitDefault the velLimitDefault to set
	 */
	public void setVelLimitDefault(final double velLimitDefault)
	{
		this.velLimitDefault = velLimitDefault;
		if (velLimit == 0)
		{
			velLimit = velLimitDefault;
		}
	}
	
	
	/**
	 * @param accLimitDefault the accLimitDefault to set
	 */
	public void setAccLimitDefault(final double accLimitDefault)
	{
		this.accLimitDefault = accLimitDefault;
		if (accLimit == 0)
		{
			accLimit = accLimitDefault;
		}
	}
}
