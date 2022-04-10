/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.visualizer;

import edu.tigers.sumatra.clock.ThreadUtil;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.thread.NamedThreadFactory;
import edu.tigers.sumatra.util.GlobalShortcuts;
import edu.tigers.sumatra.views.ISumatraPresenter;
import edu.tigers.sumatra.views.ISumatraViewPresenter;
import edu.tigers.sumatra.visualizer.field.VisualizerFieldPresenter;
import edu.tigers.sumatra.visualizer.field.interaction.BallInteractor;
import edu.tigers.sumatra.visualizer.options.VisualizerOptionsPresenter;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.util.List;


/**
 * Presenter for the visualizer.
 */
@Log4j2
public class VisualizerPresenter implements ISumatraViewPresenter, IWorldFrameObserver
{
	private static final int VISUALIZATION_FPS = 24;

	@Getter
	private final VisualizerFieldPresenter fieldPresenter = new VisualizerFieldPresenter();
	@Getter
	private final VisualizerOptionsPresenter optionsPresenter = new VisualizerOptionsPresenter(
			fieldPresenter.getFieldPanel()
	);

	@Getter
	private final VisualizerPanel viewPanel = new VisualizerPanel(
			fieldPresenter.getFieldPanel(),
			optionsPresenter.getOptionsMenu()
	);


	private final BallInteractor ballInteractor = new BallInteractor();
	private Thread updateThread;


	@Override
	public void onStart()
	{
		fieldPresenter.getOnFieldClicks().add(ballInteractor::onFieldClick);

		fieldPresenter.onStart();
		optionsPresenter.onStart();

		NamedThreadFactory factory = new NamedThreadFactory("VisualizerUpdater");
		updateThread = factory.newThread(this::updateLoop);
		updateThread.start();
	}


	@Override
	public void onStop()
	{
		ISumatraViewPresenter.super.onStop();
		if (updateThread != null)
		{
			updateThread.interrupt();
			updateThread = null;
		}

		fieldPresenter.onStop();
		optionsPresenter.onStop();

		GlobalShortcuts.removeAllForComponent(viewPanel);

		fieldPresenter.getOnFieldClicks().clear();
	}


	@Override
	public void onStartModuli()
	{
		ISumatraViewPresenter.super.onStartModuli();

		SumatraModel.getInstance().getModule(AWorldPredictor.class).addObserver(ballInteractor);
		SumatraModel.getInstance().getModule(AWorldPredictor.class).addObserver(fieldPresenter);
	}


	@Override
	public void onStopModuli()
	{
		ISumatraViewPresenter.super.onStopModuli();

		SumatraModel.getInstance().getModule(AWorldPredictor.class).removeObserver(ballInteractor);
		SumatraModel.getInstance().getModule(AWorldPredictor.class).removeObserver(fieldPresenter);
	}


	@Override
	public List<ISumatraPresenter> getChildPresenters()
	{
		return List.of(fieldPresenter, optionsPresenter);
	}


	private void updateLoop()
	{
		while (!Thread.interrupted())
		{
			long t0 = System.nanoTime();
			update();
			long t1 = System.nanoTime();
			long sleep = (1_000_000_000L / VISUALIZATION_FPS) - (t1 - t0);
			if (sleep > 0)
			{
				ThreadUtil.parkNanosSafe(sleep);
			}
		}
	}


	private void update()
	{
		try
		{
			fieldPresenter.update();
			optionsPresenter.update(fieldPresenter.getShapeMaps());
		} catch (Exception e)
		{
			log.error("Exception in visualizer updater", e);
		}
	}
}
