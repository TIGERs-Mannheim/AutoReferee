/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.view.toolbar;

import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.util.ImageScaler;
import edu.tigers.sumatra.view.FpsPanel;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * The frame tool bar.
 */
@Log4j2
public class ToolBar
{
	private final List<IToolbarObserver> observers = new CopyOnWriteArrayList<>();
	private final JButton btnRecSave = new JButton();

	@Getter
	private final JToolBar jToolBar = new JToolBar();
	@Getter
	private final FpsPanel fpsPanel = new FpsPanel();
	@Getter
	private final JProgressBar heapBar = new JProgressBar();
	@Getter
	private final JLabel heapLabel = new JLabel();


	public ToolBar()
	{
		log.trace("Create toolbar");

		var btnEmergency = new JButton();
		btnEmergency.setForeground(Color.red);
		btnEmergency.addActionListener(actionEvent -> notifyEmergencyStop());
		btnEmergency.setIcon(ImageScaler.scaleDefaultButtonImageIcon("/stop-emergency.png"));
		btnEmergency.setToolTipText("Emergency stop [Esc]");
		btnEmergency.setBorder(BorderFactory.createEmptyBorder());
		btnEmergency.setBackground(new Color(0, 0, 0, 1));

		btnRecSave.addActionListener(actionEvent -> toggleRecord());
		btnRecSave.setIcon(ImageScaler.scaleDefaultButtonImageIcon("/record.png"));
		btnRecSave.setToolTipText("Start/Stop recording");
		btnRecSave.setBorder(BorderFactory.createEmptyBorder());
		btnRecSave.setBackground(new Color(0, 0, 0, 1));

		var btnTournament = new JToggleButton();
		btnTournament.addActionListener(this::toggleTournamentMode);
		btnTournament.setIcon(ImageScaler.scaleDefaultButtonImageIcon("/tournament_bw.png"));
		btnTournament.setToolTipText("Tournament mode (off)");
		btnTournament.setBorder(BorderFactory.createEmptyBorder());
		btnTournament.setBackground(new Color(0, 0, 0, 1));
		btnTournament.setContentAreaFilled(false);

		JPanel heapPanel = new JPanel(new BorderLayout());
		heapLabel.setToolTipText("Memory Usage (current/total/maximum)");
		heapPanel.add(heapLabel, BorderLayout.NORTH);
		heapPanel.add(heapBar, BorderLayout.SOUTH);
		heapBar.setStringPainted(true);
		heapBar.setMinimum(0);
		heapBar.setToolTipText("Memory Usage");

		// --- configure toolbar ---
		jToolBar.setFloatable(false);
		jToolBar.setRollover(true);

		JPanel toolBarPanel = new JPanel();
		toolBarPanel.setLayout(new MigLayout("inset 1"));

		// --- add buttons ---
		toolBarPanel.add(btnEmergency, "left");
		toolBarPanel.add(btnRecSave, "left");
		toolBarPanel.add(btnTournament, "left");
		toolBarPanel.add(fpsPanel, "left");
		toolBarPanel.add(heapPanel, "left");
		jToolBar.add(toolBarPanel);

		// initialize icons
		for (EStartStopButtonState icon : EStartStopButtonState.values())
		{
			log.trace("Load button icon " + icon.name());
		}
	}


	/**
	 * @param o
	 */
	public void addObserver(final IToolbarObserver o)
	{
		observers.add(o);
	}


	/**
	 * @param o
	 */
	public void removeObserver(final IToolbarObserver o)
	{
		observers.remove(o);
	}


	/**
	 * @param recording
	 */
	public void setRecordingEnabled(final boolean recording)
	{
		if (recording)
		{
			btnRecSave.setIcon(ImageScaler.scaleDefaultButtonImageIcon("/recordActive.gif"));
		} else
		{
			btnRecSave.setIcon(ImageScaler.scaleDefaultButtonImageIcon("/record.png"));
		}

		jToolBar.repaint();
	}


	private void notifyEmergencyStop()
	{
		observers.forEach(IToolbarObserver::onEmergencyStop);
	}


	private void toggleTournamentMode(ActionEvent e)
	{
		JToggleButton btn = (JToggleButton) e.getSource();
		SumatraModel.getInstance().setProductive(btn.isSelected());
		if (btn.isSelected())
		{
			btn.setIcon(ImageScaler.scaleDefaultButtonImageIcon("/tournament_color.png"));
			btn.setToolTipText("Tournament mode (on)");
		} else
		{
			btn.setIcon(ImageScaler.scaleDefaultButtonImageIcon("/tournament_bw.png"));
			btn.setToolTipText("Tournament mode (off)");
		}
		jToolBar.repaint();
	}


	private void toggleRecord()
	{
		btnRecSave.setEnabled(false);
		new Thread(() -> {
			observers.forEach(IToolbarObserver::onToggleRecord);
			btnRecSave.setEnabled(true);
		}, "RecordSaveButton").start();
	}
}
