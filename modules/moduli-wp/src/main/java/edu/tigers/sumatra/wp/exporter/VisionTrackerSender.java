/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.exporter;

import edu.tigers.moduli.AModule;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.network.MulticastUDPTransmitter;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.TrackerPacketGenerator;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;
import edu.tigers.sumatra.wp.proto.SslVisionWrapperTracked;


/**
 * Export standardized vision tracking data.
 */
public class VisionTrackerSender extends AModule implements IWorldFrameObserver
{
	private MulticastUDPTransmitter transmitter;
	private TrackerPacketGenerator trackerPacketGenerator;

	@Override
	public void startModule()
	{
		String address = getSubnodeConfiguration().getString("address", "224.5.23.2");
		int port = getSubnodeConfiguration().getInt("port", 10010);
		transmitter = new MulticastUDPTransmitter(address, port);

		trackerPacketGenerator = new TrackerPacketGenerator();

		SumatraModel.getInstance().getModule(AWorldPredictor.class).addObserver(this);
	}


	@Override
	public void stopModule()
	{
		SumatraModel.getInstance().getModule(AWorldPredictor.class).removeObserver(this);
	}


	@Override
	public void onNewWorldFrame(final WorldFrameWrapper wfw)
	{
		SslVisionWrapperTracked.TrackerWrapperPacket packet = trackerPacketGenerator.generate(wfw.getSimpleWorldFrame());
		transmitter.send(packet.toByteArray());
	}

}
