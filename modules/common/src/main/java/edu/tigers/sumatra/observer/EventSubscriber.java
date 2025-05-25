/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.observer;

import java.util.function.Consumer;


/**
 * Allow subscribing to continuously incoming events.
 *
 * @param <T> the type of the event
 */
public interface EventSubscriber<T>
{
	/**
	 * Subscribe to new events.
	 *
	 * @param consumer the consumer to be notified
	 */
	void subscribe(Consumer<T> consumer);

	/**
	 * Unsubscribe from new events.
	 *
	 * @param consumer the consumer to be removed
	 */
	void unsubscribe(Consumer<T> consumer);
}
