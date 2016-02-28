/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 12, 2016
 * Author(s): Lukas Magel
 * *********************************************************
 */
package edu.tigers.autoref.view.startstop;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JMenu;

import net.miginfocom.swing.MigLayout;
import edu.tigers.autoref.view.ObservablePanel;
import edu.tigers.autoref.view.startstop.StartStopPanel.IStartStopPanelObserver;
import edu.tigers.autoreferee.engine.rules.AutoRefEngine.AutoRefMode;
import edu.tigers.sumatra.views.ISumatraView;


/**
 * @author Lukas Magel
 */
public class StartStopPanel extends ObservablePanel<IStartStopPanelObserver> implements ISumatraView
{
	/**
	 * @author Lukas Magel
	 */
	public interface IStartStopPanelObserver
	{
		/**
		 * 
		 */
		void onStartButtonPressed();
		
		
		/**
		 * 
		 */
		void onStopButtonPressed();
		
	}
	
	/**  */
	private static final long			serialVersionUID	= 1L;
	
	private JButton						startButton			= null;
	private JButton						stopButton			= null;
	private JComboBox<AutoRefMode>	refModeBox			= null;
	
	
	/**
	 * 
	 */
	public StartStopPanel()
	{
		setLayout(new MigLayout("center"));
		
		startButton = new JButton("Start");
		startButton.setEnabled(true);
		startButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				observer.forEach(obs -> obs.onStartButtonPressed());
			}
		});
		
		stopButton = new JButton("Stop");
		stopButton.setEnabled(false);
		stopButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				observer.forEach(obs -> obs.onStopButtonPressed());
			}
		});
		
		refModeBox = new JComboBox<>(AutoRefMode.values());
		
		add(refModeBox, "align center, span, grow, wrap");
		add(startButton);
		add(stopButton);
	}
	
	
	/**
	 * @param enabled
	 */
	public void setModeBoxEnabled(final boolean enabled)
	{
		refModeBox.setEnabled(enabled);
	}
	
	
	/**
	 * @return
	 */
	public JButton getStartButton()
	{
		return startButton;
	}
	
	
	/**
	 * @return
	 */
	public JButton getStopButton()
	{
		return stopButton;
	}
	
	
	/**
	 * @return
	 */
	public AutoRefMode getModeSetting()
	{
		return (AutoRefMode) refModeBox.getSelectedItem();
	}
	
	
	@Override
	public List<JMenu> getCustomMenus()
	{
		return null;
	}
	
	
	@Override
	public void onShown()
	{
	}
	
	
	@Override
	public void onHidden()
	{
	}
	
	
	@Override
	public void onFocused()
	{
	}
	
	
	@Override
	public void onFocusLost()
	{
	}
}
