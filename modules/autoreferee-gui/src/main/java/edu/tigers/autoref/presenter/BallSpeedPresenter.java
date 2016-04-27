/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 26, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoref.presenter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.IConfigClient;
import com.github.g3force.configurable.IConfigObserver;

import edu.tigers.autoref.view.panel.FixedTimeRangeChartPanel;
import edu.tigers.autoref.view.panel.SumatraViewPanel;
import edu.tigers.autoreferee.AutoRefConfig;
import edu.tigers.moduli.IModuliStateObserver;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.views.ISumatraView;
import edu.tigers.sumatra.views.ISumatraViewPresenter;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.EGameStateNeutral;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * Presenter class that displays ball velocity data in a {@link FixedTimeRangeChartPanel} instance to create an
 * oscilloscope like effect where the graph line chases its own tail. To achieve this effect the chart only displays a
 * fixed amount of data points. The presenter calculates sets the buffer size of the chart accordingly to always have
 * the chart display enough dots to fill about 90 percent of the width.
 * The plot can be paused by the user. This would normally cause gaps in the plot where it was paused. These gaps also
 * distort the oscilloscope effect since the size of the data point buffer of the chart is set based on the assumption
 * that the entire x range is filled with data points. To circumvent this issue this class does not use the timestamps
 * of the world frames directly but maintains its own timestamp value that is incremented with the time delta of two
 * consecutive frames. To avoid gaps the timestamp value is not updated when the plot is paused. Because of this new
 * data points are displayed directly after the last points before the pause.
 * 
 * @author "Lukas Magel"
 */
public class BallSpeedPresenter implements ISumatraViewPresenter, IWorldFrameObserver, IModuliStateObserver,
		ActionListener
{
	private enum PauseState
	{
		/** The chart has been paused manually by the user */
		MANUAL,
		/** The chart has been paused through the auto pause setting if the gamestate is not running */
		AUTO,
		/** The chart is running */
		RUNNING
	}
	
	/** The period in ms at the end of which the chart is updated */
	private static final int			chartUpdatePeriod		= 50;
	
	/** The absolute time range displayed in the chart in seconds */
	private int								timeRange				= 20;
	private boolean						pauseWhenNotRunning	= false;
	private boolean						pauseRequested			= false;
	private boolean						resumeRequested		= false;
	private PauseState					chartState				= PauseState.RUNNING;
	
	private long							curTime					= 0L;
	private BallSpeedModel				model						= new BallSpeedModel();
	
	
	private Timer							chartTimer;
	
	private SumatraViewPanel			mainPanel				= new SumatraViewPanel();
	private JButton						pauseButton				= new JButton("Pause");
	private JButton						resumeButton			= new JButton("Resume");
	private FixedTimeRangeChartPanel	chartPanel;
	private JCheckBox						stopChartCheckbox;
	private JSlider						timeRangeSlider;
	
	
	/**
	 * 
	 */
	public BallSpeedPresenter()
	{
		setupGUI();
		
		chartTimer = new Timer(chartUpdatePeriod, this);
		chartTimer.setDelay(chartUpdatePeriod);
	}
	
	
	private void setupGUI()
	{
		timeRangeSlider = new JSlider(SwingConstants.VERTICAL, 0, 120, timeRange);
		timeRangeSlider.setPaintTicks(true);
		timeRangeSlider.setPaintLabels(true);
		timeRangeSlider.setMajorTickSpacing(30);
		timeRangeSlider.setMinorTickSpacing(10);
		timeRangeSlider.setBackground(Color.WHITE);
		timeRangeSlider.setToolTipText("Adjust the ball speed time range [s] (Resets the graph!)");
		timeRangeSlider.addChangeListener(e -> {
			if (!timeRangeSlider.getValueIsAdjusting())
			{
				timeRange = Math.max(timeRangeSlider.getValue(), 1);
				chartPanel.setRange(getTimeRange());
				chartPanel.setPointBufferSizeWithPeriod(TimeUnit.MILLISECONDS.toNanos(chartUpdatePeriod));
				chartPanel.clear();
				curTime = 0;
			}
		});
		
		stopChartCheckbox = new JCheckBox("Pause when not RUNNING");
		stopChartCheckbox.setBackground(Color.WHITE);
		stopChartCheckbox.setSelected(pauseWhenNotRunning);
		stopChartCheckbox.addActionListener(e -> {
			pauseWhenNotRunning = stopChartCheckbox.isSelected();
			
			/*
			 * The updateChartState() method only triggers on state transitions that occur after the pauseWhenNotRunning
			 * variable has been altered. To also stop/restart the chart if the pauseWhenNotRunning feature is first
			 * activated/deactivated the state update is performed directly inside the callback
			 */
				synchronized (model)
				{
					if (pauseWhenNotRunning)
					{
						if ((model.getLastState() != EGameStateNeutral.RUNNING) && (chartState == PauseState.RUNNING))
						{
							chartState = PauseState.AUTO;
						}
					} else
					{
						if (chartState == PauseState.AUTO)
						{
							chartState = PauseState.RUNNING;
						}
					}
				}
			});
		
		chartPanel = new FixedTimeRangeChartPanel(getTimeRange(), true);
		chartPanel.setColor(Color.BLUE);
		chartPanel.clipY(0, 15);
		chartPanel.setXTitle("Time [s]");
		chartPanel.setYTitle("Ball Speed [m/s]");
		chartPanel.setPointBufferSizeWithPeriod(TimeUnit.MILLISECONDS.toNanos(chartUpdatePeriod));
		setMaxBallVelocityLine(AutoRefConfig.getMaxBallVelocity());
		
		pauseButton.addActionListener(e -> pauseRequested = true);
		resumeButton.addActionListener(e -> resumeRequested = true);
		
		JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		southPanel.setBackground(Color.WHITE);
		southPanel.add(pauseButton);
		southPanel.add(resumeButton);
		southPanel.add(stopChartCheckbox);
		
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(chartPanel, BorderLayout.CENTER);
		mainPanel.add(southPanel, BorderLayout.SOUTH);
		mainPanel.add(timeRangeSlider, BorderLayout.EAST);
		
		ConfigRegistration.registerConfigurableCallback("autoreferee", new IConfigObserver()
		{
			@Override
			public void afterApply(final IConfigClient configClient)
			{
				setMaxBallVelocityLine(AutoRefConfig.getMaxBallVelocity());
			}
		});
	}
	
	
	@Override
	public Component getComponent()
	{
		return mainPanel;
	}
	
	
	@Override
	public ISumatraView getSumatraView()
	{
		return mainPanel;
	}
	
	
	/**
	 * @return time range in naoseconds
	 */
	private long getTimeRange()
	{
		return TimeUnit.SECONDS.toNanos(timeRange);
	}
	
	
	private void setMaxBallVelocityLine(final double value)
	{
		chartPanel.setHorizontalLine("Max", Color.RED, value);
	}
	
	
	@Override
	public void onModuliStateChanged(final ModulesState state)
	{
		Optional<AWorldPredictor> optPredictor = getPredictor();
		
		if (state == ModulesState.ACTIVE)
		{
			optPredictor.ifPresent(predictor -> {
				predictor.addWorldFrameConsumer(this);
				chartTimer.start();
			});
		} else if (state == ModulesState.RESOLVED)
		{
			chartTimer.stop();
			optPredictor.ifPresent(predictor -> predictor.removeWorldFrameConsumer(this));
		}
	}
	
	
	@Override
	public void onNewWorldFrame(final WorldFrameWrapper wFrameWrapper)
	{
		synchronized (model)
		{
			model.update(wFrameWrapper);
		}
	}
	
	
	@Override
	public void actionPerformed(final ActionEvent e)
	{
		synchronized (model)
		{
			updateChart();
			model.reset();
		}
	}
	
	
	private void updateChart()
	{
		updateChartState();
		
		if (chartState == PauseState.RUNNING)
		{
			curTime += TimeUnit.MILLISECONDS.toNanos(chartUpdatePeriod);
			chartPanel.addPoint(curTime, model.getLastBallSpeed());
		}
	}
	
	
	/**
	 * Updates the state of the chart according to the gamestate and requests from the user.
	 * The chart can be in a RUNNING, MANUAL (manually stopped by the user) or AUTO (automatically stopped
	 * by the "Pause When Not Running" feature).
	 */
	private void updateChartState()
	{
		if (model.hasGameStateChanged() && pauseWhenNotRunning && (chartState != PauseState.MANUAL))
		{
			/*
			 * The auto pause feature is activated, a gamestate change has been detected, and the chart is not in a
			 * manually paused state. This means that depending on the current state the chart is either put in auto pause
			 * or running state
			 */
			if (model.getLastState() == EGameStateNeutral.RUNNING)
			{
				chartState = PauseState.RUNNING;
			} else
			{
				chartState = PauseState.AUTO;
			}
		}
		
		/*
		 * A manualy pause request will override the automatic pause/resume mechanism and will cause the chart to be
		 * paused until the resume button is pressed
		 */
		if (pauseRequested)
		{
			chartState = PauseState.MANUAL;
			pauseRequested = false;
		}
		/*
		 * The resume request will put the chart back into the running state no matter if it was manually or automatically
		 * paused.
		 */
		if (resumeRequested)
		{
			chartState = PauseState.RUNNING;
			resumeRequested = false;
		}
	}
	
	
	private Optional<AWorldPredictor> getPredictor()
	{
		try
		{
			AWorldPredictor predictor = (AWorldPredictor) SumatraModel.getInstance().getModule(AWorldPredictor.MODULE_ID);
			return Optional.of(predictor);
		} catch (ModuleNotFoundException e)
		{
		}
		return Optional.empty();
	}
	
	
}
