/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine;

import java.net.InetAddress;

import org.apache.log4j.Logger;

import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.events.IGameEvent;
import edu.tigers.autoreferee.module.AutoRefModule;
import edu.tigers.autoreferee.remote.AutoRefToGameControllerConnector;
import edu.tigers.autoreferee.remote.GameEventResponse;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.AReferee;
import edu.tigers.sumatra.referee.SslGameControllerProcess;


public class ActiveAutoRefEngine extends AutoRefEngine
{
	private final Logger log = Logger.getLogger(ActiveAutoRefEngine.class.getName());
	private static final String DEFAULT_REFEREE_HOST = "localhost";
	
	private AutoRefToGameControllerConnector remote;
	
	
	@Override
	public void start()
	{
		AReferee referee = SumatraModel.getInstance().getModule(AReferee.class);
		String hostname = referee.getActiveSource().getRefBoxAddress()
				.map(InetAddress::getHostAddress)
				.orElse(DEFAULT_REFEREE_HOST);
		int port = SumatraModel.getInstance().getModule(AutoRefModule.class)
				.getSubnodeConfiguration().getInt("gameControllerPort",
						SslGameControllerProcess.GAME_CONTROLLER_PORT);
		remote = new AutoRefToGameControllerConnector(hostname, port);
		remote.addGameEventResponseObserver(this::onGameControllerResponse);
		remote.start();
	}
	
	
	@Override
	public void stop()
	{
		remote.stop();
	}
	
	
	@Override
	public void process(final IAutoRefFrame frame)
	{
		processEngine(frame).forEach(this::processGameEvent);
	}
	
	
	@Override
	protected void processGameEvent(final IGameEvent gameEvent)
	{
		super.processGameEvent(gameEvent);
		remote.sendEvent(gameEvent);
	}
	
	
	private void onGameControllerResponse(GameEventResponse response)
	{
		if (response.getResponse() != GameEventResponse.Response.OK)
		{
			log.warn("Game-controller response was not OK: " + response);
		}
	}
}
