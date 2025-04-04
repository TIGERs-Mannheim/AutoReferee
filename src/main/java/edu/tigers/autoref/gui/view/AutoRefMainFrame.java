/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoref.gui.view;

import edu.tigers.autoref.AutoRefReplayPresenter;
import edu.tigers.autoref.view.ballspeed.BallSpeedView;
import edu.tigers.autoref.view.gamelog.GameLogView;
import edu.tigers.autoref.view.main.AutoRefView;
import edu.tigers.sumatra.AMainFrame;
import edu.tigers.sumatra.config.ConfigEditorView;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.persistence.PersistenceDb;
import edu.tigers.sumatra.presenter.log.LogView;
import edu.tigers.sumatra.presenter.referee.RefereeView;
import edu.tigers.sumatra.view.replay.ReplayLoadMenu;
import edu.tigers.sumatra.visualizer.VisualizerView;
import net.infonode.docking.RootWindow;
import net.infonode.docking.properties.RootWindowProperties;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.io.Serial;
import java.nio.file.Path;


/**
 * @author "Lukas Magel"
 */
public class AutoRefMainFrame extends AMainFrame implements ReplayLoadMenu.IReplayLoadMenuObserver
{
	@Serial
	private static final long serialVersionUID = 8459059861313702417L;

	private final ReplayLoadMenu replayMenu = new ReplayLoadMenu();


	/**
	 * Default
	 */
	public AutoRefMainFrame()
	{
		setTitle("TIGERs AutoReferee " + SumatraModel.getVersion());

		replayMenu.addObserver(this);
		replayMenu.setMnemonic(KeyEvent.VK_R);

		// gameLogView and AutoRefView must be initialized in this order to ensure that game log is notified
		// about the game log table model
		final GameLogView gameLogView = new GameLogView();
		gameLogView.ensureInitialized();
		final AutoRefView autoRefView = new AutoRefView();
		autoRefView.ensureInitialized();

		addView(new LogView(true));
		addView(new VisualizerView());
		addView(new ConfigEditorView());
		addView(autoRefView);
		addView(gameLogView);
		addView(new BallSpeedView());
		addView(new RefereeView());

		updateViewMenu();
		fillMenuBar();
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}


	/**
	 *
	 */
	private void fillMenuBar()
	{
		JMenu fileMenu = new JMenu("File");

		getJMenuBar().add(fileMenu);
		getJMenuBar().add(replayMenu);

		/*
		 * Adds the menu items for layout and views
		 */
		super.addMenuItems();
	}


	@Override
	protected ImageIcon getFrameIcon()
	{
		return loadIconImage("/whistle.png");
	}


	@Override
	protected RootWindow createRootWindow()
	{
		RootWindow rootWindow = super.createRootWindow();

		/*
		 * Specifies that all floating windows should be created as separate JFrame instead of a JDialog.
		 * This ensures that the HumanRefView Panel can be minimized/maximized independently.
		 */
		RootWindowProperties windowProps = rootWindow.getRootWindowProperties();
		windowProps.getFloatingWindowProperties().setUseFrame(true);

		return rootWindow;
	}


	@Override
	public void onOpenReplay(final PersistenceDb db)
	{
		new AutoRefReplayPresenter().start(db, 0);
	}


	@Override
	public void onCompressReplay(final Path path)
	{
		startReplayCompressionThread(path);
	}
}
