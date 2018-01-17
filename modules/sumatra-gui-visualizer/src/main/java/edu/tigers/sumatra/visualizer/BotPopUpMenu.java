/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.visualizer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.DefaultButtonModel;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.EAiType;


/**
 * Right click menu on number on bot.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BotPopUpMenu extends JPopupMenu
{
	/**  */
	private static final long serialVersionUID = 5547621546041047224L;
	
	private final BotID botId;
	
	private final List<IBotPopUpMenuObserver> observers = new CopyOnWriteArrayList<>();
	
	
	/**
	 * @param botId
	 * @param status
	 */
	public BotPopUpMenu(final BotID botId, final BotStatus status)
	{
		this.botId = botId;
		final JCheckBoxMenuItem hideFromRcm = new JCheckBoxMenuItem("Hide from RCM");
		add(hideFromRcm);
		
		hideFromRcm.setSelected(status.isHideRcm());
		hideFromRcm.addActionListener(new HideFromRcmActionListener());
		
		ButtonGroup aiButtonGroup = new ButtonGroup();
		for (EAiType aiAssignment : EAiType.values())
		{
			JRadioButtonMenuItem radioButton = new JRadioButtonMenuItem(aiAssignment.name());
			radioButton.addActionListener(new AiAssignmentChangeListener());
			radioButton.setActionCommand(aiAssignment.name());
			add(radioButton);
			aiButtonGroup.add(radioButton);
			
			if (aiAssignment == status.getAiAssignment())
			{
				radioButton.setSelected(true);
			}
		}
		
		final ButtonModel aiButtonModel = new DefaultButtonModel();
		aiButtonModel.setGroup(aiButtonGroup);
	}
	
	
	/**
	 * @param observer
	 */
	@SuppressWarnings("squid:S2250") // Collection methods with O(n) performance
	public void addObserver(final IBotPopUpMenuObserver observer)
	{
		observers.add(observer);
	}
	
	
	/**
	 * @param observer
	 */
	@SuppressWarnings("squid:S2250") // Collection methods with O(n) performance
	public void removeObserver(final IBotPopUpMenuObserver observer)
	{
		observers.remove(observer);
	}
	
	/**
	 * Default
	 */
	public interface IBotPopUpMenuObserver
	{
		/**
		 * @param botId
		 * @param hide
		 */
		default void onHideBotFromRcmClicked(final BotID botId, final boolean hide)
		{
		}
		
		
		/**
		 * @param botID
		 * @param aiAssignment
		 */
		default void onBotAiAssignmentChanged(final BotID botID, final EAiType aiAssignment)
		{
		}
	}
	
	private class HideFromRcmActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			boolean checked = ((JCheckBoxMenuItem) e.getSource()).isSelected();
			for (final IBotPopUpMenuObserver observer : observers)
			{
				observer.onHideBotFromRcmClicked(botId, checked);
			}
		}
	}
	
	private class AiAssignmentChangeListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			JRadioButtonMenuItem button = (JRadioButtonMenuItem) e.getSource();
			EAiType aiAssignment = EAiType.valueOf(button.getActionCommand());
			for (final IBotPopUpMenuObserver observer : observers)
			{
				observer.onBotAiAssignmentChanged(botId, aiAssignment);
			}
		}
	}
}
