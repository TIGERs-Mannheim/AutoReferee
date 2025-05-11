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
 * Exception if the module-system isn't able to start the modules.
 */
public class StartModuleException extends RuntimeException
{
	/**
	 * @param msg of the exception
	 * @param cause of the exception
	 */
	public StartModuleException(final String msg, final Throwable cause)
	{
		super(msg, cause);
	}
}
