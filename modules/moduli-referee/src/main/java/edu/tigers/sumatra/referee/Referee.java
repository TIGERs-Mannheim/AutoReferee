/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.referee;

import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.referee.control.GcEventFactory;
import edu.tigers.sumatra.referee.proto.SslGcApi;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage;
import edu.tigers.sumatra.referee.source.ARefereeMessageSource;
import edu.tigers.sumatra.referee.source.CiRefereeSyncedReceiver;
import edu.tigers.sumatra.referee.source.DirectRefereeMsgForwarder;
import edu.tigers.sumatra.referee.source.ERefereeMessageSource;
import edu.tigers.sumatra.referee.source.NetworkRefereeReceiver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.EnumMap;
import java.util.Map;


/**
 * Implementation of {@link AReferee} which can use various referee message sources.
 */
public class Referee extends AReferee
{
	private static final Logger log = LogManager.getLogger(Referee.class.getName());
	private final Map<ERefereeMessageSource, ARefereeMessageSource> msgSources = new EnumMap<>(
			ERefereeMessageSource.class);

	private ARefereeMessageSource source;
	private SslGameControllerProcess sslGameControllerProcess;
	private boolean internalGameControlledActive = false;


	public Referee()
	{
		msgSources.put(ERefereeMessageSource.NETWORK, new NetworkRefereeReceiver());
		msgSources.put(ERefereeMessageSource.INTERNAL_FORWARDER, new DirectRefereeMsgForwarder());
		msgSources.put(ERefereeMessageSource.CI, new CiRefereeSyncedReceiver());
	}


	@Override
	public void startModule()
	{
		ERefereeMessageSource activeSource = ERefereeMessageSource
				.valueOf(getSubnodeConfiguration().getString("source", ERefereeMessageSource.NETWORK.name()));
		boolean useGameController = getSubnodeConfiguration().getBoolean("gameController", false);
		int port = getPort();
		((NetworkRefereeReceiver) msgSources.get(ERefereeMessageSource.NETWORK)).setPort(port);

		source = msgSources.get(activeSource);

		if (useGameController)
		{
			startGameController();
		}

		source.addObserver(this);
		source.start();

		notifyRefereeMsgSourceChanged(source);
	}


	private int getPort()
	{
		return getSubnodeConfiguration().getInt("port", 10003);
	}


	public void startGameController()
	{
		sslGameControllerProcess = new SslGameControllerProcess();
		if (getSubnodeConfiguration().containsKey("gc-ui-port"))
		{
			sslGameControllerProcess.setGcUiPort(getSubnodeConfiguration().getInt("gc-ui-port"));
		}
		if (source.getType() == ERefereeMessageSource.CI)
		{
			sslGameControllerProcess.setTimeAcquisitionMode("ci");
		} else
		{
			sslGameControllerProcess.setTimeAcquisitionMode("system");
		}
		sslGameControllerProcess.setPublishAddress("224.5.23.1:" + getPort());

		File stateStoreFile = new File("build/state-store.json.stream");
		if (stateStoreFile.exists() && !stateStoreFile.delete())
		{
			log.warn("Could not remove state store file, although it exists");
		}

		new Thread(sslGameControllerProcess).start();
		initGameController();
		internalGameControlledActive = true;
	}


	public void stopGameController()
	{
		if (sslGameControllerProcess != null)
		{
			sslGameControllerProcess.stop();
			sslGameControllerProcess = null;
		}
		internalGameControlledActive = false;
	}


	@Override
	public void initGameController()
	{
		sslGameControllerProcess.getClientBlocking().ifPresent(this::initGameController);
	}


	private void initGameController(SslGameControllerClient client)
	{
		client.sendEvent(GcEventFactory.teamName(ETeamColor.BLUE, "BLUE AI"));
		client.sendEvent(GcEventFactory.teamName(ETeamColor.YELLOW, "YELLOW AI"));
		client.sendEvent(GcEventFactory.stage(SslGcRefereeMessage.Referee.Stage.NORMAL_FIRST_HALF));
	}


	public int getGameControllerUiPort()
	{
		return sslGameControllerProcess.getGcUiPort();
	}


	@Override
	public void stopModule()
	{
		source.stop();
		source.removeObserver(this);
		stopGameController();
	}


	@Override
	public void onNewRefereeMessage(final SslGcRefereeMessage.Referee msg)
	{
		notifyNewRefereeMsg(msg);
	}


	@Override
	public void sendGameControllerEvent(final SslGcApi.Input event)
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
		return msgSources.get(type);
	}


	@Override
	public boolean isInternalGameControllerUsed()
	{
		return internalGameControlledActive;
	}


	public void addGcApiObserver(IGameControllerApiObserver o)
	{
		if (sslGameControllerProcess != null)
		{
			sslGameControllerProcess.getClient().ifPresent(c -> c.addObserver(o));
		}
	}


	public void removeGcApiObserver(IGameControllerApiObserver o)
	{
		if (sslGameControllerProcess != null)
		{
			sslGameControllerProcess.getClient().ifPresent(c -> c.removeObserver(o));
		}
	}
}
