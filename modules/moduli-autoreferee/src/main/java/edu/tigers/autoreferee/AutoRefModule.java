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

import edu.tigers.autoreferee.engine.ActiveAutoRefEngine;
import edu.tigers.autoreferee.engine.IAutoRefEngine;
import edu.tigers.autoreferee.engine.IAutoRefEngine.AutoRefMode;
import edu.tigers.autoreferee.engine.PassiveAutoRefEngine;
import edu.tigers.autoreferee.engine.calc.BallLeftFieldCalc;
import edu.tigers.autoreferee.engine.calc.BotLastTouchedBallCalc;
import edu.tigers.autoreferee.engine.calc.GameStateHistoryCalc;
import edu.tigers.autoreferee.engine.calc.IRefereeCalc;
import edu.tigers.autoreferee.engine.calc.PossibleGoalCalc;
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
		/** Right before running, but only when the engine is first started */
		STARTED,
		/**  */
		PAUSED,
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
	
	private IAutoRefEngine					autoRefEngine;
	
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
		calculators.add(new PossibleGoalCalc());
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
		new ActiveAutoRefEngine(null);
		AutoRefConfig.getBallPlacementAccuracy();
		refObserver.clear();
	}
	
	
	@Override
	public void stopModule()
	{
		if (autoRefEngine != null)
		{
			autoRefEngine.stop();
		}
		setState(AutoRefState.STOPPED);
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
	 * @return
	 */
	public IAutoRefEngine getEngine()
	{
		return autoRefEngine;
	}
	
	
	/**
	 * @return
	 */
	public AutoRefState getState()
	{
		return state;
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
		ThreadedTCPRefboxRemote remote = null;
		try
		{
			setState(AutoRefState.STARTING);
			
			if (mode == AutoRefMode.ACTIVE)
			{
				remote = new ThreadedTCPRefboxRemote();
				remote.start(AutoRefConfig.getRefboxHostname(), AutoRefConfig.getRefboxPort());
				
				autoRefEngine = new ActiveAutoRefEngine(remote);
			} else
			{
				autoRefEngine = new PassiveAutoRefEngine();
			}
			lastFrame = null;
			
			AWorldPredictor predictor = (AWorldPredictor) SumatraModel
					.getInstance().getModule(AWorldPredictor.MODULE_ID);
			predictor.addWorldFrameConsumer(AutoRefModule.this);
			
			setState(AutoRefState.STARTED);
			setState(AutoRefState.RUNNING);
		} catch (ModuleNotFoundException | IOException e)
		{
			if (remote != null)
			{
				remote.close();
			}
			if (autoRefEngine != null)
			{
				autoRefEngine.stop();
			}
			setState(AutoRefState.STOPPED);
			throw new StartModuleException(e.getMessage(), e);
		}
	}
	
	
	/**
	 * 
	 */
	public void stop()
	{
		if ((state == AutoRefState.RUNNING) || (state == AutoRefState.PAUSED))
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
		
		if (autoRefEngine != null)
		{
			autoRefEngine.stop();
		}
	}
	
	
	/**
	 * 
	 */
	public void pause()
	{
		if (state != AutoRefState.RUNNING)
		{
			return;
		}
		
		try
		{
			AWorldPredictor predictor = (AWorldPredictor) SumatraModel
					.getInstance().getModule(AWorldPredictor.MODULE_ID);
			predictor.removeWorldFrameConsumer(this);
		} catch (ModuleNotFoundException err)
		{
			log.warn("Could not find a module", err);
		}
		autoRefEngine.pause();
		
		setState(AutoRefState.PAUSED);
	}
	
	
	/**
	 * 
	 */
	public void resume()
	{
		if (state != AutoRefState.PAUSED)
		{
			return;
		}
		
		autoRefEngine.resume();
		try
		{
			AWorldPredictor predictor = (AWorldPredictor) SumatraModel
					.getInstance().getModule(AWorldPredictor.MODULE_ID);
			predictor.addWorldFrameConsumer(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find the predictor module -> Stopping the referee", err);
			doStop();
			return;
		}
		
		setState(AutoRefState.RUNNING);
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
		try
		{
			runCalculators(currentFrame);
		} catch (Exception t)
		{
			log.error("Error while running autoref calculators", t);
			return;
		}
		
		try
		{
			autoRefEngine.process(currentFrame);
		} catch (Exception t)
		{
			log.error("Error while running autoref engine", t);
		}
		
		for (IAutoRefStateObserver o : refObserver)
		{
			try
			{
				o.onNewAutoRefFrame(currentFrame);
			} catch (Exception t)
			{
				log.error("Error in autoref state observer (" + (o != null ? o.toString() : "null") + ")", t);
			}
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
