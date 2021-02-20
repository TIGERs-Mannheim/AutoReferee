/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.network;


import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.NoRouteToHostException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


/**
 * This class is an {@link ITransmitter} implementation capable of sending some {@code byte[]}-data via UDP to a
 * multicast-group.
 */
@Log4j2
public class MulticastUDPTransmitter implements ITransmitter<byte[]>
{
	private final int targetPort;
	private final InetAddress targetAddr;

	private final List<MulticastSocket> sockets = new ArrayList<>();
	private boolean lastSendFailed = false;


	/**
	 * @param targetAddr multicast address to send to
	 * @param targetPort network port to send to
	 */
	public MulticastUDPTransmitter(final String targetAddr, final int targetPort)
	{
		this.targetPort = targetPort;
		this.targetAddr = addressByName(targetAddr);
	}


	private List<NetworkInterface> getNetworkInterfaces()
	{
		try
		{
			return NetworkInterface.networkInterfaces().collect(Collectors.toUnmodifiableList());
		} catch (SocketException e)
		{
			log.error("Could not get available network interfaces", e);
		}
		return Collections.emptyList();
	}


	public void connectToAllInterfaces()
	{
		var networkInterfaces = getNetworkInterfaces();
		for (var nif : networkInterfaces)
		{
			connectTo(nif);
		}
	}


	public void connectTo(String nifName)
	{
		try
		{
			var nif = NetworkInterface.getByName(nifName);
			if (nif != null)
			{
				connectTo(nif);
			} else
			{
				log.warn("Specified nif not found: {}", nifName);
			}
		} catch (SocketException e)
		{
			log.error("Could not get an interface by name", e);
		}
	}


	private void connectTo(NetworkInterface nif)
	{
		try
		{
			if (nif.supportsMulticast())
			{
				@SuppressWarnings("squid:S2095") // closing resources: can not close resource here
				var socket = new MulticastSocket();
				socket.setNetworkInterface(nif);
				sockets.add(socket);
			}
		} catch (IOException e)
		{
			log.warn("Could not connect at {}", nif, e);
		}
	}


	private InetAddress addressByName(final String targetAddr)
	{
		try
		{
			return InetAddress.getByName(targetAddr);
		} catch (UnknownHostException err)
		{
			log.error("The Host could not be found!", err);
		}
		return null;
	}


	@Override
	public synchronized boolean send(final byte[] data)
	{
		if (sockets.isEmpty())
		{
			if (!lastSendFailed)
			{
				log.warn("Transmitter is not ready to send!");
				lastSendFailed = true;
			}
			return false;
		}

		DatagramPacket tempPacket = new DatagramPacket(data, data.length, targetAddr, targetPort);

		for (var socket : sockets)
		{
			// Receive _outside_ the synchronized state, to prevent blocking of the state
			try
			{
				socket.send(tempPacket); // DatagramPacket is sent...
				lastSendFailed = false;
			} catch (NoRouteToHostException nrh)
			{
				log.warn("No route to host: '" + targetAddr + "'. Dropping packet...", nrh);
			} catch (IOException err)
			{
				if (!lastSendFailed)
				{
					log.warn("Error while sending data to: '" + targetAddr + ":" + targetPort + "'. "
							+ "If you are not in any network, multicast is not supported by default. "
							+ "On Linux, you can enable multicast on the loopback interface by executing following commands as root: "
							+ "route add -net 224.0.0.0 netmask 240.0.0.0 dev lo && ifconfig lo multicast", err);
					lastSendFailed = true;
				}
			}
		}

		return !lastSendFailed;
	}


	@Override
	public synchronized void close()
	{
		sockets.forEach(MulticastSocket::close);
		sockets.clear();
	}
}
