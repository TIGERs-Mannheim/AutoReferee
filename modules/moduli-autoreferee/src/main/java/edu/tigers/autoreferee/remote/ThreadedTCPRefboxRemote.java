/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 9, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.remote;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.RefboxRemoteControl.SSL_RefereeRemoteControlReply;
import edu.dhbw.mannheim.tigers.sumatra.model.data.RefboxRemoteControl.SSL_RefereeRemoteControlReply.Outcome;
import edu.dhbw.mannheim.tigers.sumatra.model.data.RefboxRemoteControl.SSL_RefereeRemoteControlRequest;
import edu.dhbw.mannheim.tigers.sumatra.model.data.RefboxRemoteControl.SSL_RefereeRemoteControlRequest.Builder;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Point;
import edu.tigers.autoreferee.engine.RefCommand;
import edu.tigers.sumatra.math.IVector2;


/**
 * @author "Lukas Magel"
 */
public class ThreadedTCPRefboxRemote implements IRefboxRemote, Runnable
{
	private static final Logger			log				= Logger.getLogger(ThreadedTCPRefboxRemote.class);
	
	private static final int				INT_FIELD_SIZE	= 4;
	
	private boolean							stop;
	private Thread								thread;
	
	private int									nextMsgId		= 0;
	private SocketChannel					socket;
	private ByteBuffer						intBuffer;
	private BlockingQueue<RefCommand>	commandQueue;
	
	
	/**
	 * 
	 */
	public ThreadedTCPRefboxRemote()
	{
		commandQueue = new LinkedBlockingQueue<>();
		thread = new Thread(this, "RefboxRemoteSenderThread");
		intBuffer = ByteBuffer.allocate(INT_FIELD_SIZE);
		intBuffer.order(ByteOrder.BIG_ENDIAN);
	}
	
	
	/**
	 * Connect to the refbox via the specified hostname and port
	 * 
	 * @param hostname the hostname
	 * @param port
	 * @throws IOException
	 */
	public void start(final String hostname, final int port) throws IOException
	{
		socket = SocketChannel.open();
		InetSocketAddress addr = new InetSocketAddress(hostname, port);
		try
		{
			socket.connect(addr);
		} catch (IOException e)
		{
			throw new IOException("Unable to connect to the Refbox: " + e.getMessage(), e);
		}
		thread.start();
	}
	
	
	/**
	 * 
	 */
	public void close()
	{
		stop = true;
		try
		{
			if (socket != null)
			{
				socket.close();
			}
		} catch (IOException e)
		{
			log.warn("Error while closing refbox remote control socket", e);
		}
		try
		{
			thread.interrupt();
			thread.join();
		} catch (InterruptedException e)
		{
			log.warn("Error while joining the sending thread", e);
		}
	}
	
	
	@Override
	public void sendCommand(final RefCommand command)
	{
		try
		{
			commandQueue.put(command);
		} catch (InterruptedException e)
		{
			log.error("", e);
		}
	}
	
	
	@Override
	public void run()
	{
		
		try
		{
			while (!stop)
			{
				RefCommand cmd = commandQueue.take();
				SSL_RefereeRemoteControlRequest request = buildProtobuf(cmd);
				writeRequest(request);
				
				SSL_RefereeRemoteControlReply reply = readReply();
				if (reply.getOutcome() != Outcome.OK)
				{
					log.warn("Unexpected outcome from refbox: " + reply.getOutcome());
				}
			}
		} catch (InterruptedException e)
		{
			log.debug("Interrupted", e);
		} catch (IOException e)
		{
			log.error("", e);
		}
		
	}
	
	
	private SSL_RefereeRemoteControlReply readReply() throws IOException
	{
		prepareBuf(INT_FIELD_SIZE);
		while (intBuffer.hasRemaining())
		{
			socket.read(intBuffer);
		}
		
		intBuffer.flip();
		int msgLength = intBuffer.getInt();
		
		
		prepareBuf(msgLength);
		while (intBuffer.hasRemaining())
		{
			socket.read(intBuffer);
		}
		intBuffer.flip();
		
		return SSL_RefereeRemoteControlReply.parseFrom(intBuffer.array());
	}
	
	
	private void writeRequest(final SSL_RefereeRemoteControlRequest req) throws IOException
	{
		int totalSize = req.getSerializedSize() + INT_FIELD_SIZE;
		prepareBuf(totalSize);
		
		intBuffer.putInt(req.getSerializedSize());
		intBuffer.put(req.toByteArray());
		intBuffer.flip();
		
		while (intBuffer.hasRemaining())
		{
			socket.write(intBuffer);
		}
	}
	
	
	SSL_RefereeRemoteControlRequest buildProtobuf(final RefCommand cmd)
	{
		Builder reqBuilder = SSL_RefereeRemoteControlRequest.newBuilder();
		reqBuilder.setMessageId(nextMsgId++);
		reqBuilder.setCommand(cmd.getCommand());
		
		if (cmd.getKickPos().isPresent())
		{
			IVector2 kickPos = cmd.getKickPos().get();
			Point kickPoint = Point.newBuilder().setX((float) kickPos.x()).setY((float) kickPos.y()).build();
			reqBuilder.setDesignatedPosition(kickPoint);
		}
		
		return reqBuilder.build();
	}
	
	
	private void ensureSize(final int size)
	{
		if (intBuffer.capacity() < size)
		{
			intBuffer = ByteBuffer.allocate(size);
		}
	}
	
	
	private void prepareBuf(final int size)
	{
		ensureSize(size);
		intBuffer.clear();
		intBuffer.limit(size);
	}
}
