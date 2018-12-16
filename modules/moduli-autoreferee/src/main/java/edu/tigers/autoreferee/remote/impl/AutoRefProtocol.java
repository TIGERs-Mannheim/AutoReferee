/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.remote.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import edu.tigers.autoreferee.engine.events.GameEventResponse;
import edu.tigers.autoreferee.engine.events.IGameEvent;
import edu.tigers.autoreferee.remote.IRefboxRemote;
import edu.tigers.sumatra.game.GameControllerProtocol;
import edu.tigers.sumatra.game.MessageSigner;
import edu.tigers.sumatra.gamecontroller.SslGameControllerAutoRef;
import edu.tigers.sumatra.gamecontroller.SslGameControllerCommon;


/**
 * @author "Lukas Magel"
 */
public class AutoRefProtocol implements IRefboxRemote, Runnable
{
	private static final String AUTOREF_ID = "TIGERs Mannheim AutoRef";
	private static final Logger log = Logger.getLogger(AutoRefProtocol.class);
	
	private Thread thread;
	private GameControllerProtocol protocol;
	private boolean running = false;
	private CountDownLatch terminationLatch = null;
	
	private LinkedBlockingDeque<QueueEntry> commandQueue;
	
	private List<IGameEventResponseObserver> responseObserverList = new ArrayList<>();
	
	private String nextToken;
	private MessageSigner signer;
	
	
	/**
	 * @param hostname the hostname
	 * @param port
	 */
	public AutoRefProtocol(final String hostname, final int port)
	{
		protocol = new GameControllerProtocol(hostname, port);
		protocol.addConnectedHandler(this::register);
		
		commandQueue = new LinkedBlockingDeque<>();
		thread = new Thread(this, "AutoRefCommunicator");
		// disable signer for know - buggy with integration tests and might be replaced
		signer = new MessageSigner(null, null);
		// getClass().getResource("/keys/TIGERs-Mannheim-autoRef.key.pem.pkcs8")
		// getClass().getResource("/keys/TIGERs-Mannheim-autoRef.pub.pem")
	}
	
	
	private void register()
	{
		SslGameControllerAutoRef.ControllerToAutoRef reply;
		reply = protocol.receiveMessage(SslGameControllerAutoRef.ControllerToAutoRef.parser());
		if (reply == null || !reply.hasControllerReply())
		{
			log.error("Receiving initial Message failed");
			return;
		}
		
		nextToken = reply.getControllerReply().getNextToken();
		
		SslGameControllerAutoRef.AutoRefRegistration.Builder registration = SslGameControllerAutoRef.AutoRefRegistration
				.newBuilder()
				.setIdentifier(AUTOREF_ID);
		registration.getSignatureBuilder().setToken(nextToken).setPkcs1V15(ByteString.EMPTY);
		byte[] signature = signer.sign(registration.build().toByteArray());
		registration.getSignatureBuilder().setPkcs1V15(ByteString.copyFrom(signature));
		
		protocol.sendMessage(registration.build());
		
		reply = protocol.receiveMessage(SslGameControllerAutoRef.ControllerToAutoRef.parser());
		if (reply == null)
		{
			log.error("Receiving AutoRefRegistration reply failed");
		} else if (reply.getControllerReply().getStatusCode() != SslGameControllerCommon.ControllerReply.StatusCode.OK)
		{
			log.error("Server did not allow registration: " + reply.getControllerReply().getStatusCode() + " - "
					+ reply.getControllerReply().getReason());
		} else
		{
			log.info("Successfully registered AutoRef");
			nextToken = reply.getControllerReply().getNextToken();
		}
	}
	
	
	/**
	 * Connect to the refbox via the specified hostname and port
	 * 
	 * @throws IOException
	 */
	public synchronized void start()
	{
		thread.setName("AutoRefCommunication");
		thread.start();
	}
	
	
	@Override
	public synchronized void stop()
	{
		protocol.disconnect();
		running = false;
		terminationLatch = new CountDownLatch(1);
		thread.interrupt();
		try
		{
			Validate.isTrue(terminationLatch.await(2, TimeUnit.SECONDS));
		} catch (InterruptedException e)
		{
			log.warn("Interrupted while waiting for termination", e);
			Thread.currentThread().interrupt();
		}
	}
	
	
	@Override
	public void sendEvent(final IGameEvent event)
	{
		QueueEntry entry = new QueueEntry(event);
		commandQueue.add(entry);
	}
	
	
	@Override
	public void run()
	{
		protocol.connectBlocking();
		running = true;
		while (running)
		{
			readWriteLoop();
		}
		
		if (terminationLatch != null)
		{
			terminationLatch.countDown();
		}
	}
	
	
	/**
	 * @throws InterruptedException
	 * @throws InvalidProtocolBufferException
	 * @throws IOException
	 */
	private void readWriteLoop()
	{
		while (running)
		{
			QueueEntry entry;
			try
			{
				entry = commandQueue.take();
				SslGameControllerAutoRef.AutoRefToController.Builder req = SslGameControllerAutoRef.AutoRefToController
						.newBuilder();
				req.setGameEvent(entry.getEvent().toProtobuf());
				
				if (nextToken != null)
				{
					req.getSignatureBuilder().setToken(nextToken).setPkcs1V15(ByteString.EMPTY);
					byte[] signature = signer.sign(req.build().toByteArray());
					req.getSignatureBuilder().setPkcs1V15(ByteString.copyFrom(signature));
				}
				
				if (!protocol.sendMessage(req.build()))
				{
					
					log.info(String.format("Put game event '%s' back into queue after lost connection",
							entry.getEvent().getType()));
					commandQueue.addFirst(entry);
					continue;
				}
				SslGameControllerAutoRef.ControllerToAutoRef reply = protocol
						.receiveMessage(SslGameControllerAutoRef.ControllerToAutoRef.parser());
				if (reply == null || !reply.hasControllerReply())
				{
					log.error("Receiving GameController Reply failed");
				} else if (reply.getControllerReply()
						.getStatusCode() != SslGameControllerCommon.ControllerReply.StatusCode.OK)
				{
					log.warn(
							"Remote control rejected command " + entry.getEvent() + " with outcome "
									+ reply.getControllerReply().getStatusCode());
				}
				
				if (reply != null)
				{
					responseObserverList.forEach(a -> a.notify(new GameEventResponse(reply.getControllerReply())));
					nextToken = reply.getControllerReply().getNextToken();
				}
			} catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
				return;
			}
		}
	}
	
	
	public void addGameEventResponseObserver(IGameEventResponseObserver observer)
	{
		this.responseObserverList.add(observer);
	}
	
	private static class QueueEntry
	{
		private final IGameEvent event;
		
		
		public QueueEntry(final IGameEvent event)
		{
			this.event = event;
		}
		
		
		/**
		 * @return the cmd
		 */
		public IGameEvent getEvent()
		{
			return event;
		}
	}
	
	public interface IGameEventResponseObserver
	{
		void notify(GameEventResponse response);
	}
}
