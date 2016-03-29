/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 26, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoref.view.panel;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.IAxis;
import info.monitorenter.gui.chart.ITracePoint2D;
import info.monitorenter.gui.chart.rangepolicies.RangePolicyFixedViewport;
import info.monitorenter.gui.chart.traces.Trace2DLtd;
import info.monitorenter.gui.chart.traces.painters.TracePainterLine;
import info.monitorenter.util.Range;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;


/**
 * This chart class displays time based values in a fixed time range like an Oscilloscope. To achieve this data points
 * are wrapped around if they exceed the displayed range. It uses a specialized painter class to avoid painting long
 * strokes which are caused by large differences in x.
 * 
 * @author "Lukas Magel"
 */
public class FixedTimeRangeChartPanel extends JPanel
{
	/**  */
	private static final long	serialVersionUID	= -5176647826548801416L;
	
	private long					timeRange;
	
	private Chart2D				mainChart			= new Chart2D();
	private Trace2DLtd			mainTrace			= new Trace2DLtd();
	
	
	/**
	 * @param timeRange The time range that will be displayed in nanoseconds
	 */
	public FixedTimeRangeChartPanel(final long timeRange)
	{
		setLayout(new BorderLayout());
		add(mainChart, BorderLayout.CENTER);
		
		setupChart();
		
		setRange(timeRange);
	}
	
	
	private void setupChart()
	{
		IAxis<?> xAxis = mainChart.getAxisX();
		IAxis<?> yAxis = mainChart.getAxisY();
		
		xAxis.setPaintGrid(true);
		xAxis.setRangePolicy(new RangePolicyFixedViewport());
		yAxis.setPaintGrid(true);
		
		/*
		 * Setup the main trace of the graph
		 * The trace uses the special painter class to avoid long strokes which are caused by larger x value differences.
		 */
		mainTrace.setTracePainter(new NoCarriageReturnLinePainter());
		mainTrace.setName(null);
		mainChart.addTrace(mainTrace);
		
		mainChart.setGridColor(Color.LIGHT_GRAY);
	}
	
	
	/**
	 * Add a new data point to the chart
	 * 
	 * @param timestamp in nanoseconds
	 * @param y
	 */
	public void addPoint(final long timestamp, final double y)
	{
		double x = (timestamp % timeRange) / 1e9;
		mainTrace.addPoint(x, y);
	}
	
	
	/**
	 * Set the displayed y range
	 * 
	 * @param min
	 * @param max
	 */
	public void clipY(final double min, final double max)
	{
		mainChart.getAxisY().setRangePolicy(new RangePolicyFixedViewport(new Range(min, max)));
	}
	
	
	/**
	 * Set the color of the data plot
	 * 
	 * @param color
	 */
	public void setColor(final Color color)
	{
		mainTrace.setColor(color);
	}
	
	
	/**
	 * X axis title
	 * 
	 * @param title
	 */
	public void setXTitle(final String title)
	{
		mainChart.getAxisX().getAxisTitle().setTitle(title);
	}
	
	
	/**
	 * Y axis title
	 * 
	 * @param title
	 */
	public void setYTitle(final String title)
	{
		mainChart.getAxisY().getAxisTitle().setTitle(title);
	}
	
	
	/**
	 * The size of the data point buffer of the chart
	 * 
	 * @return
	 */
	public int getPointBufferSize()
	{
		return mainTrace.getMaxSize();
	}
	
	
	/**
	 * The time range that is displayed
	 * 
	 * @param range_ns in nanoseconds
	 */
	public void setRange(final long range_ns)
	{
		timeRange = range_ns;
		mainChart.getAxisX().setRange(new Range(0, timeRange / 1e9));
	}
	
	
	/**
	 * @param bufSize
	 */
	public void setPointBufferSize(final int bufSize)
	{
		mainTrace.setMaxSize(bufSize);
	}
	
	
	/**
	 * Remove all currently displayed data points
	 */
	public void clear()
	{
		mainTrace.removeAllPoints();
	}
	
	/**
	 * Special line painter class which only draws a line if the x values of line start and end are not spaced too far
	 * apart.
	 * 
	 * @author "Lukas Magel"
	 */
	private class NoCarriageReturnLinePainter extends TracePainterLine
	{
		
		/**  */
		private static final long	serialVersionUID	= 672321723106037578L;
		
		
		@Override
		public void paintPoint(final int absoluteX, final int absoluteY, final int nextX, final int nextY,
				final Graphics g, final ITracePoint2D original)
		{
			if (Math.abs((nextX - absoluteX)) < 10)
			{
				super.paintPoint(absoluteX, absoluteY, nextX, nextY, g, original);
			}
		}
		
	}
	
	
}
