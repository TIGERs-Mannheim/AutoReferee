/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.observer;

import edu.tigers.sumatra.observer.StateSubscriber.StateChangeObserver;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


/**
 * State distributor that notifies all registered observers when the state changes.
 *
 * @param <T> the type of the state
 */
@AllArgsConstructor
@NoArgsConstructor
public class StateDistributor<T> extends BasicDistributor<StateChangeObserver<T>> implements StateSubscriber<T>
{
	private T state;


	/**
	 * Notify all registered observers with the given state.
	 *
	 * @param state the state
	 */
	public void set(T state)
	{
		var oldState = this.state;
		this.state = state;
		// Make sure that consumers are not called in parallel due to concurrent calls of this set.
		synchronized (this)
		{
			getConsumers().values().forEach(consumer -> consumer.onStateChange(oldState, state));
		}
	}


	public T get()
	{
		return state;
	}


	@Override
	public void clear()
	{
		super.clear();
		state = null;
	}
}
