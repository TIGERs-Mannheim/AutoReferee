/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoref.view.main;

import java.awt.BorderLayout;
import java.util.Arrays;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import edu.tigers.autoreferee.engine.events.EGameEvent;
import edu.tigers.autoreferee.engine.events.EGameEventDetectorType;
import edu.tigers.sumatra.components.BetterScrollPane;
import edu.tigers.sumatra.components.EnumCheckBoxPanel;
import edu.tigers.sumatra.components.IEnumPanel;
import edu.tigers.sumatra.util.MigLayoutResizeListener;
import edu.tigers.sumatra.views.ISumatraView;
import net.miginfocom.swing.MigLayout;


/**
 * @author "Lukas Magel"
 */
public class AutoRefMainPanel extends JPanel implements IAutoRefMainPanel, ISumatraView
{
	private static final long serialVersionUID = 1511856775227796442L;
	
	private StartStopPanel startStopPanel = new StartStopPanel();
	private ActiveEnginePanel activeEnginePanel = new ActiveEnginePanel();
	private EnumCheckBoxPanel<EGameEventDetectorType> gameEventDetectorPanel;
	private EnumCheckBoxPanel<EGameEvent> gameEventPanel;
	
	
	/**
	 * 
	 */
	public AutoRefMainPanel()
	{
		gameEventDetectorPanel = new EnumCheckBoxPanel<>(EGameEventDetectorType.class, "Game Event Detectors",
				BoxLayout.PAGE_AXIS);
		gameEventPanel = new EnumCheckBoxPanel<>(EGameEvent.class, "Published Game Events",
				BoxLayout.PAGE_AXIS);
		
		setupGUI();
	}
	
	
	private void setupGUI()
	{
		setLayout(new BorderLayout());
		JPanel panel = new JPanel();
		final BetterScrollPane scrollPane = new BetterScrollPane(panel);
		add(scrollPane, BorderLayout.CENTER);
		
		panel.setLayout(new MigLayout("wrap 2", "[250][250]", "[][]"));
		this.addComponentListener(new MigLayoutResizeListener(this, panel)); // Set number of panels per row based on width

		panel.add(startStopPanel, "grow, top");
		panel.add(activeEnginePanel, "grow, top");
		panel.add(gameEventPanel, "grow x, top");
		panel.add(gameEventDetectorPanel, "grow x, top");
	}
	
	
	@Override
	public IStartStopPanel getStartStopPanel()
	{
		return startStopPanel;
	}
	
	
	@Override
	public IActiveEnginePanel getEnginePanel()
	{
		return activeEnginePanel;
	}
	
	
	@Override
	public IEnumPanel<EGameEventDetectorType> getGameEventDetectorPanel()
	{
		return gameEventDetectorPanel;
	}
	
	
	@Override
	public IEnumPanel<EGameEvent> getGameEventPanel()
	{
		return gameEventPanel;
	}
	
	
	public void setPanelsEnabled(final boolean enabled)
	{
		Arrays.asList(startStopPanel, activeEnginePanel, gameEventDetectorPanel, gameEventPanel).forEach(
				panel -> panel.setPanelEnabled(enabled));
	}
}
