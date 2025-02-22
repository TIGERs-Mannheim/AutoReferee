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
public class DependencyException extends Exception
{
	private static final long serialVersionUID = -688270423190284593L;


	/**
	 * @param msg of the exception
	 */
	public DependencyException(final String msg)
	{
		super(msg);
	}


	public DependencyException(final String message, final Throwable cause)
	{
		super(message, cause);
	}
}
