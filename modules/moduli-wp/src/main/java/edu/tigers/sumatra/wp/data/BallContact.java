/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.data;

import com.sleepycat.persist.model.Persistent;
import lombok.AllArgsConstructor;
import lombok.Value;


/**
 * Encode the contact (time/duration) to the ball.
 */
@Persistent
@Value
@AllArgsConstructor
public class BallContact
{
	long current;
	long start;
	long end;
	long visionStart;
	long visionEnd;


	@SuppressWarnings("unused")
	public BallContact()
	{
		current = 0;
		start = 0;
		end = 0;
		visionStart = 0;
		visionEnd = 0;
	}


	public static BallContact def(long timestamp)
	{
		return new BallContact(timestamp, (long) -1e9, (long) -1e9, (long) -1e9, (long) -1e9);
	}


	public boolean hasContact()
	{
		return current == end;
	}


	public boolean hasNoContact()
	{
		return !hasContact();
	}


	public boolean hasContactFromVision()
	{
		return current == visionEnd;
	}


	public boolean hasNoContactFromVision()
	{
		return !hasContactFromVision();
	}


	public boolean hasContactFromVisionOrBarrier()
	{
		return hasContact() || hasContactFromVision();
	}


	public double getContactDuration()
	{
		if (hasNoContact())
		{
			return 0;
		}
		return (end - start) * 1e-9;
	}


	public double getContactDurationFromVision()
	{
		if (hasNoContactFromVision())
		{
			return 0;
		}
		return (visionEnd - visionStart) * 1e-9;
	}


	public boolean hadContact(double horizon)
	{
		return (current - end) * 1e-9 < horizon;
	}


	public boolean hadContactFromVision(double horizon)
	{
		return (current - visionEnd) * 1e-9 < horizon;
	}
}
