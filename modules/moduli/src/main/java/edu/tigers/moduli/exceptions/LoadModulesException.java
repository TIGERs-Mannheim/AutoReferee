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
 * Exception if the module-system isn't able to resolve the modules.
 */
public class LoadModulesException extends Exception
{
	private static final long serialVersionUID = -5850251277618067045L;


	/**
	 * @param msg of the exception
	 * @param cause of the exception
	 */
	public LoadModulesException(final String msg, final Throwable cause)
	{
		super(msg, cause);
	}


	/**
	 * @param msg of the exception
	 */
	public LoadModulesException(final String msg)
	{
		super(msg);
	}
}
