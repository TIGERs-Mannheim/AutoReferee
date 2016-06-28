/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 28, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoref.presenter.humanref;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.Timer;

import edu.tigers.autoref.view.humanref.PassiveHumanRefPanel;
import edu.tigers.autoreferee.engine.events.IGameEvent;
import edu.tigers.autoreferee.engine.log.GameLogEntry;


/**
 * @author "Lukas Magel"
 */
public class PassiveHumanRefViewDriver extends BaseHumanRefViewDriver implements ActionListener
{
	private final static int				eventLogSize		= 5;
	private final static int				timerPeriodMS		= 1_000;
	private final static int				maxAge				= 10_000;
	private final static float				maxTransparency	= 0.8f;
	private final PassiveHumanRefPanel	panel;
	private final Timer						agingTimer;
	
	private LinkedList<EventAgePair>		events				= new LinkedList<>();
	
	
	/**
	 * @param panel
	 */
	public PassiveHumanRefViewDriver(final PassiveHumanRefPanel panel)
	{
		super(panel);
		this.panel = panel;
		agingTimer = new Timer(timerPeriodMS, this);
	}
	
	
	@Override
	public void setNewGameLogEntry(final GameLogEntry entry)
	{
		switch (entry.getType())
		{
			case GAME_EVENT:
				IGameEvent event = entry.getGameEvent();
				addToList(event);
				updateEvents();
				updateAge();
				break;
			default:
				break;
		
		}
	}
	
	
	private void addToList(final IGameEvent event)
	{
		if (events.size() >= eventLogSize)
		{
			events.pollLast();
		}
		events.offerFirst(new EventAgePair(event, 0));
	}
	
	
	private void updateEvents()
	{
		panel.setEvents(events.stream().map(pair -> pair.event).collect(Collectors.toList()));
	}
	
	
	private void updateAge()
	{
		events.forEach(pair -> pair.age = Math.min(pair.age + timerPeriodMS, maxAge));
		
		List<Float> transparency = events.stream()
				.map(pair -> (float) pair.age / maxAge)
				.map(agePercentage -> agePercentage * maxTransparency).collect(Collectors.toList());
		System.out.println(transparency);
		panel.setTransparency(transparency);
	}
	
	
	@Override
	public void start()
	{
		agingTimer.start();
	}
	
	
	@Override
	public void stop()
	{
		agingTimer.stop();
	}
	
	
	@Override
	public void actionPerformed(final ActionEvent e)
	{
		updateAge();
	}
	
	private static class EventAgePair
	{
		public final IGameEvent	event;
		public int					age;
		
		
		/**
		 * @param event
		 * @param age
		 */
		public EventAgePair(final IGameEvent event, final int age)
		{
			this.event = event;
			this.age = age;
		}
	}
	
}
