/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 19, 2015
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoref.gui.view;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import edu.dhbw.mannheim.tigers.sumatra.presenter.log.LogView;
import edu.tigers.autoref.view.AutoRefView;
import edu.tigers.autoref.view.BallSpeedView;
import edu.tigers.autoref.view.GameLogView;
import edu.tigers.autoref.view.VisualizerAutoRefView;
import edu.tigers.sumatra.AMainFrame;
import edu.tigers.sumatra.config.ConfigEditorView;


/**
 * @author "Lukas Magel"
 */
public class AutoRefMainFrame extends AMainFrame
{
	
	private static final long	serialVersionUID	= 8459059861313702417L;
	
	
	/**
	 * 
	 */
	public AutoRefMainFrame()
	{
		setTitle("Autoreferee");
		
		addView(new LogView(true));
		addView(new VisualizerAutoRefView());
		addView(new ConfigEditorView());
		addView(new AutoRefView());
		addView(new GameLogView());
		addView(new BallSpeedView());
		
		updateViewMenu();
		fillMenuBar();
	}
	
	
	/**
	 * 
	 */
	private void fillMenuBar()
	{
		JMenu fileMenu = new JMenu("File");
		JMenuItem exitItem = new JMenuItem("Exit");
		exitItem.addActionListener(new Exit());
		fileMenu.add(exitItem);
		
		getJMenuBar().add(fileMenu);
		
		/*
		 * Adds the menu items for layout and views
		 */
		super.addMenuItems();
		
	}
	
	
	@Override
	protected ImageIcon getFrameIcon()
	{
		return loadIconImage("whistle.png");
	}
}
