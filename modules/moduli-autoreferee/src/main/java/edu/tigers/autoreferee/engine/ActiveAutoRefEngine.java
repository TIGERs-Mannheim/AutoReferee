/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 3, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.states.IAutoRefState;
import edu.tigers.autoreferee.engine.states.IAutoRefStateContext;
import edu.tigers.autoreferee.engine.states.impl.DummyAutoRefState;
import edu.tigers.autoreferee.engine.states.impl.PlaceBallState;
import edu.tigers.autoreferee.engine.states.impl.PrepareKickoffState;
import edu.tigers.autoreferee.engine.states.impl.PreparePenaltyState;
import edu.tigers.autoreferee.engine.states.impl.RunningState;
import edu.tigers.autoreferee.engine.states.impl.StopState;
import edu.tigers.autoreferee.engine.violations.IRuleViolation;
import edu.tigers.autoreferee.remote.IRefboxRemote;
import edu.tigers.sumatra.Referee.SSL_Referee.Stage;
import edu.tigers.sumatra.wp.data.EGameStateNeutral;


/**
 * @author "Lukas Magel"
 */
public class ActiveAutoRefEngine extends AbstractAutoRefEngine
{
	private static final Logger							log									= Logger
																													.getLogger(ActiveAutoRefEngine.class);
	
	/** in ms */
	private static long										DUPLICATE_RESEND_WAIT_TIME_MS	= 500;
	
	private List<IAutoRefEngineObserver>				engineObserver						= new ArrayList<>();
	private IAutoRefState									dummyState							= null;
	private Map<EGameStateNeutral, IAutoRefState>	refStates							= new HashMap<>();
	
	private final IRefboxRemote							remote;
	private FollowUpAction									followUp								= null;
	private boolean											doProceed							= false;
	
	private RefCommand										lastRefCommand						= null;
	private long												lastCommandTimestamp				= 0;
	
	
	private class RefStateContext implements IAutoRefStateContext
	{
		private long	ts;
		
		
		private RefStateContext(final long ts)
		{
			this.ts = ts;
		}
		
		
		@Override
		public void sendCommand(final RefCommand cmd)
		{
			ActiveAutoRefEngine.this.sendCommand(cmd, ts);
		}
		
		
		@Override
		public FollowUpAction getFollowUpAction()
		{
			return followUp;
		}
		
		
		@Override
		public void setFollowUpAction(final FollowUpAction action)
		{
			setFollowUp(action);
		}
		
		
		@Override
		public boolean doProceed()
		{
			return doProceed;
		}
		
	}
	
	/**
	 * @author Lukas Magel
	 */
	public interface IAutoRefEngineObserver
	{
		
		/**
		 * @param proceedPossible
		 */
		public void onStateChanged(final boolean proceedPossible);
		
		
		/**
		 * @param action
		 */
		public void onFollowUpChanged(final FollowUpAction action);
		
	}
	
	
	/**
	 * @param remote
	 */
	public ActiveAutoRefEngine(final IRefboxRemote remote)
	{
		this.remote = remote;
		setupStates();
	}
	
	
	private void setupStates()
	{
		RunningState runningState = new RunningState();
		refStates.put(EGameStateNeutral.RUNNING, runningState);
		
		PrepareKickoffState prepKickOffState = new PrepareKickoffState();
		refStates.put(EGameStateNeutral.PREPARE_KICKOFF_BLUE, prepKickOffState);
		refStates.put(EGameStateNeutral.PREPARE_KICKOFF_YELLOW, prepKickOffState);
		
		PreparePenaltyState prepPenaltyState = new PreparePenaltyState();
		refStates.put(EGameStateNeutral.PREPARE_PENALTY_BLUE, prepPenaltyState);
		refStates.put(EGameStateNeutral.PREPARE_PENALTY_YELLOW, prepPenaltyState);
		
		PlaceBallState placeBallState = new PlaceBallState();
		refStates.put(EGameStateNeutral.BALL_PLACEMENT_BLUE, placeBallState);
		refStates.put(EGameStateNeutral.BALL_PLACEMENT_YELLOW, placeBallState);
		
		StopState stopState = new StopState();
		refStates.put(EGameStateNeutral.STOPPED, stopState);
		
		dummyState = new DummyAutoRefState();
	}
	
	
	@Override
	public synchronized void stop()
	{
		remote.close();
	}
	
	
	@Override
	public synchronized void reset()
	{
		super.reset();
		setFollowUp(null);
		doProceed = false;
		resetRefStates();
	}
	
	
	@Override
	public synchronized void resume()
	{
		super.resume();
		doProceed = false;
		resetRefStates();
	}
	
	
	/**
	 * 
	 */
	public synchronized void proceed()
	{
		doProceed = true;
	}
	
	
	@Override
	public AutoRefMode getMode()
	{
		return AutoRefMode.ACTIVE;
	}
	
	
	private void resetRefStates()
	{
		refStates.values().forEach(state -> state.reset());
	}
	
	
	private IAutoRefState getActiveState(final EGameStateNeutral gameState)
	{
		IAutoRefState state = refStates.get(gameState);
		if (state == null)
		{
			return dummyState;
		}
		return state;
	}
	
	
	@Override
	public synchronized void process(final IAutoRefFrame frame)
	{
		if (engineState == EEngineState.PAUSED)
		{
			return;
		}
		
		super.process(frame);
		
		IAutoRefState state = getActiveState(frame.getGameState());
		RefStateContext ctx = new RefStateContext(frame.getTimestamp());
		
		List<IRuleViolation> violations = getViolations(frame);
		logViolations(violations);
		
		boolean canProceed = state.canProceed();
		if (violations.size() > 0)
		{
			IRuleViolation violation = violations.get(0);
			state.handleViolation(violation, ctx);
		}
		
		state.update(frame, ctx);
		
		if (state.canProceed() != canProceed)
		{
			notifyStateChange(state.canProceed());
		}
		
		doProceed = false;
	}
	
	
	@Override
	protected void onGameStateChange(final EGameStateNeutral oldGameState, final EGameStateNeutral newGameState)
	{
		super.onGameStateChange(oldGameState, newGameState);
		
		IAutoRefState oldRefState = getActiveState(oldGameState);
		IAutoRefState newRefState = getActiveState(newGameState);
		if ((oldRefState != newRefState) && (newRefState != null))
		{
			newRefState.reset();
		}
		
		notifyStateChange(false);
		
		if (newGameState == EGameStateNeutral.RUNNING)
		{
			setFollowUp(null);
		}
	}
	
	
	@Override
	protected void onStageChange(final Stage oldStage, final Stage newStage)
	{
		super.onStageChange(oldStage, newStage);
		
		if ((oldStage == Stage.NORMAL_FIRST_HALF) || (oldStage == Stage.NORMAL_SECOND_HALF)
				|| (oldStage == Stage.EXTRA_FIRST_HALF) || (oldStage == Stage.EXTRA_SECOND_HALF))
		{
			setFollowUp(null);
		}
	}
	
	
	private void sendCommand(final RefCommand cmd, final long ts)
	{
		if (cmd.equals(lastRefCommand)
				&& ((ts - lastCommandTimestamp) < TimeUnit.MILLISECONDS.toNanos(DUPLICATE_RESEND_WAIT_TIME_MS)))
		{
			log.debug("Dropping duplicate ref message: " + cmd.getCommand());
			return;
		}
		lastRefCommand = cmd;
		lastCommandTimestamp = ts;
		doSendCommand(cmd);
	}
	
	
	private void doSendCommand(final RefCommand cmd)
	{
		gameLog.addEntry(cmd);
		remote.sendCommand(cmd);
	}
	
	
	/**
	 * @param observer
	 */
	public synchronized void addObserver(final IAutoRefEngineObserver observer)
	{
		engineObserver.add(observer);
	}
	
	
	/**
	 * @param observer
	 */
	public synchronized void removeObserver(final IAutoRefEngineObserver observer)
	{
		engineObserver.remove(observer);
	}
	
	
	private void setFollowUp(final FollowUpAction action)
	{
		followUp = action;
		engineObserver.forEach(observer -> observer.onFollowUpChanged(followUp));
		gameLog.addEntry(followUp);
	}
	
	
	private void notifyStateChange(final boolean canProceed)
	{
		engineObserver.forEach(obs -> obs.onStateChanged(canProceed));
	}
}
