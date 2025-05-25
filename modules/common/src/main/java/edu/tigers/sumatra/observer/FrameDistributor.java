/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.observer;

import edu.tigers.sumatra.util.Safe;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;


/**
 * Distributes frames to all registered subscribers.
 *
 * @param <T> the type of the frame
 */
public class FrameDistributor<T> implements FrameSubscriber<T>
{
	private final List<Consumer<T>> consumers = new CopyOnWriteArrayList<>();
	private final List<Runnable> clearConsumers = new CopyOnWriteArrayList<>();

	private T lastFrame;


	@Override
	public void subscribe(Consumer<T> consumer)
	{
		consumers.add(consumer);
		var event = lastFrame;
		if (event != null)
		{
			Safe.run(consumer, event);
		}
	}


	@Override
	public void subscribeClear(Runnable runnable)
	{
		clearConsumers.add(runnable);
		var event = lastFrame;
		if (event == null)
		{
			Safe.run(runnable);
		}
	}


	@Override
	public void unsubscribe(Consumer<T> consumer)
	{
		consumers.remove(consumer);
	}


	@Override
	public void unsubscribeClear(Runnable runnable)
	{
		clearConsumers.remove(runnable);
	}


	/**
	 * Notify all subscribers about a new frame.
	 * This method is synchronized to ensure that the consumers are not called in parallel, which they might not expect.
	 *
	 * @param frame
	 */
	public synchronized void newFrame(T frame)
	{
		consumers.forEach(c -> Safe.run(c, frame));
		lastFrame = frame;
	}


	/**
	 * Notify all subscribers to clear the frame.
	 * This method is synchronized to ensure that the consumers are not called in parallel, which they might not expect.
	 */
	public synchronized void clearFrame()
	{
		lastFrame = null;
		clearConsumers.forEach(Runnable::run);
	}
}
