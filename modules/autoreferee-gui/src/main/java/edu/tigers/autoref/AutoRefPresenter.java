/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 12, 2016
 * Author(s): Lukas Magel
 * *********************************************************
 */
package edu.tigers.autoref;

import java.awt.Component;
import java.awt.EventQueue;

import org.apache.log4j.Logger;

import edu.tigers.autoref.view.startstop.StartStopPanel;
import edu.tigers.autoref.view.startstop.StartStopPanel.IStartStopPanelObserver;
import edu.tigers.autoreferee.AutoRefModule;
import edu.tigers.autoreferee.AutoRefModule.AutoRefState;
import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.IAutoRefStateObserver;
import edu.tigers.autoreferee.engine.rules.AutoRefEngine.AutoRefMode;
import edu.tigers.moduli.IModuliStateObserver;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.exceptions.StartModuleException;
import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.sumatra.model.ModuliStateAdapter;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.views.ISumatraView;
import edu.tigers.sumatra.views.ISumatraViewPresenter;


/**
 * @author Lukas Magel
 */
public class AutoRefPresenter implements ISumatraViewPresenter, IAutoRefStateObserver, IStartStopPanelObserver,
		IModuliStateObserver
{
	private static Logger	log				= Logger.getLogger(AutoRefPresenter.class);
														
	private StartStopPanel	startStopPanel	= null;
														
														
	/**
	 * 
	 */
	public AutoRefPresenter()
	{
		startStopPanel = new StartStopPanel();
		startStopPanel.addObserver(this);
		
		ModuliStateAdapter.getInstance().addObserver(this);
	}
	
	
	@Override
	public Component getComponent()
	{
		return startStopPanel;
	}
	
	
	@Override
	public ISumatraView getSumatraView()
	{
		return startStopPanel;
	}
	
	
	@Override
	public void onModuliStateChanged(final ModulesState state)
	{
		if (state == ModulesState.ACTIVE)
		{
			try
			{
				AutoRefModule autoref = (AutoRefModule) SumatraModel
						.getInstance().getModule(AutoRefModule.MODULE_ID);
				autoref.addObserver(this);
			} catch (ModuleNotFoundException e)
			{
				log.error("AutoRef not found", e);
			}
		}
	}
	
	
	@Override
	public void onAutoRefStateChanged(final AutoRefState state)
	{
		boolean startEnabled = false;
		boolean stopEnabled = false;
		switch (state)
		{
			case RUNNING:
				stopEnabled = true;
				break;
			case STOPPED:
				startEnabled = true;
				break;
			case STARTING:
				break;
		}
		
		final boolean startVal = startEnabled;
		final boolean stopVal = stopEnabled;
		EventQueue.invokeLater(() -> {
			startStopPanel.getStartButton().setEnabled(startVal);
			startStopPanel.getStopButton().setEnabled(stopVal);
			startStopPanel.setModeBoxEnabled(startVal);
		});
	}
	
	
	@Override
	public void onStartButtonPressed()
	{
		new Thread(new AutoRefStarter(startStopPanel.getModeSetting())).start();
	}
	
	
	@Override
	public void onStopButtonPressed()
	{
		try
		{
			AutoRefModule autoref = (AutoRefModule) SumatraModel
					.getInstance().getModule(AutoRefModule.MODULE_ID);
			autoref.stop();
		} catch (ModuleNotFoundException e)
		{
			log.error(e);
		}
	}
	
	private class AutoRefStarter implements Runnable
	{
		private final AutoRefMode	mode;
		
		
		/**
		 * @param mode
		 */
		public AutoRefStarter(final AutoRefMode mode)
		{
			this.mode = mode;
		}
		
		
		@Override
		public void run()
		{
			try
			{
				AutoRefModule autoref = (AutoRefModule) SumatraModel
						.getInstance().getModule(AutoRefModule.MODULE_ID);
				autoref.start(mode);
			} catch (ModuleNotFoundException | StartModuleException e)
			{
				log.error("Error during Autoref startup: " + e.getMessage(), e);
			}
		}
		
	}
	
	
	@Override
	public void onNewAutoRefFrame(final IAutoRefFrame frame)
	{
	}
	
}
