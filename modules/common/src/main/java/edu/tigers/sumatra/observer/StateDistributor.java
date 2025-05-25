/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.observer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * State distributor that notifies all registered observers when the state changes.
 *
 * @param <T> the type of the state
 */
@AllArgsConstructor
@NoArgsConstructor
public class StateDistributor<T> implements StateSubscriber<T>
{
	@Getter
	private final List<StateChangeObserver<T>> consumers = new CopyOnWriteArrayList<>();
	private T state;


	@Override
	public void subscribe(StateChangeObserver<T> consumer)
	{
		consumers.add(consumer);
	}


	@Override
	public void unsubscribe(StateChangeObserver<T> consumer)
	{
		consumers.remove(consumer);
	}


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
			consumers.forEach(consumer -> consumer.onStateChange(oldState, state));
		}
	}


	public T get()
	{
		return state;
	}
}
