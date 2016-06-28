/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 12, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoref.view.humanref.components;

import java.awt.Color;
import java.text.DecimalFormat;
import java.time.Duration;

import javax.swing.JLabel;

import edu.tigers.autoref.view.generic.JFormatLabel.LabelFormatter;


/**
 * @author "Lukas Magel"
 */
public class DurationFormatter implements LabelFormatter<Duration>
{
	
	private Duration							lowValue;
	private static final DecimalFormat	format	= new DecimalFormat("00");
	
	
	/**
	 * @param lowValue
	 */
	public DurationFormatter(final Duration lowValue)
	{
		this.lowValue = lowValue;
	}
	
	
	private String getText(Duration value)
	{
		StringBuilder strBuilder = new StringBuilder();
		if (value.isNegative())
		{
			strBuilder.append("-");
			value = value.negated();
		}
		
		long minutes = (int) (value.toMinutes() % 60);
		long seconds = (int) (value.getSeconds() % 60);
		
		strBuilder.append(format.format(minutes));
		strBuilder.append(":");
		strBuilder.append(format.format(seconds));
		return strBuilder.toString();
	}
	
	
	private Color getColor(final Duration value)
	{
		if (value.compareTo(lowValue) < 0)
		{
			return Color.RED;
		}
		return Color.BLACK;
	}
	
	
	@Override
	public void formatLabel(final Duration value, final JLabel label)
	{
		label.setText(getText(value));
		label.setForeground(getColor(value));
	}
	
}
