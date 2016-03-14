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
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;

import edu.tigers.autoref.view.panel.ActiveEnginePanel;
import edu.tigers.autoref.view.panel.ActiveEnginePanel.IActiveEnginePanelObserver;
import edu.tigers.autoref.view.panel.AutoRefPanel;
import edu.tigers.autoref.view.panel.StartStopPanel;
import edu.tigers.autoref.view.panel.StartStopPanel.IStartStopPanelObserver;
import edu.tigers.autoref.view.panel.ViolationsPanel;
import edu.tigers.autoref.view.panel.ViolationsPanel.IViolationsPanelObserver;
import edu.tigers.autoreferee.AutoRefModule;
import edu.tigers.autoreferee.AutoRefModule.AutoRefState;
import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.IAutoRefStateObserver;
import edu.tigers.autoreferee.engine.ActiveAutoRefEngine;
import edu.tigers.autoreferee.engine.ActiveAutoRefEngine.IAutoRefEngineObserver;
import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.IAutoRefEngine;
import edu.tigers.autoreferee.engine.IAutoRefEngine.AutoRefMode;
import edu.tigers.autoreferee.engine.violations.IViolationDetector.EViolationDetectorType;
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
public class AutoRefPresenter implements ISumatraViewPresenter, IModuliStateObserver
{
	private static Logger		log					= Logger.getLogger(AutoRefPresenter.class);
	
	private AutoRefPanel			mainPanel			= new AutoRefPanel();
	private StartStopPanel		startStopPanel		= new StartStopPanel();
	private ViolationsPanel		violationsPanel	= new ViolationsPanel();
	private ActiveEnginePanel	activeEnginePanel	= new ActiveEnginePanel();
	
	private class AutoRefStateObserver implements IAutoRefStateObserver
	{
		
		@Override
		public void onAutoRefStateChanged(final AutoRefState state)
		{
			EventQueue.invokeLater(() -> startStopPanel.setState(state));
			
			switch (state)
			{
				case STOPPED:
					EventQueue.invokeLater(() -> activeEnginePanel.setPanelEnabled(false));
					break;
				case STARTED:
					Optional<AutoRefModule> optModule = getAutoRefModule();
					if (optModule.isPresent())
					{
						AutoRefModule module = optModule.get();
						IAutoRefEngine engine = module.getEngine();
						if (engine.getMode() == AutoRefMode.ACTIVE)
						{
							EventQueue.invokeLater(() -> activeEnginePanel.setPanelEnabled(true));
							ActiveAutoRefEngine activeEngine = (ActiveAutoRefEngine) engine;
							activeEngine.addObserver(new AutoRefEngineObserver());
						}
					}
					break;
				default:
					break;
			}
		}
		
		
		@Override
		public void onNewAutoRefFrame(final IAutoRefFrame frame)
		{
		}
	}
	
	private class ViolationsPanelObserver implements IViolationsPanelObserver
	{
		@Override
		public void onButtonTicked(final EViolationDetectorType type, final boolean value)
		{
			Set<EViolationDetectorType> types = violationsPanel.getValues();
			Optional<AutoRefModule> autoref = getAutoRefModule();
			if (autoref.isPresent() && (autoref.get().getState() == AutoRefState.RUNNING))
			{
				autoref.get().getEngine().setActiveViolations(types);
			}
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
				Optional<AutoRefModule> optAutoref = getAutoRefModule();
				if (optAutoref.isPresent())
				{
					optAutoref.get().start(mode);
				}
			} catch (StartModuleException e)
			{
				log.error("Error during Autoref startup: " + e.getMessage(), e);
			}
		}
		
	}
	
	private class StartStopPanelObserver implements IStartStopPanelObserver
	{
		@Override
		public void onStartButtonPressed()
		{
			new Thread(new AutoRefStarter(startStopPanel.getModeSetting())).start();
		}
		
		
		@Override
		public void onStopButtonPressed()
		{
			Optional<AutoRefModule> optRef = getAutoRefModule();
			optRef.ifPresent(autoref -> autoref.stop());
		}
		
		
		@Override
		public void onPauseButtonPressed()
		{
			Optional<AutoRefModule> optModule = getAutoRefModule();
			optModule.ifPresent(module -> module.pause());
			
		}
		
		
		@Override
		public void onResumeButtonPressed()
		{
			Optional<AutoRefModule> optModule = getAutoRefModule();
			optModule.ifPresent(module -> module.resume());
		}
	}
	
	private class AutoRefEngineObserver implements IAutoRefEngineObserver
	{
		
		@Override
		public void onStateChanged(final boolean proceedPossible)
		{
			EventQueue.invokeLater(() -> {
				activeEnginePanel.setProceedButtonEnabled(proceedPossible);
			});
		}
		
		
		@Override
		public void onFollowUpChanged(final FollowUpAction action)
		{
			EventQueue.invokeLater(() -> {
				activeEnginePanel.setNextAction(action);
			});
		}
	}
	
	private class ActiveEnginePanelObserver implements IActiveEnginePanelObserver
	{
		
		@Override
		public void onResetButtonPressed()
		{
			Optional<AutoRefModule> optModule = getAutoRefModule();
			optModule.ifPresent(module -> module.getEngine().reset());
		}
		
		
		@Override
		public void onProceedButtonPressed()
		{
			Optional<AutoRefModule> optModule = getAutoRefModule();
			if (optModule.isPresent())
			{
				AutoRefModule module = optModule.get();
				
				IAutoRefEngine engine = module.getEngine();
				if ((engine != null) && (engine.getMode() == AutoRefMode.ACTIVE))
				{
					ActiveAutoRefEngine activeEngine = (ActiveAutoRefEngine) engine;
					activeEngine.proceed();
				}
			}
		}
		
	}
	
	
	/**
	 * 
	 */
	public AutoRefPresenter()
	{
		startStopPanel.addObserver(new StartStopPanelObserver());
		violationsPanel.addObserver(new ViolationsPanelObserver());
		
		activeEnginePanel.setPanelEnabled(false);
		activeEnginePanel.addObserver(new ActiveEnginePanelObserver());
		
		mainPanel.setLayout(new MigLayout("center", "[320][]", "[][]"));
		mainPanel.add(startStopPanel, "grow x, top");
		mainPanel.add(violationsPanel, "span 1 2, wrap");
		mainPanel.add(activeEnginePanel, "grow x, top");
		
		ModuliStateAdapter.getInstance().addObserver(this);
	}
	
	
	@Override
	public Component getComponent()
	{
		return mainPanel;
	}
	
	
	@Override
	public ISumatraView getSumatraView()
	{
		return mainPanel;
	}
	
	
	@Override
	public void onModuliStateChanged(final ModulesState state)
	{
		switch (state)
		{
			case ACTIVE:
				Optional<AutoRefModule> optModule = getAutoRefModule();
				optModule.ifPresent(autoref -> autoref.addObserver(new AutoRefStateObserver()));
				setPanelsEnabledLater(true);
				break;
			case NOT_LOADED:
			case RESOLVED:
				setPanelsEnabledLater(false);
				break;
		}
	}
	
	
	private void setPanelsEnabledLater(final boolean enabled)
	{
		EventQueue.invokeLater(() -> setPanelsEnabled(enabled));
	}
	
	
	private void setPanelsEnabled(final boolean enabled)
	{
		Arrays.asList(startStopPanel, activeEnginePanel, violationsPanel).forEach(
				panel -> panel.setPanelEnabled(enabled));
		if (enabled == true)
		{
			startStopPanel.setState(AutoRefState.STOPPED);
		}
	}
	
	
	private Optional<AutoRefModule> getAutoRefModule()
	{
		try
		{
			AutoRefModule autoref = (AutoRefModule) SumatraModel.getInstance().getModule(AutoRefModule.MODULE_ID);
			return Optional.of(autoref);
		} catch (ModuleNotFoundException e)
		{
			log.error(e);
		}
		return Optional.empty();
	}
}
