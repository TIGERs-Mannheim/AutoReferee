/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.presenter.replay;

import edu.tigers.sumatra.view.replay.ReplayControlPanel;
import edu.tigers.sumatra.views.ASumatraViewPresenter;
import edu.tigers.sumatra.views.ISumatraView;

import java.awt.Component;


/**
 * Replay control view presenter
 */
public class ReplayControlPresenter extends ASumatraViewPresenter
{
	private final ReplayControlPanel panel = new ReplayControlPanel();


	@Override
	public Component getComponent()
	{
		return panel;
	}


	@Override
	public ISumatraView getSumatraView()
	{
		return panel;
	}
	

	public ReplayControlPanel getReplayPanel()
	{
		return panel;
	}
}
