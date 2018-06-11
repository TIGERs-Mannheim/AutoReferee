/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.view.log;

import edu.tigers.sumatra.presenter.log.LogPresenter;
import edu.tigers.sumatra.view.TextPane;
import edu.tigers.sumatra.views.ISumatraView;
import net.miginfocom.swing.MigLayout;
import org.apache.log4j.Priority;

import javax.swing.JMenu;
import javax.swing.JPanel;
import java.util.ArrayList;
import java.util.List;


/**
 * ( @see {@link LogPresenter})
 * 
 * @author AndreR
 */
public class LogPanel extends JPanel implements ISumatraView
{
	private static final long	serialVersionUID	= 1L;
	
	private final TextPane		textPane;
	private final FilterPanel	filterPanel;
	
	
	/**
	 * @param maxCapacity
	 * @param initialLevel
	 */
	public LogPanel(final int maxCapacity, final Priority initialLevel)
	{
		setLayout(new MigLayout("fill, inset 0", "", ""));
		
		textPane = new TextPane(maxCapacity);
		filterPanel = new FilterPanel(initialLevel);
		
		final JPanel display = new JPanel(new MigLayout("fill", "", ""));
		display.add(textPane, "push, grow, wrap");
		display.add(filterPanel, "growx");

		
		add(display, "grow");
	}
	
	
	/**
	 * @return
	 */
	public TextPane getTextPane()
	{
		return textPane;
	}
	
	
	/**
	 * @return
	 */
	public FilterPanel getFilterPanel()
	{
		return filterPanel;
	}
	
	
	/**
	 * @return
	 */
	public SlidePanel getSlidePanel()
	{
		return filterPanel.getSlidePanel();
	}
	
	
	@Override
	public List<JMenu> getCustomMenus()
	{
		return new ArrayList<>();
	}
	
	
	@Override
	public void onShown()
	{
		
	}
	
	
	@Override
	public void onHidden()
	{
		
	}
	
	
	@Override
	public void onFocused()
	{
		
	}
	
	
	@Override
	public void onFocusLost()
	{
		
	}
}
