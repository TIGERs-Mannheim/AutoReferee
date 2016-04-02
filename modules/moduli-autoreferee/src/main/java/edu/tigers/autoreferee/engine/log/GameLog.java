/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 23, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.log;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.apache.log4j.Logger;

import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.RefCommand;
import edu.tigers.autoreferee.engine.violations.IRuleViolation;
import edu.tigers.sumatra.referee.RefereeMsg;
import edu.tigers.sumatra.wp.data.EGameStateNeutral;


/**
 * @author "Lukas Magel"
 */
public class GameLog
{
	private static final Logger		log					= Logger.getLogger(GameLog.class);
	
	private long							startRefTimestamp	= 0;
	private Instant						startTime			= Instant.now();
	private long							currentTimestamp	= 0;
	private List<GameLogEntry>			entries				= new ArrayList<>();
	private List<IGameLogObserver>	observer				= new ArrayList<>();
	
	
	/**
	 * @param timestamp
	 */
	public void initialize(final long timestamp)
	{
		log.debug("Initialized game log with timestamp: " + timestamp);
		startTime = Instant.now();
		startRefTimestamp = timestamp;
		currentTimestamp = timestamp;
	}
	
	
	private long getTimeSinceStart()
	{
		return currentTimestamp - startRefTimestamp;
	}
	
	
	private Instant getCurrentInstant()
	{
		return startTime.plusNanos(getTimeSinceStart());
	}
	
	
	/**
	 * @return a read only view of the log entries
	 */
	public List<GameLogEntry> getEntries()
	{
		return Collections.unmodifiableList(entries);
	}
	
	
	/**
	 * Set the current frame timestamp which will be used as reference timestamp for all entries added after this
	 * invocation
	 * 
	 * @param timestamp the current frame timestamp in nanoseconds
	 */
	public void setCurrentTimestamp(final long timestamp)
	{
		currentTimestamp = timestamp;
	}
	
	
	private void addEntryToLog(final GameLogEntry entry)
	{
		int id;
		synchronized (entries)
		{
			entries.add(entry);
			id = entries.size() - 1;
		}
		log.debug("Added new entry with id " + id + "and type " + entry.getType());
		observer.forEach(obs -> obs.onNewEntry(id, entry));
	}
	
	
	private GameLogEntry buildEntry(final Consumer<? super GameLogEntryBuilder> consumer)
	{
		GameLogEntryBuilder builder = new GameLogEntryBuilder();
		builder.setTimestamp(currentTimestamp);
		builder.setTimeSinceStart(getTimeSinceStart());
		builder.setInstant(getCurrentInstant());
		
		consumer.accept(builder);
		return builder.toEntry();
	}
	
	
	private GameLogEntry buildAndAddEntry(final Consumer<? super GameLogEntryBuilder> consumer)
	{
		GameLogEntry entry = buildEntry(consumer);
		addEntryToLog(entry);
		return entry;
	}
	
	
	/**
	 * @param gamestate
	 * @return
	 */
	public GameLogEntry addEntry(final EGameStateNeutral gamestate)
	{
		return buildAndAddEntry(builder -> builder.setGamestate(gamestate));
	}
	
	
	/**
	 * @param violation
	 * @return
	 */
	public GameLogEntry addEntry(final IRuleViolation violation)
	{
		return buildAndAddEntry(builder -> builder.setViolation(violation));
	}
	
	
	/**
	 * @param refereeMsg
	 * @return
	 */
	public GameLogEntry addEntry(final RefereeMsg refereeMsg)
	{
		return buildAndAddEntry(builder -> builder.setRefereeMsg(refereeMsg));
	}
	
	
	/**
	 * @param action
	 * @return
	 */
	public GameLogEntry addEntry(final FollowUpAction action)
	{
		return buildAndAddEntry(builder -> builder.setFollowUpAction(action));
	}
	
	
	/**
	 * @param command
	 * @return
	 */
	public GameLogEntry addEntry(final RefCommand command)
	{
		return buildAndAddEntry(builder -> builder.setCommand(command));
	}
	
	
	/**
	 * @param observer
	 */
	public void addObserver(final IGameLogObserver observer)
	{
		this.observer.add(observer);
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final IGameLogObserver observer)
	{
		this.observer.remove(observer);
	}
	
	/**
	 * @author "Lukas Magel"
	 */
	public interface IGameLogObserver
	{
		/**
		 * @param id
		 * @param entry
		 */
		public void onNewEntry(int id, GameLogEntry entry);
	}
}
