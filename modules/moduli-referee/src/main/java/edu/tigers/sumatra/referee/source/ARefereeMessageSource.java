/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.referee.source;

import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage;

import java.net.InetAddress;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;


public abstract class ARefereeMessageSource
{
	private final List<IRefereeSourceObserver> observers = new CopyOnWriteArrayList<>();

	private final ERefereeMessageSource type;


	protected ARefereeMessageSource(final ERefereeMessageSource type)
	{
		this.type = type;
	}


	/**
	 * @param observer
	 */
	public void addObserver(final IRefereeSourceObserver observer)
	{
		observers.add(observer);
	}


	/**
	 * @param observer
	 */
	public void removeObserver(final IRefereeSourceObserver observer)
	{
		observers.remove(observer);
	}


	protected void notifyNewRefereeMessage(final SslGcRefereeMessage.Referee msg)
	{
		for (IRefereeSourceObserver observer : observers)
		{
			observer.onNewRefereeMessage(msg);
		}
	}


	/**
	 * Start source
	 */
	public void start()
	{
	}


	/**
	 * Stop source
	 */
	public void stop()
	{
	}


	/**
	 * @return the type
	 */
	public ERefereeMessageSource getType()
	{
		return type;
	}


	public Optional<InetAddress> getRefBoxAddress()
	{
		return Optional.empty();
	}
}
