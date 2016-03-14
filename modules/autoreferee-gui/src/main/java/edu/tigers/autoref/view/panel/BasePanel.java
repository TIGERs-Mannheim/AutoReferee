/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 12, 2016
 * Author(s): Lukas Magel
 * *********************************************************
 */
package edu.tigers.autoref.view.panel;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;


/**
 * @author Lukas Magel
 * @param <T>
 */
public abstract class BasePanel<T> extends JPanel
{
	
	/**  */
	private static final long	serialVersionUID	= 1L;
	
	protected List<T>				observer				= new ArrayList<>();
	
	
	/**
	 * @param observer
	 */
	public void addObserver(final T observer)
	{
		this.observer.add(observer);
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final T observer)
	{
		this.observer.remove(observer);
	}
	
	
	/**
	 * @param enabled
	 */
	public abstract void setPanelEnabled(boolean enabled);
}
