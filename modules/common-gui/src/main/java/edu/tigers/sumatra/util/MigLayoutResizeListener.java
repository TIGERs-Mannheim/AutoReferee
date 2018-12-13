/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.util;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JPanel;

import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;


/**
 * Change the number of panels per row in a MigLayout to 1, when 2 panels no longer fit next to each other.
 *
 * @author David Risch <DavidR@tigers-mannheim.de>
 */
public class MigLayoutResizeListener extends ComponentAdapter
{
	private final JPanel outerPanel;
	private final JPanel migPanel;
	private final MigLayout migLayout;
	
	
	/**
	 * @param outerPanel JPanel which surrounds the MigLayout JPanel
	 * @param migLayoutPanel JPanel with the MigLayout as it's layout
	 */
	public MigLayoutResizeListener(JPanel outerPanel, JPanel migLayoutPanel)
	{
		this.outerPanel = outerPanel;
		this.migPanel = migLayoutPanel;
		this.migLayout = ((MigLayout) migPanel.getLayout());
	}
	
	
	@Override
	public void componentResized(ComponentEvent e)
	{
		if (migPanel.getComponentCount() <= 1)
		{
			return; // wrap is irrelevant if there is only one panel (or none)
		}
		
		int widthOfTwoPanels = migPanel.getComponent(0).getWidth() + migPanel.getComponent(1).getWidth();
		boolean canFitTwoColumns = (outerPanel.getWidth() - 35) > widthOfTwoPanels;
		
		Object layoutConstraintsObject = migLayout.getLayoutConstraints();
		
		// layoutConstraintsObject can be a String or a net.miginfocom.layout.LC
		if (layoutConstraintsObject instanceof String)
		{
			String layoutConstraints = (String) layoutConstraintsObject;
			
			layoutConstraints = layoutConstraints.replaceAll(
					"wrap " + (canFitTwoColumns ? 1 : 2), "wrap " + (canFitTwoColumns ? 2 : 1));
			
			migLayout.setLayoutConstraints(layoutConstraints);
		} else
		{
			LC layoutConstraints = (LC) layoutConstraintsObject;
			
			layoutConstraints.setWrapAfter(canFitTwoColumns ? 2 : 1);
			
			migLayout.setLayoutConstraints(layoutConstraints);
		}
		migPanel.updateUI();
	}
}
