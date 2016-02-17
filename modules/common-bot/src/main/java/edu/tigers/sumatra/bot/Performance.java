/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 10, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.bot;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import com.sleepycat.persist.model.Persistent;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class Performance
{
	
	
	@Configurable(spezis = { "GRSIM" }, defValue = "2")
	private double	accMax			= 2;
	@Configurable(spezis = { "GRSIM" }, defValue = "2")
	private double	brkMax			= 2;
											
	@Configurable(spezis = { "GRSIM" }, defValue = "2")
	private double	velMax			= 2.0;
											
	@Configurable(spezis = { "GRSIM" }, defValue = "30")
	private double	accMaxW			= 30;
	@Configurable(spezis = { "GRSIM" }, defValue = "30")
	private double	brkMaxW			= 30;
											
	@Configurable(spezis = { "GRSIM" }, defValue = "10")
	private double	velMaxW			= 10;
											
	private double	velMaxOverride	= -1;
											
											
	static
	{
		ConfigRegistration.registerClass("botmgr", Performance.class);
	}
	
	
	/**
	 * 
	 */
	public Performance()
	{
	}
	
	
	/**
	 * @return the accMax
	 */
	public final double getAccMax()
	{
		return accMax;
	}
	
	
	/**
	 * @return the accMaxW
	 */
	public final double getAccMaxW()
	{
		return accMaxW;
	}
	
	
	/**
	 * @return the brkMax
	 */
	public final double getBrkMax()
	{
		return brkMax;
	}
	
	
	/**
	 * @return the brkMaxW
	 */
	public final double getBrkMaxW()
	{
		return brkMaxW;
	}
	
	
	/**
	 * @return the velMax
	 */
	public final double getVelMax()
	{
		if (velMaxOverride > 0)
		{
			return velMaxOverride;
		}
		return velMax;
	}
	
	
	/**
	 * @return the velMaxW
	 */
	public final double getVelMaxW()
	{
		return velMaxW;
	}
	
	
	/**
	 * @param accMax the accMax to set
	 */
	public final void setAccMax(final double accMax)
	{
		this.accMax = accMax;
	}
	
	
	/**
	 * @param accMaxW the accMaxW to set
	 */
	public final void setAccMaxW(final double accMaxW)
	{
		this.accMaxW = accMaxW;
	}
	
	
	/**
	 * @param brkMax the brkMax to set
	 */
	public final void setBrkMax(final double brkMax)
	{
		this.brkMax = brkMax;
	}
	
	
	/**
	 * @param brkMaxW the brkMaxW to set
	 */
	public final void setBrkMaxW(final double brkMaxW)
	{
		this.brkMaxW = brkMaxW;
	}
	
	
	/**
	 * @param velMax the velMax to set
	 */
	public final void setVelMax(final double velMax)
	{
		this.velMax = velMax;
	}
	
	
	/**
	 * @param velMaxW the velMaxW to set
	 */
	public final void setVelMaxW(final double velMaxW)
	{
		this.velMaxW = velMaxW;
	}
	
	
	/**
	 * @return the velMaxOverride
	 */
	public final double getVelMaxOverride()
	{
		return velMaxOverride;
	}
	
	
	/**
	 * @param velMaxOverride the velMaxOverride to set
	 */
	public final void setVelMaxOverride(final double velMaxOverride)
	{
		this.velMaxOverride = velMaxOverride;
	}
	
	
	/**
	 * @param enable
	 */
	public final void setVelMaxOverride(final boolean enable)
	{
		velMaxOverride = enable ? 1.2 : -1;
	}
}
