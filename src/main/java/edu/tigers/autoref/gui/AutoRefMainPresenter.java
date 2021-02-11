/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoref.gui;

import edu.tigers.autoref.AutoRefReplayPresenter;
import edu.tigers.autoref.gui.view.AutoRefMainFrame;
import edu.tigers.moduli.IModuliStateObserver;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.sumatra.AMainPresenter;
import edu.tigers.sumatra.model.ModuliStateAdapter;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.persistence.BerkeleyDb;
import edu.tigers.sumatra.persistence.IRecordObserver;
import edu.tigers.sumatra.persistence.RecordManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * @author "Lukas Magel"
 */
public class AutoRefMainPresenter extends AMainPresenter implements IModuliStateObserver
{
	private static final Logger log = LogManager.getLogger(AutoRefMainPresenter.class);
	private static final String LAST_LAYOUT_FILENAME = "last.ly";
	private static final String DEFAULT_LAYOUT = "default.ly";
	private static final String KEY_LAYOUT_PROP = AutoRefMainPresenter.class.getName() + ".layout";

	private RecordManagerObserver recordManagerObserver;


	/**
	 * Default
	 */
	public AutoRefMainPresenter()
	{
		super(new AutoRefMainFrame());
		final AutoRefMainFrame mainFrame = (AutoRefMainFrame) getMainFrame();

		ModuliStateAdapter.getInstance().addObserver(this);
		mainFrame.activate();

		Runtime.getRuntime().addShutdownHook(new Thread(this::onExit));
	}


	public void setWindowSize(int width, int height)
	{
		getMainFrame().setSize(width, height);
	}


	@Override
	protected String getLastLayoutFile()
	{
		return LAST_LAYOUT_FILENAME;
	}


	@Override
	protected String getLayoutKey()
	{
		return KEY_LAYOUT_PROP;
	}


	@Override
	protected String getDefaultLayout()
	{
		return DEFAULT_LAYOUT;
	}


	@Override
	public void onExit()
	{
		super.onExit();

		SumatraModel.getInstance().stopModules();
		SumatraModel.getInstance().saveUserProperties();
	}


	private void initRecordManagerBinding()
	{
		try
		{
			RecordManager recordManager = SumatraModel.getInstance().getModule(RecordManager.class);
			recordManagerObserver = new RecordManagerObserver();
			recordManager.addObserver(recordManagerObserver);
		} catch (ModuleNotFoundException e)
		{
			log.debug("There is no record manager. Wont't add observer", e);
		}
	}


	private void deinitRecordManagerBinding()
	{
		try
		{
			RecordManager recordManager = SumatraModel.getInstance().getModule(RecordManager.class);
			recordManager.removeObserver(recordManagerObserver);
			recordManagerObserver = null;
		} catch (ModuleNotFoundException e)
		{
			log.debug("There is no record manager. Wont't add observer", e);
		}
	}



	 @Override
	 public void onModuliStateChanged(final ModulesState state)
	 {
		  switch (state)
		  {
				case ACTIVE:
					 initRecordManagerBinding();
					 break;
				case RESOLVED:
					 deinitRecordManagerBinding();
					 break;
				case NOT_LOADED:
				default:
					 break;
		  }
	 }

	private static class RecordManagerObserver implements IRecordObserver
	{
		@Override
		public void onStartStopRecord(final boolean recording)
		{
			// nothing to do here
		}


		@Override
		public void onViewReplay(final BerkeleyDb persistence, final long startTime)
		{
			new AutoRefReplayPresenter().start(persistence, startTime);
		}
	}

}
