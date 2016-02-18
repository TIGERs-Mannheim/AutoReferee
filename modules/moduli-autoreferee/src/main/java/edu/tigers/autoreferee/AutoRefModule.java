/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 12, 2015
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.log4j.Logger;

import edu.tigers.autoreferee.engine.calc.BallLeftFieldCalc;
import edu.tigers.autoreferee.engine.calc.BotLastTouchedBallCalc;
import edu.tigers.autoreferee.engine.calc.GameStateHistoryCalc;
import edu.tigers.autoreferee.engine.calc.IRefereeCalc;
import edu.tigers.autoreferee.engine.rules.AutoRefEngine;
import edu.tigers.autoreferee.engine.rules.AutoRefEngine.AutoRefMode;
import edu.tigers.autoreferee.remote.ThreadedTCPRefboxRemote;
import edu.tigers.moduli.AModule;
import edu.tigers.moduli.exceptions.InitModuleException;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.exceptions.StartModuleException;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * @author "Lukas Magel"
 */
public class AutoRefModule extends AModule implements IWorldFrameObserver
{
	/**
	 * @author Lukas Magel
	 */
	public enum AutoRefState
	{
		/**  */
		RUNNING,
		/**  */
		STARTING,
		/**  */
		STOPPED
	}
	
	private final static Logger			log			= Logger.getLogger(AutoRefModule.class);
	/**  */
	public static String						MODULE_ID	= "autoreferee";
	
	private List<IRefereeCalc>				calculators	= new ArrayList<>();
	private List<IAutoRefStateObserver>	refObserver	= new ArrayList<>();
	
	private ThreadedTCPRefboxRemote		remote;
	private AutoRefEngine					ruleEngine;
	
	private AutoRefState						state			= AutoRefState.STOPPED;
	private IAutoRefFrame					lastFrame;
	
	
	/**
	 * @param config
	 */
	public AutoRefModule(final SubnodeConfiguration config)
	{
		calculators.add(new BallLeftFieldCalc());
		calculators.add(new BotLastTouchedBallCalc());
		calculators.add(new GameStateHistoryCalc());
	}
	
	
	@Override
	public void initModule() throws InitModuleException
	{
		
	}
	
	
	@Override
	public void deinitModule()
	{
		
	}
	
	
	@Override
	public void startModule() throws StartModuleException
	{
		// Load all classes to execute the static blocks for config registration
		new AutoRefEngine(null, AutoRefMode.PASSIVE);
		AutoRefConfig.getBallPlacementAccuracy();
	}
	
	
	@Override
	public void stopModule()
	{
		if (remote != null)
		{
			remote.close();
		}
	}
	
	
	/**
	 * @param observer
	 */
	public void addObserver(final IAutoRefStateObserver observer)
	{
		refObserver.add(observer);
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final IAutoRefStateObserver observer)
	{
		refObserver.remove(observer);
	}
	
	
	/**
	 * @param mode
	 * @throws StartModuleException
	 */
	public void start(final AutoRefMode mode) throws StartModuleException
	{
		if (state == AutoRefState.STOPPED)
		{
			doStart(mode);
		}
	}
	
	
	private void doStart(final AutoRefMode mode) throws StartModuleException
	{
		try
		{
			setState(AutoRefState.STARTING);
			
			if (mode == AutoRefMode.ACTIVE)
			{
				remote = new ThreadedTCPRefboxRemote();
				remote.start(AutoRefConfig.getRefboxHostname(), AutoRefConfig.getRefboxPort());
				
				ruleEngine = new AutoRefEngine(remote, mode);
			} else
			{
				ruleEngine = new AutoRefEngine(null, mode);
			}
			
			
			AWorldPredictor predictor = (AWorldPredictor) SumatraModel
					.getInstance().getModule(AWorldPredictor.MODULE_ID);
			predictor.addWorldFrameConsumer(AutoRefModule.this);
			
			lastFrame = null;
			setState(AutoRefState.RUNNING);
		} catch (ModuleNotFoundException | IOException e)
		{
			setState(AutoRefState.STOPPED);
			throw new StartModuleException(e.getMessage(), e);
		}
	}
	
	
	/**
	 * 
	 */
	public void stop()
	{
		if (state == AutoRefState.RUNNING)
		{
			doStop();
		}
	}
	
	
	private void doStop()
	{
		setState(AutoRefState.STOPPED);
		
		try
		{
			AWorldPredictor predictor = (AWorldPredictor) SumatraModel
					.getInstance().getModule(AWorldPredictor.MODULE_ID);
			predictor.removeWorldFrameConsumer(this);
		} catch (ModuleNotFoundException err)
		{
			log.warn("Could not find a module", err);
		}
		
		if (remote != null)
		{
			remote.close();
		}
	}
	
	
	private void setState(final AutoRefState state)
	{
		this.state = state;
		refObserver.forEach(obs -> obs.onAutoRefStateChanged(state));
	}
	
	
	@Override
	public void onNewWorldFrame(final WorldFrameWrapper wFrameWrapper)
	{
		if (lastFrame == null)
		{
			/*
			 * Sit this one out since we need a first frame for initialization
			 */
			lastFrame = createNewRefFrame(wFrameWrapper);
			return;
		}
		AutoRefFrame currentFrame = createNewRefFrame(wFrameWrapper);
		runCalculators(currentFrame);
		
		ruleEngine.update(currentFrame);
		
		for (IAutoRefStateObserver o : refObserver)
		{
			o.onNewAutoRefFrame(currentFrame);
		}
		
		lastFrame = currentFrame;
	}
	
	
	private AutoRefFrame createNewRefFrame(final WorldFrameWrapper wFrameWrapper)
	{
		if (lastFrame != null)
		{
			lastFrame.cleanUp();
		}
		return new AutoRefFrame(lastFrame, wFrameWrapper);
	}
	
	
	private void runCalculators(final AutoRefFrame frame)
	{
		for (IRefereeCalc calc : calculators)
		{
			calc.process(frame);
		}
	}
	
}
