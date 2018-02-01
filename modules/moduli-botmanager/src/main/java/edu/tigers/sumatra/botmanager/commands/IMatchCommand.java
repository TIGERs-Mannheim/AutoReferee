/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jul 30, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.botmanager.commands;

import edu.tigers.sumatra.botmanager.commands.botskills.ABotSkill;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface IMatchCommand
{
	
	/**
	 * @param skill
	 */
	void setSkill(ABotSkill skill);
	
	
	/**
	 * @param enable
	 */
	void setKickerAutocharge(final boolean enable);
	
	
	/**
	 * @return
	 */
	ABotSkill getSkill();
	
	
	/**
	 * @param control
	 */
	void setMultimediaControl(final MultimediaControl control);
	
	
	/**
	 * @param enable
	 */
	public void setStrictVelocityLimit(final boolean enable);
}