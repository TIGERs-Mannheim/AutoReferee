/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 12, 2016
 * Author(s): Lukas Magel
 * *********************************************************
 */
package edu.tigers.sumatra.components;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JPanel;


/**
 * Generic panel base class which groups commonly needed functionality like an observer mechanism as well as a method to
 * enabled/disable the panel and all of its components
 * 
 * @author Lukas Magel
 * @param <T>
 */
public abstract class BasePanel<T> extends JPanel
{
	
	/**  */
	private static final long	serialVersionUID	= 1L;
	
	private static final int	ALPHA_MAX			= 255;
	
	private int						alpha					= ALPHA_MAX;
	private List<T>				observer				= new ArrayList<>();
	
	
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
	
	
	protected List<T> getObserver()
	{
		return observer;
	}
	
	
	protected void informObserver(final Consumer<T> consumer)
	{
		observer.forEach(observer -> consumer.accept(observer));
	}
	
	
	/**
	 * @param value [0,255]
	 */
	public void setAlphaValue(final int value)
	{
		alpha = Math.min(ALPHA_MAX, Math.max(0, alpha));
	}
	
	
	/**
	 * @return [0,255]
	 */
	public float getAlphaValue()
	{
		return alpha;
	}
	
	
	@Override
	public void paint(final Graphics g)
	{
		if ((alpha < ALPHA_MAX) && (g instanceof Graphics2D))
		{
			Graphics2D g2 = (Graphics2D) g;
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) alpha / ALPHA_MAX));
			
		}
		super.paint(g);
	}
	
	
	/**
	 * Enable/disable the panel and all of its components
	 * 
	 * @param enabled
	 */
	public abstract void setPanelEnabled(boolean enabled);
	
}
