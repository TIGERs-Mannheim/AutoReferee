/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.visualizer.options;


import com.jidesoft.swing.CheckBoxTree;
import edu.tigers.sumatra.components.BetterScrollPane;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import java.awt.BorderLayout;


@Log4j2
public class ShapeSelectionPanel extends JPanel
{
	@Getter
	private final JToolBar toolBar = new JToolBar();

	@Getter
	private final CheckBoxTree tree = new CheckBoxTree();

	@Getter
	private final JButton expandAll = new JButton("Exp");
	@Getter
	private final JButton collapseAll = new JButton("Col");


	public ShapeSelectionPanel()
	{
		setLayout(new BorderLayout());

		toolBar.setFloatable(false);
		toolBar.add(expandAll);
		toolBar.add(collapseAll);

		var scrollPane = new BetterScrollPane(tree);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

		add(toolBar, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
	}


}
