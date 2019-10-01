/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.statemachine;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;


/**
 * Generic state machine.
 *
 * @param <T>
 */
public class StateMachine<T extends IState> implements IStateMachine<T>
{
	private static final Logger log = Logger.getLogger(StateMachine.class.getName());
	private static final int MAX_STATE_CHANGES_PER_UPDATE = 15;

	private final Map<IEvent, Map<IState, T>> transitions = new HashMap<>();
	private String name = "StateMachine";
	private T currentState = null;
	private boolean initialStateInitialized = false;
	private boolean extendedLogging = false;
	private int stateChangesSinceUpdate = 0;


	private T getNextState(IEvent event)
	{
		Validate.notNull(currentState, "StateMachine not init");
		Map<IState, T> subTransitions = transitions.computeIfAbsent(event, k -> new HashMap<>());
		return subTransitions.getOrDefault(currentState, subTransitions.get(null));
	}


	private void extendedLogging(Runnable run)
	{
		if (extendedLogging)
		{
			run.run();
		}
	}


	@Override
	public void update()
	{
		if (currentState == null)
		{
			// no initial state or done
			return;
		}
		if (!initialStateInitialized)
		{
			// initialize the very first state
			currentState.doEntryActions();
			initialStateInitialized = true;
		}
		stateChangesSinceUpdate = 0;
		currentState.doUpdate();
	}


	@Override
	public void changeState(final T newState)
	{
		if (newState.equals(currentState))
		{
			return;
		}

		extendedLogging(() -> log.trace(name + ": Switch state from " + currentState + " to " + newState));

		if (stateChangesSinceUpdate > MAX_STATE_CHANGES_PER_UPDATE)
		{
			log.error("Number of allowed state changes exceeded. Possible state loop!");
			return;
		}
		stateChangesSinceUpdate++;

		currentState.doExitActions();

		// the order of the following statements matters:
		// in both methods, one could trigger another state change, resulting in a recursive call of this method.
		// by setting the currentState first, the last call to changeState will have priority
		// by calling the methods on `newState` instead of `currentState`, it is guaranteed, that the new state
		// is called, not a future currentState.
		currentState = newState;
		newState.doEntryActions();
		newState.doUpdate();
	}


	@Override
	public void stop()
	{
		if (currentState != null)
		{
			currentState.doExitActions();
		}
		currentState = null;
	}


	@Override
	public void triggerEvent(final IEvent event)
	{
		Validate.notNull(event);
		extendedLogging(() -> log.trace(name + ": New event: " + event));

		T newState = getNextState(event);
		if (newState != null)
		{
			changeState(newState);
		}
	}


	@Override
	public final T getCurrentState()
	{
		return currentState;
	}


	@Override
	public final void addTransition(final IState currentState, final IEvent event, final T state)
	{
		Validate.notNull(event);
		Validate.notNull(state);
		Map<IState, T> subTransitions = transitions.computeIfAbsent(event, k -> new HashMap<>());
		T preState = subTransitions.put(currentState, state);
		if (preState != null)
		{
			log.warn(name + ": Overwriting transition for event: " + event + " and state " + currentState
					+ ". Change state from "
					+ preState + " to "
					+ state);
		}
	}


	@Override
	public final void setInitialState(final T currentState)
	{
		if (this.currentState != null && initialStateInitialized)
		{
			throw new IllegalStateException("Already initialized");
		}
		this.currentState = currentState;
	}


	@Override
	public void setExtendedLogging(final boolean extendedLogging)
	{
		this.extendedLogging = extendedLogging;
	}


	@Override
	public void setName(final String name)
	{
		this.name = name;
	}
}
