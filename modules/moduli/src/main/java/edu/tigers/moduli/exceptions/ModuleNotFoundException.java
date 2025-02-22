/*
 * *********************************************************
 * Copyright (c) 2010 DLR Oberpfaffenhofen KN
 * Project: flightControl
 * Date: Mar 5, 2010
 * Authors:
 * Bernhard Perun <bernhard.perun@dlr.de>
 * *********************************************************
 */
package edu.tigers.moduli.exceptions;

/**
 * Exception if the module-system isn't able to find a module.
 */
public class ModuleNotFoundException extends RuntimeException
{
	private static final long serialVersionUID = -3273863493959166184L;


	/**
	 * @param msg of the exception
	 */
	public ModuleNotFoundException(final String msg)
	{
		super(msg);
	}
}
