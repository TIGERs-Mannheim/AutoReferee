/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.visualizer;

import edu.tigers.sumatra.visualizer.field.FieldPanel;
import edu.tigers.sumatra.visualizer.options.VisualizerOptionsMenu;
import lombok.extern.log4j.Log4j2;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.io.Serial;


/**
 * Visualizes the current game situation.
 * It also allows the user to set a robot at a determined position.
 */
@Log4j2
public class VisualizerPanel extends JPanel
{
	@Serial
	private static final long serialVersionUID = 2686191777355388548L;


	public VisualizerPanel(FieldPanel fieldPanel, VisualizerOptionsMenu optionsMenu)
	{
		setLayout(new BorderLayout());
		add(optionsMenu, BorderLayout.PAGE_START);
		add(fieldPanel, BorderLayout.CENTER);
	}
}
