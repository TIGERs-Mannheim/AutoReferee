/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 12, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.util;

import java.awt.Image;

import javax.swing.ImageIcon;


/**
 * Util class for scaling images
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public final class ImageScaler
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private ImageScaler()
	{
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param imageIcon
	 * @param width
	 * @param height
	 * @return
	 */
	public static ImageIcon scaleImageIcon(final ImageIcon imageIcon, final int width, final int height)
	{
		return scale(imageIcon, width, height, java.awt.Image.SCALE_DEFAULT);
	}
	
	
	/**
	 * Scale the image and reduce aliasing artifacts
	 * 
	 * @param imageIcon
	 * @param width
	 * @param height
	 * @return
	 */
	public static ImageIcon scaleImageIconSmooth(final ImageIcon imageIcon, final int width, final int height)
	{
		return scale(imageIcon, width, height, java.awt.Image.SCALE_SMOOTH);
	}
	
	
	private static ImageIcon scale(final ImageIcon imageIcon, final int width, final int height, final int hints)
	{
		Image image = imageIcon.getImage();
		Image newimg = image.getScaledInstance(width, height, hints);
		return new ImageIcon(newimg);
	}
	
	
	/**
	 * Scale an image from resources to default button size
	 * 
	 * @param path
	 * @return
	 */
	public static ImageIcon scaleDefaultButtonImageIcon(final String path)
	{
		ImageIcon imageIcon = new ImageIcon(ImageScaler.class.getResource(path));
		return scaleImageIcon(imageIcon, ScalingUtil.getImageButtonSize(), ScalingUtil.getImageButtonSize());
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
