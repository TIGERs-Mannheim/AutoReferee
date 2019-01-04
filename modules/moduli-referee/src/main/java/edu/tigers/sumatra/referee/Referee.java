/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.referee;

import java.util.ArrayList;
import java.util.List;

import edu.tigers.sumatra.Referee.SSL_Referee;
import edu.tigers.sumatra.referee.events.Event;
import edu.tigers.sumatra.referee.events.GcEventFactory;
import edu.tigers.sumatra.referee.source.ARefereeMessageSource;
import edu.tigers.sumatra.referee.source.DirectRefereeMsgForwarder;
import edu.tigers.sumatra.referee.source.ERefereeMessageSource;
import edu.tigers.sumatra.referee.source.IRefereeSourceObserver;
import edu.tigers.sumatra.referee.source.NetworkRefereeReceiver;


/**
 * Implementation of {@link AReferee} which can use various referee message sources.
 */
public class Referee extends AReferee implements IRefereeSourceObserver
{
	private final List<ARefereeMessageSource> msgSources = new ArrayList<>();
	
	private ARefereeMessageSource source;
	private SslGameControllerProcess sslGameControllerProcess;
	private boolean controllable = false;
	
	
	public Referee()
	{
		msgSources.add(new NetworkRefereeReceiver());
		msgSources.add(new DirectRefereeMsgForwarder());
	}
	
	
	@Override
	public void startModule()
	{
		ERefereeMessageSource activeSource = ERefereeMessageSource
				.valueOf(getSubnodeConfiguration().getString("source", ERefereeMessageSource.NETWORK.name()));
		int port = getSubnodeConfiguration().getInt("port", 10003);
		boolean useGameController = getSubnodeConfiguration().getBoolean("gameController", false);
		
		msgSources.stream()
				.filter(s -> s.getType() == ERefereeMessageSource.NETWORK)
				.findFirst()
				.map(s -> (NetworkRefereeReceiver) s)
				.ifPresent(s -> s.setPort(port));
		
		source = msgSources.stream()
				.filter(s -> s.getType() == activeSource)
				.findAny()
				.orElse(msgSources.get(0));
		
		source.addObserver(this);
		source.start();
		
		controllable = useGameController;
		
		notifyRefereeMsgSourceChanged(source);
		
		if (useGameController)
		{
			sslGameControllerProcess = new SslGameControllerProcess();
			new Thread(sslGameControllerProcess).start();
			
			sslGameControllerProcess.getClientBlocking().ifPresent(c -> c.sendEvent(GcEventFactory.triggerResetMatch()));
		}
	}
	
	
	@Override
	public void stopModule()
	{
		source.stop();
		source.removeObserver(this);
		if (sslGameControllerProcess != null)
		{
			sslGameControllerProcess.stop();
		}
	}
	
	
	@Override
	public void onNewRefereeMessage(final SSL_Referee msg)
	{
		notifyNewRefereeMsg(msg);
	}
	
	
	@Override
	public void sendGameControllerEvent(final Event event)
	{
		if (sslGameControllerProcess != null)
		{
			sslGameControllerProcess.getClient().ifPresent(c -> c.sendEvent(event));
		}
	}
	
	
	@Override
	public ARefereeMessageSource getActiveSource()
	{
		return source;
	}
	
	
	@Override
	public ARefereeMessageSource getSource(final ERefereeMessageSource type)
	{
		return msgSources.stream()
				.filter(s -> s.getType() == type)
				.findAny()
				.orElse(null);
	}
	
	
	@Override
	public boolean isControllable()
	{
		return controllable;
	}
}
