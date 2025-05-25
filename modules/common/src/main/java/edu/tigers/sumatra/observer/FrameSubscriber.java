/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.observer;

import java.util.function.Consumer;


/**
 * Allow subscribing to continuously incoming frames.
 * It is ensured that the observer is called with the latest frame and each new frame.
 * The observer is also notified when the frame is cleared.
 *
 * @param <T> the type of the frame
 */
public interface FrameSubscriber<T> extends EventSubscriber<T>
{
	@Override
	void subscribe(Consumer<T> consumer);

	/**
	 * Subscribe to clear frame.
	 *
	 * @param runnable the runnable to be notified
	 */
	void subscribeClear(Runnable runnable);

	@Override
	void unsubscribe(Consumer<T> consumer);

	/**
	 * Unsubscribe from clear frames.
	 *
	 * @param runnable the runnable to be removed
	 */
	void unsubscribeClear(Runnable runnable);
}
