/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 4, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoref.view.panel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;

import org.apache.log4j.Logger;

import edu.tigers.autoref.view.panel.ViolationsPanel.IViolationsPanelObserver;
import edu.tigers.autoreferee.engine.violations.IViolationDetector.EViolationDetectorType;


/**
 * @author "Lukas Magel"
 */
public class ViolationsPanel extends BasePanel<IViolationsPanelObserver>
{
	/**  */
	private static final long								serialVersionUID	= -2337828703164277142L;
	private static final Logger							log					= Logger.getLogger(ViolationsPanel.class);
	
	private Map<EViolationDetectorType, JCheckBox>	boxes					= new HashMap<>();
	
	/**
	 * @author "Lukas Magel"
	 */
	public interface IViolationsPanelObserver
	{
		
		/**
		 * @param type
		 * @param value
		 */
		public void onButtonTicked(EViolationDetectorType type, boolean value);
	}
	
	private class CheckBoxActionListener implements ActionListener
	{
		
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			try
			{
				EViolationDetectorType detType = EViolationDetectorType.valueOf(e.getActionCommand());
				boolean value = ((JCheckBox) e.getSource()).isSelected();
				onSelectionChange(detType, value);
			} catch (IllegalArgumentException ex)
			{
				log.warn("Unable to parse \"" + e.getActionCommand() + "\" to enum value");
			}
		}
	}
	
	
	/**
	 * 
	 */
	public ViolationsPanel()
	{
		setUp();
	}
	
	
	private void setUp()
	{
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		for (EViolationDetectorType type : EViolationDetectorType.values())
		{
			JCheckBox checkBox = new JCheckBox(type.name());
			checkBox.setSelected(true);
			checkBox.addActionListener(new CheckBoxActionListener());
			boxes.put(type, checkBox);
			add(checkBox);
		}
		
		setBorder(BorderFactory.createTitledBorder("Violations"));
	}
	
	
	private void onSelectionChange(final EViolationDetectorType type, final boolean value)
	{
		observer.forEach(obs -> obs.onButtonTicked(type, value));
	}
	
	
	/**
	 * @return
	 */
	public Set<EViolationDetectorType> getValues()
	{
		Set<EViolationDetectorType> values = new HashSet<>();
		for (EViolationDetectorType type : boxes.keySet())
		{
			JCheckBox box = boxes.get(type);
			if (box.isSelected())
			{
				values.add(type);
			}
		}
		return values;
	}
	
	
	@Override
	public void setPanelEnabled(final boolean enabled)
	{
		boxes.values().forEach(box -> box.setEnabled(enabled));
	}
}
