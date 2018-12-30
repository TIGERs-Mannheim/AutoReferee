/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.game;

import org.junit.Assert;
import org.junit.Test;


public class MessageSignerTest
{
	
	@Test
	public void signingTest()
	{
		// Check Signing by singing and verifying some test data
		MessageSigner signer = new MessageSigner();
		byte[] data = "Foo".getBytes();
		
		byte[] sig = signer.sign(data);
		Assert.assertTrue(signer.verify(data, sig));
		Assert.assertFalse(signer.verify("Bar".getBytes(), sig));
	}
	
}