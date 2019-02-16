/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.referee;

import java.util.ArrayList;
import java.util.List;

import edu.tigers.sumatra.Referee.SSL_Referee;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.referee.control.Event;
import edu.tigers.sumatra.referee.control.GcEventFactory;
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
	private long currentTime = 0;
	private Thread gcUpdateThread;
	
	
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
			sslGameControllerProcess = new SslGameControllerProcess(getGameControllerUiPort());
			new Thread(sslGameControllerProcess).start();
			
			sslGameControllerProcess.getClientBlocking().ifPresent(c -> c.sendEvent(GcEventFactory.triggerResetMatch()));
			sslGameControllerProcess.getClientBlocking().ifPresent(c -> c.sendEvent(GcEventFactory.timestamp(0)));
			sslGameControllerProcess.getClientBlocking()
					.ifPresent(c -> c.sendEvent(GcEventFactory.teamName(ETeamColor.BLUE, "BLUE AI")));
			sslGameControllerProcess.getClientBlocking()
					.ifPresent(c -> c.sendEvent(GcEventFactory.teamName(ETeamColor.YELLOW, "YELLOW AI")));
			sslGameControllerProcess.getClientBlocking().ifPresent(c -> c.sendEvent(GcEventFactory.nextStage()));
			
			gcUpdateThread = new Thread(this::updateGcTime);
			gcUpdateThread.setName("GC_Time_Update");
			gcUpdateThread.start();
		}
	}
	
	
	private void updateGcTime()
	{
		while (!gcUpdateThread.isInterrupted())
		{
			sslGameControllerProcess.getClientBlocking()
					.ifPresent(c -> c.sendEvent(GcEventFactory.timestamp(currentTime)));
			try
			{
				Thread.sleep(100);
			} catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
				return;
			}
		}
	}
	
	
	public int getGameControllerUiPort()
	{
		return getSubnodeConfiguration().getInt("gc-ui-port", 50543);
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
		if (gcUpdateThread != null)
		{
			gcUpdateThread.interrupt();
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
	
	
	@Override
	public void setCurrentTime(long timestamp)
	{
		currentTime = timestamp;
	}
}
