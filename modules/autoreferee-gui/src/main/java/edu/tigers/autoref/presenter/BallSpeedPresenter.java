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
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javax.swing.JCheckBox;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import edu.tigers.autoref.view.panel.FixedTimeRangeChartPanel;
import edu.tigers.autoref.view.panel.SumatraViewPanel;
import edu.tigers.moduli.IModuliStateObserver;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.views.ISumatraView;
import edu.tigers.sumatra.views.ISumatraViewPresenter;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.EGameStateNeutral;
import edu.tigers.sumatra.wp.data.TrackedBall;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * Presenter class that displays ball velocity data in a {@link FixedTimeRangeChartPanel} instance to create an
 * oscilloscope like effect where the graph line chases its own tail. To achieve this effect the chart only displays a
 * fixed amount of data points. The presenter measures the frequency with which new data is returned from the model and
 * sets the buffer size of the chart accordingly to always have the chart display enough dots to fill about 90 percent
 * of the width.
 * The plot can be paused by the user. This would normally cause gaps in the plot where it was paused. These gaps also
 * distort the oscilloscope effect since the size of the data point buffer of the chart is set based on the assumption
 * that the entire x range is filled with data points. To circumvent this issue this class does not use the timestamps
 * of the world frames directly but maintains its own timestamp value that is incremented with the time delta of two
 * consecutive frames. To avoid gaps the timestamp value is not updated when the plot is paused. Because of this new
 * data points are displayed directly after the last points before the pause.
 * All operations which modify class level attributes are executed in the Swing GUI thread to avoid race conditions.
 * 
 * @author "Lukas Magel"
 */
public class BallSpeedPresenter implements ISumatraViewPresenter, IWorldFrameObserver, IModuliStateObserver
{
	private SumatraViewPanel			mainPanel				= new SumatraViewPanel();
	private FixedTimeRangeChartPanel	chartPanel;
	private JCheckBox						stopChartCheckbox;
	private JSlider						timeRangeSlider;
	
	private WindowMean					mean						= new WindowMean(10);
	
	/** The absolute time range displayed in the chart in seconds */
	private int								timeRange				= 20;
	private long							curTime					= 0L;
	private Long							lastTimestamp			= 0L;
	private EGameStateNeutral			lastState				= EGameStateNeutral.UNKNOWN;
	
	private boolean						pauseWhenNotRunning	= false;
	
	
	/**
	 * 
	 */
	public BallSpeedPresenter()
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
				chartPanel.clear();
				checkAdjustTraceSize(mean.getMean());
				curTime = 0;
			}
		});
		
		stopChartCheckbox = new JCheckBox("Pause when not Running");
		stopChartCheckbox.setBackground(Color.WHITE);
		stopChartCheckbox.setSelected(pauseWhenNotRunning);
		stopChartCheckbox.addActionListener(e -> {
			pauseWhenNotRunning = stopChartCheckbox.isSelected();
			/*
			 * Reset the timestamp of the last frame to null
			 * This avoids gaps in the graph which would otherwise be created when the graph is paused for a certain amount
			 * of time
			 */
				lastTimestamp = null;
			});
		
		chartPanel = new FixedTimeRangeChartPanel(getTimeRange());
		chartPanel.setColor(Color.RED);
		chartPanel.clipY(0, 15);
		chartPanel.setXTitle("Time [s]");
		chartPanel.setYTitle("Ball Speed [m/s]");
		
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(chartPanel, BorderLayout.CENTER);
		mainPanel.add(stopChartCheckbox, BorderLayout.SOUTH);
		mainPanel.add(timeRangeSlider, BorderLayout.EAST);
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
		return (long) (timeRange * 1e9);
	}
	
	
	@Override
	public void onModuliStateChanged(final ModulesState state)
	{
		Optional<AWorldPredictor> optPredictor = getPredictor();
		
		if (state == ModulesState.ACTIVE)
		{
			/*
			 * Reset the timestamp of the last frame to null
			 * This avoids gaps in the graph
			 * All operations which modify the state of the presenter are exeucted in the gui thread to avoid race
			 * conditions
			 */
			SwingUtilities.invokeLater(() -> lastTimestamp = null);
			optPredictor.ifPresent(predictor -> predictor.addWorldFrameConsumer(this));
		} else if (state == ModulesState.RESOLVED)
		{
			optPredictor.ifPresent(predictor -> predictor.removeWorldFrameConsumer(this));
		}
	}
	
	
	@Override
	public void onNewWorldFrame(final WorldFrameWrapper wFrameWrapper)
	{
		long timestamp = wFrameWrapper.getSimpleWorldFrame().getTimestamp();
		EGameStateNeutral curState = wFrameWrapper.getGameState();
		
		TrackedBall ball = wFrameWrapper.getSimpleWorldFrame().getBall();
		double ballSpeed = ball.getVel().getLength();
		
		/*
		 * All operations which modify the state of the presenter are exeucted in the gui thread to avoid race
		 * conditions
		 */
		SwingUtilities.invokeLater(() -> updateChart(timestamp, curState, ballSpeed));
	}
	
	
	/**
	 * Add a new data point to the chart
	 * 
	 * @param timestamp
	 * @param curState
	 * @param ballSpeed
	 */
	private void updateChart(final long timestamp, final EGameStateNeutral curState, final double ballSpeed)
	{
		long timeDiff = 0;
		if ((curState != lastState) || (lastTimestamp == null))
		{
			lastTimestamp = timestamp;
		} else
		{
			timeDiff = timestamp - lastTimestamp;
			long diffMean = mean.add(timeDiff);
			checkAdjustTraceSize(diffMean);
		}
		
		if (!pauseWhenNotRunning || (curState == EGameStateNeutral.RUNNING))
		{
			curTime += timeDiff;
			chartPanel.addPoint(curTime, ballSpeed);
		}
		
		lastTimestamp = timestamp;
		lastState = curState;
	}
	
	
	/**
	 * Calculates if the data point buffer size of the chart needs to be adjusted if new data points arrive with a time
	 * delta (T) of {@code timeGap} nanoseconds. Since the documentation of the chart library states that adjusting the
	 * buffer is quite expensive this method only adjusts the size if current size and required size differ by at least
	 * 10%.
	 * 
	 * @param timeGap in nanoseconds
	 */
	private void checkAdjustTraceSize(final long timeGap)
	{
		if (timeGap <= 0)
		{
			return;
		}
		
		int requiredSize = (int) (((getTimeRange() * 95) / 100) / timeGap);
		int curSize = chartPanel.getPointBufferSize();
		
		double percentDiff = Math.abs((double) (requiredSize - curSize)) / curSize;
		if (percentDiff > 0.1)
		{
			chartPanel.setPointBufferSize(requiredSize);
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
	
	/**
	 * Maintains a fixed sized list of data values and calculates the mean over all values. If a new value is added after
	 * the maximum size has been reached the oldest value is discarded from the list.
	 * 
	 * @author "Lukas Magel"
	 */
	private class WindowMean
	{
		private final int		maxSize;
		private List<Long>	values	= new LinkedList<>();
		
		
		public WindowMean(final int size)
		{
			maxSize = size;
		}
		
		
		/**
		 * Add a new value to the list and calculate the current mean
		 * 
		 * @param value
		 * @return
		 */
		public long add(final long value)
		{
			append(value);
			return getMean();
		}
		
		
		private void append(final long value)
		{
			if (values.size() >= maxSize)
			{
				values.remove(0);
			}
			values.add(value);
		}
		
		
		public long getMean()
		{
			return values.stream().mapToLong(val -> val).sum() / values.size();
		}
	}
}
