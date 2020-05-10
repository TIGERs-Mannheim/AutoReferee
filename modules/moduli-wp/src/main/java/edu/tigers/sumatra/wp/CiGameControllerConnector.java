/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.tigers.sumatra.SslGcCi;
import edu.tigers.sumatra.SslGcRefereeMessage;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;


/**
 * This connector connects to the CI interface of a local game-controller directly with a fast TCP connection.
 * It sends the current time and the tracker packets to the GC and receives the updated referee messages afterwards.
 */
public class CiGameControllerConnector
{
	private static final Logger log = LogManager.getLogger(CiGameControllerConnector.class);

	private static final String HOSTNAME = "localhost";
	private int port = 10009;
	private Socket socket;

	private TrackerPacketGenerator trackerPacketGenerator;


	public void start()
	{
		trackerPacketGenerator = new TrackerPacketGenerator();
		connect();
	}


	public void stop()
	{
		if (socket != null)
		{
			try
			{
				socket.close();
				socket = null;
			} catch (IOException e)
			{
				log.warn("Closing socket failed", e);
			}
		}
	}


	private void connect()
	{
		try
		{
			socket = new Socket(HOSTNAME, port);
			socket.setTcpNoDelay(true);
			send(0);
		} catch (IOException e)
		{
			log.warn("Connection to SSL-Game-Controller Failed", e);
		}
	}


	private void send(final long timestamp)
	{
		send(SslGcCi.CiInput.newBuilder()
				.setTimestamp(timestamp)
				.build());
	}


	private void send(final SimpleWorldFrame swf)
	{
		send(SslGcCi.CiInput.newBuilder()
				.setTimestamp(swf.getTimestamp())
				.setTrackerPacket(trackerPacketGenerator.generate(swf))
				.build());
	}


	private void send(final SslGcCi.CiInput input)
	{
		try
		{
			input.writeDelimitedTo(socket.getOutputStream());
			socket.getOutputStream().flush();
		} catch (IOException e)
		{
			log.warn("Could not write to socket", e);
		}
	}


	/**
	 * Receive a referee message from the controller
	 */
	private List<SslGcRefereeMessage.Referee> receiveRefereeMessages()
	{
		List<SslGcRefereeMessage.Referee> messages = new ArrayList<>();
		try
		{
			do
			{
				SslGcCi.CiOutput output = SslGcCi.CiOutput.parseDelimitedFrom(socket.getInputStream());
				if (output == null)
				{
					log.warn("Receiving Message failed: Socket was at EOF, most likely the connection was closed");
					stop();
					break;
				}

				if (output.hasRefereeMsg())
				{
					messages.add(output.getRefereeMsg());
				}
			} while (socket.getInputStream().available() > 0);
		} catch (IOException e)
		{
			log.warn("Receiving CI message from SSL-Game-Controller failed", e);
			stop();
		}
		return messages;
	}


	public List<SslGcRefereeMessage.Referee> process(final SimpleWorldFrame swf)
	{
		if (socket == null)
		{
			return Collections.emptyList();
		}

		send(swf);
		return receiveRefereeMessages();
	}


	public void setPort(final int port)
	{
		this.port = port;
	}
}
