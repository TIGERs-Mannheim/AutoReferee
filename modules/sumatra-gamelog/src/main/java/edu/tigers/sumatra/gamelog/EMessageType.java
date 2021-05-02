/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.gamelog;

/**
 * @author AndreR <andre@ryll.cc>
 */
public enum EMessageType
{
	/** ignore message */
	BLANK(0),
	/** try to guess message type by parsing the data */
	UNKNOWN(1),
	/** */
	SSL_VISION_2010(2),
	/** */
	SSL_REFBOX_2013(3),
	/** */
	SSL_VISION_2014(4);
	
	private final int id;
	
	
	/**
	 */
	EMessageType(final int id)
	{
		this.id = id;
	}
	
	
	/**
	 * @return
	 */
	public int getId()
	{
		return id;
	}
	
	
	/**
	 * Convert an id to an enum.
	 * 
	 * @param id
	 * @return enum
	 */
	public static EMessageType getMessageTypeConstant(final int id)
	{
		for (EMessageType s : values())
		{
			if (s.getId() == id)
			{
				return s;
			}
		}
		
		return UNKNOWN;
	}
}
