/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.visualizer.options;

import edu.tigers.sumatra.drawable.IShapeLayer;
import lombok.Getter;


/**
 * Available visualizer options and their names
 */
@SuppressWarnings("squid:S1192") // duplicated string literals
@Getter
public enum EVisualizerOptions implements IShapeLayer
{
	FANCY("Visualizer", "Fancy drawing"),
	TURN_NEXT("Visualizer", "Horizontal field"),
	RESET_FIELD("Visualizer", "Reset field"),
	DARK("Visualizer", "Dark mode", false),

	;


	private final String layerName;
	private final String category;
	private final boolean visibleByDefault;


	EVisualizerOptions(final String category, final String layerName)
	{
		this(category, layerName, true);
	}


	/**
	 * @param layerName this name will be shown to the user
	 */
	EVisualizerOptions(final String category, final String layerName, final boolean visibleByDefault)
	{
		this.layerName = layerName;
		this.category = category;
		this.visibleByDefault = visibleByDefault;
	}


	@Override
	public String getId()
	{
		return name();
	}
}
