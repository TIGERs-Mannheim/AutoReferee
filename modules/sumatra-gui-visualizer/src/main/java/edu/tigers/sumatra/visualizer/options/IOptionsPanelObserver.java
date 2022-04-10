/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.visualizer.options;

/**
 * FieldPanel observer interface.
 */
public interface IOptionsPanelObserver
{
	/**
	 * @param string
	 * @param isSelected
	 */
	void onCheckboxClick(String string, boolean isSelected);


	/**
	 * @param option
	 * @param state
	 */
	void onActionFired(EVisualizerOptions option, boolean state);
}
