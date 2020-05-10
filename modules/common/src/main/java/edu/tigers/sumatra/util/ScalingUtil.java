/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.util;

import javax.swing.JTextPane;


/**
 * Util class for getting default GUI dimensions.
 * Needed for the GUI to scale properly on high DPI screens with gui scaling.
 *
 * @author David Risch <DavidR@tigers-mannheim.de>
 */
public class ScalingUtil
{
	public enum FontSize
	{
		SMALL,
		MEDIUM,
		LARGE
	}

	private static int fontSizeSmall;
	private static int fontSizeMedium;
	private static int fontSizeLarge;
	private static int imageButtonSize;
	private static int tableRowHeight;

	// Size of various GUI elements relative to the default text size set by the selected LookAndFeel
	private static final double TABLE_ROW_FACTOR = 1.5;
	private static final double IMAGE_BUTTON_FACTOR = 2.25;
	private static final double FONT_SMALL_FACTOR = 0.8;
	private static final double FONT_LARGE_FACTOR = 1.2;

	static
	{
		// Make sure the sizes are initialized, even when running without a UI
		update();
	}

	private ScalingUtil()
	{
	}


	public static int getFontSize(FontSize fontSize)
	{
		if (fontSize == FontSize.SMALL)
		{
			return fontSizeSmall;
		} else if (fontSize == FontSize.LARGE)
		{
			return fontSizeLarge;
		} else
		{
			return fontSizeMedium;
		}
	}


	public static int getImageButtonSize()
	{
		return imageButtonSize;
	}


	public static int getTableRowHeight()
	{
		return tableRowHeight;
	}


	/**
	 * Gets the default font size of the active LookAndFeel.
	 * If the LookAndFeel supports GUI scaling this value will depend on the scale factor.
	 * This function must only be called after the LookAndFeel has been set (in MainPresenter.onSelectLookAndFeel)
	 */
	public static void update()
	{
		double baselineSize = new JTextPane().getFont().getSize();
		fontSizeSmall = (int) Math.ceil(baselineSize * FONT_SMALL_FACTOR);
		fontSizeMedium = (int) Math.ceil(baselineSize * 1.0);
		fontSizeLarge = (int) Math.ceil(baselineSize * FONT_LARGE_FACTOR);

		imageButtonSize = (int) Math.ceil(baselineSize * IMAGE_BUTTON_FACTOR);

		tableRowHeight = (int) Math.ceil(baselineSize * TABLE_ROW_FACTOR);
	}
}
