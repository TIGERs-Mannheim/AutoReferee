/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoref.view.main;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;

import edu.tigers.autoref.view.main.IActiveEnginePanel.IActiveEnginePanelObserver;
import edu.tigers.sumatra.components.BasePanel;
import net.miginfocom.swing.MigLayout;


/**
 * @author Lukas Magel
 */
public class ActiveEnginePanel extends BasePanel<IActiveEnginePanelObserver> implements IActiveEnginePanel
{
	private static final long serialVersionUID = -8855537755362886421L;
	
	private JButton resetButton;
	
	
	/**
	 * Create new instance
	 */
	public ActiveEnginePanel()
	{
		setLayout(new MigLayout("fill", "[50%][50%]", ""));
		setBorder(BorderFactory.createTitledBorder("Engine"));
		
		final JLabel followUpLabel = new JLabel("");
		final JLabel teamInFavorLabel = new JLabel("");
		final JLabel positionLabel = new JLabel("");
		
		resetButton = new JButton("Reset");
		resetButton.addActionListener(e -> informObserver(IActiveEnginePanelObserver::onResetButtonPressed));
		
		add(followUpLabel, "span, wrap");
		add(teamInFavorLabel, "span, wrap");
		add(positionLabel, "span, wrap");
		add(resetButton, "grow");
	}
	
	
	@Override
	public void setPanelEnabled(final boolean enabled)
	{
		resetButton.setEnabled(enabled);
	}
}
