package edu.tigers.autoreferee.remote;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Command;
import edu.tigers.autoreferee.engine.RefCommand;


/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 10, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */

/**
 * @author "Lukas Magel"
 */
public class RemoteTest
{
	
	/**
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	@Ignore
	public void test() throws IOException, InterruptedException
	{
		ThreadedTCPRefboxRemote remote = new ThreadedTCPRefboxRemote();
		remote.start("localhost", 10007);
		for (int i = 0; i < 10; i++)
		{
			remote.sendCommand(new RefCommand(Command.DIRECT_FREE_BLUE));
			Thread.sleep(1000);
			remote.sendCommand(new RefCommand(Command.STOP));
			Thread.sleep(1000);
		}
		remote.close();
	}
	
}
