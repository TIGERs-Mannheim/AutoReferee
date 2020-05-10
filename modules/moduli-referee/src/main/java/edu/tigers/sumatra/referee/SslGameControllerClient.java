/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.referee;

import java.net.URI;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;

import edu.tigers.sumatra.SslGcApi;
import edu.tigers.sumatra.SslGcEngineConfig;


public class SslGameControllerClient extends WebSocketClient
{
	private static final Logger log = LogManager.getLogger(SslGameControllerClient.class.getName());
	private Set<IGameControllerApiObserver> observers = new CopyOnWriteArraySet<>();
	private SslGcEngineConfig.Config latestConfig;

	public SslGameControllerClient(final URI serverUri)
	{
		super(serverUri);
	}


	public void addObserver(IGameControllerApiObserver o)
	{
		observers.add(o);
		if (latestConfig != null)
		{
			o.onConfigChange(latestConfig);
		}
	}


	public void removeObserver(IGameControllerApiObserver o)
	{
		observers.remove(o);
	}


	@Override
	public void onOpen(final ServerHandshake handshakedata)
	{
		// empty
	}


	@Override
	public void onMessage(final String message)
	{
		SslGcApi.Output.Builder builder = SslGcApi.Output.newBuilder();
		try
		{
			JsonFormat.parser().merge(message, builder);
		} catch (InvalidProtocolBufferException e)
		{
			log.warn("Could not parse GC API output", e);
		}
		SslGcApi.Output output = builder.build();
		if (output.hasConfig())
		{
			latestConfig = output.getConfig();
			observers.forEach(o -> o.onConfigChange(latestConfig));
		}
	}


	@Override
	public void onClose(final int code, final String reason, final boolean remote)
	{
		log.debug("WS closed: " + code + " " + reason + " remote=" + remote);
	}


	@Override
	public void onError(final Exception ex)
	{
		log.warn("WS error: " + ex.getMessage(), ex);
	}


	public void sendEvent(SslGcApi.Input event)
	{
		try
		{
			send(JsonFormat.printer()
					.omittingInsignificantWhitespace()
					.print(event));
		} catch (InvalidProtocolBufferException e)
		{
			log.warn("Could not serialize game controller event.", e);
		}
	}
}
