/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.observer;

import lombok.Getter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;


/**
 * Event distributor that notifies all registered observers on new events.
 *
 * @param <T> the type of the event
 */
public class EventDistributor<T> implements EventSubscriber<T>
{
	@Getter
	private final List<Consumer<T>> consumers = new CopyOnWriteArrayList<>();


	@Override
	public void subscribe(Consumer<T> consumer)
	{
		consumers.add(consumer);
	}


	@Override
	public void unsubscribe(Consumer<T> consumer)
	{
		consumers.remove(consumer);
	}


	/**
	 * Notify all registered observers with the given event.
	 *
	 * @param event the event
	 */
	public void newEvent(T event)
	{
		consumers.forEach(consumer -> consumer.accept(event));
	}
}
