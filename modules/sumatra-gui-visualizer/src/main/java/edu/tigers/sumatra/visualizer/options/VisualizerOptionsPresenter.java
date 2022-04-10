/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.visualizer.options;

import edu.tigers.sumatra.drawable.IShapeLayer;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.drawable.ShapeMapSource;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.util.GlobalShortcuts;
import edu.tigers.sumatra.views.ISumatraPresenter;
import edu.tigers.sumatra.visualizer.field.FieldPanel;
import edu.tigers.sumatra.visualizer.field.recorder.MediaRecorderPresenter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Map;


/**
 * Presenter for controlling the optionsPanel in the visualizer
 */
@RequiredArgsConstructor
public class VisualizerOptionsPresenter implements ISumatraPresenter, IOptionsPanelObserver
{
	private final FieldPanel fieldPanel;
	@Getter
	private final VisualizerOptionsMenu optionsMenu = new VisualizerOptionsMenu();
	private boolean saveOptions = false;


	@Override
	public void onStartModuli()
	{
		ISumatraPresenter.super.onStartModuli();
		saveOptions = true;
	}


	@Override
	public void onStopModuli()
	{
		ISumatraPresenter.super.onStopModuli();
		saveOptions = false;
	}


	@Override
	public void onStart()
	{
		ISumatraPresenter.super.onStart();
		optionsMenu.setMediaRecordingListener(new MediaRecorderPresenter(fieldPanel));
		optionsMenu.addObserver(this);
		optionsMenu.setInitialButtonState();
		optionsMenu.setButtonsEnabled(true);

		GlobalShortcuts.add(
				"Reset field",
				optionsMenu,
				() -> onActionFired(EVisualizerOptions.RESET_FIELD, true),
				KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0)
		);
	}


	@Override
	public void onStop()
	{
		ISumatraPresenter.super.onStop();
		optionsMenu.setMediaRecordingListener(null);
		optionsMenu.removeObserver(this);
		GlobalShortcuts.removeAllForComponent(optionsMenu);
	}


	public void update(Map<ShapeMapSource, ShapeMap> shapeMaps)
	{
		shapeMaps.forEach(this::newShapeMap);
	}


	private void newShapeMap(final ShapeMapSource source, final ShapeMap shapeMap)
	{
		optionsMenu.addSourceMenuIfNotPresent(source);
		for (IShapeLayer sl : shapeMap.getAllShapeLayersIdentifiers())
		{
			optionsMenu.addMenuEntry(sl);
		}
	}


	@Override
	public void onCheckboxClick(final String actionCommand, final boolean isSelected)
	{
		if (saveOptions)
		{
			SumatraModel.getInstance().setUserProperty(
					VisualizerOptionsPresenter.class.getCanonicalName() + "." + actionCommand,
					String.valueOf(isSelected));
		}

		reactOnActionCommand(actionCommand, isSelected);
	}


	@Override
	public void onActionFired(EVisualizerOptions option, boolean state)
	{
		switch (option)
		{
			case FANCY -> fieldPanel.setFancyPainting(state);
			case DARK -> fieldPanel.setDarkMode(state);
			case TURN_NEXT -> fieldPanel.turnNext();
			case RESET_FIELD -> fieldPanel.resetField();
		}
	}


	/**
	 * Do what has to be done for the specified action command
	 *
	 * @param actionCommand
	 * @param isSelected
	 */
	private void reactOnActionCommand(final String actionCommand, final boolean isSelected)
	{
		if (actionCommand.startsWith(VisualizerOptionsMenu.SOURCE_PREFIX))
		{
			String layer = actionCommand.replace(VisualizerOptionsMenu.SOURCE_PREFIX, "");
			fieldPanel.setSourceVisibility(layer, isSelected);
		} else if (actionCommand.startsWith(VisualizerOptionsMenu.CATEGORY_PREFIX))
		{
			String layer = actionCommand.replace(VisualizerOptionsMenu.CATEGORY_PREFIX, "");
			fieldPanel.setSourceCategoryVisibility(layer, isSelected);
		} else if (isVisualizerOption(actionCommand))
		{
			EVisualizerOptions option = EVisualizerOptions.valueOf(actionCommand);
			onActionFired(option, isSelected);
		} else
		{
			fieldPanel.setShapeLayerVisibility(actionCommand, isSelected);
		}
	}


	private boolean isVisualizerOption(String actionCommand)
	{
		return Arrays.stream(EVisualizerOptions.values())
				.map(Enum::name)
				.anyMatch(actionCommand::equals);
	}
}
