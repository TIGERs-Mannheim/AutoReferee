/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoref.presenter;

import static edu.tigers.sumatra.SslGcEngineConfig.Config.Behavior.BEHAVIOR_ACCEPT;
import static edu.tigers.sumatra.SslGcEngineConfig.Config.Behavior.BEHAVIOR_IGNORE;

import java.awt.Component;
import java.awt.EventQueue;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import edu.tigers.autoref.view.main.AutoRefMainPanel;
import edu.tigers.autoref.view.main.StartStopPanel.IStartStopPanelObserver;
import edu.tigers.autoreferee.IAutoRefObserver;
import edu.tigers.autoreferee.engine.EAutoRefMode;
import edu.tigers.autoreferee.engine.detector.EGameEventDetectorType;
import edu.tigers.autoreferee.module.AutoRefModule;
import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.sumatra.SslGcApi;
import edu.tigers.sumatra.SslGcEngineConfig;
import edu.tigers.sumatra.components.EnumCheckBoxPanel.IEnumPanelObserver;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.IGameControllerApiObserver;
import edu.tigers.sumatra.referee.Referee;
import edu.tigers.sumatra.referee.gameevent.EGameEvent;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;
import edu.tigers.sumatra.views.ISumatraView;
import edu.tigers.sumatra.views.ISumatraViewPresenter;


public class AutoRefPresenter
		implements ISumatraViewPresenter, IStartStopPanelObserver, IAutoRefObserver, IGameControllerApiObserver
{
	private AutoRefMainPanel mainPanel = new AutoRefMainPanel();
	private final GameEventBehaviorObserver gameEventBehaviorObserver = new GameEventBehaviorObserver();
	private final GameEventDetectorObserver gameEventDetectorObserver = new GameEventDetectorObserver();

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


	@Override
	public void onModuliStateChanged(final ModulesState state)
	{
		Optional<AutoRefModule> optModule = SumatraModel.getInstance().getModuleOpt(AutoRefModule.class);
		if (!optModule.isPresent())
		{
			return;
		}

		switch (state)
		{
			case ACTIVE:
				optModule.ifPresent(autoRef -> autoRef.addObserver(this));
				EventQueue.invokeLater(() -> mainPanel.setEnabled(true));
				mainPanel.getStartStopPanel().addObserver(this);
				mainPanel.getGameEventDetectorPanel().addObserver(gameEventDetectorObserver);
				mainPanel.getGameEventDetectorPanel().setSelectedBoxes(EGameEventDetectorType.valuesEnabledByDefault());
				mainPanel.getGameEventBehaviorPanel().addObserver(gameEventBehaviorObserver);
				optModule.ifPresent(autoRef -> mainPanel.getStartStopPanel().setAutoRefMode(autoRef.getMode()));
				SumatraModel.getInstance().getModule(Referee.class).addGcApiObserver(this);
				break;
			case NOT_LOADED:
			case RESOLVED:
				optModule.ifPresent(autoRef -> autoRef.removeObserver(this));
				mainPanel.getStartStopPanel().removeObserver(this);
				mainPanel.getGameEventDetectorPanel().removeObserver(gameEventDetectorObserver);
				mainPanel.getGameEventBehaviorPanel().removeObserver(gameEventBehaviorObserver);
				EventQueue.invokeLater(() -> mainPanel.setEnabled(false));
				SumatraModel.getInstance().getModule(Referee.class).removeGcApiObserver(this);
				break;
		}
	}


	@Override
	public void onAutoRefModeChanged(EAutoRefMode mode)
	{
		EventQueue.invokeLater(() -> mainPanel.getStartStopPanel().setAutoRefMode(mode));
	}


	@Override
	public void changeMode(final EAutoRefMode mode)
	{
		EventQueue.invokeLater(() -> SumatraModel.getInstance().getModule(AutoRefModule.class).changeMode(mode));
	}


	@Override
	public void onNewGameEventDetected(final IGameEvent gameEvent)
	{
		// empty
	}


	@Override
	public void onConfigChange(final SslGcEngineConfig.Config config)
	{
		Set<EGameEvent> enabledEvents = Arrays.stream(EGameEvent.values())
				.filter(e -> config.getGameEventBehaviorMap().get(e.name()) != BEHAVIOR_IGNORE)
				.collect(Collectors.toSet());
		config.getGameEventBehaviorMap()
				.forEach((k, v) -> mainPanel.getGameEventBehaviorPanel().setSelectedBoxes(enabledEvents));
	}

	private static class GameEventBehaviorObserver implements IEnumPanelObserver<EGameEvent>
	{
		@Override
		public void onValueTicked(final EGameEvent type, final boolean value)
		{
			String key = type.name();
			SslGcEngineConfig.Config.Behavior behavior = value
					? BEHAVIOR_ACCEPT
					: BEHAVIOR_IGNORE;
			SumatraModel.getInstance().getModule(Referee.class).sendGameControllerEvent(
					SslGcApi.Input.newBuilder()
							.setConfigDelta(SslGcEngineConfig.Config.newBuilder().putGameEventBehavior(key, behavior))
							.build());
		}
	}

	private static class GameEventDetectorObserver implements IEnumPanelObserver<EGameEventDetectorType>
	{
		@Override
		public void onValueTicked(final EGameEventDetectorType type, final boolean value)
		{
			AutoRefModule autoRef = SumatraModel.getInstance().getModule(AutoRefModule.class);
			autoRef.setGameEventDetectorActive(type, value);
		}
	}
}
