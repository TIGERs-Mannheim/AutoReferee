/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.referee.source.refbox;

import static edu.tigers.sumatra.RefboxRemoteControl.SSL_RefereeRemoteControlReply.Outcome;

import java.util.EnumMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;

import edu.tigers.sumatra.RefboxRemoteControl.SSL_RefereeRemoteControlRequest;
import edu.tigers.sumatra.Referee.SSL_Referee;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.referee.source.ARefereeMessageSource;
import edu.tigers.sumatra.referee.source.ERefereeMessageSource;
import edu.tigers.sumatra.referee.source.refbox.time.ITimeProvider;


/**
 * Internal RefBox implementation.
 * 
 * @author AndreR <andre@ryll.cc>
 */
public class RefBox extends ARefereeMessageSource
{
	private static final Logger log = Logger.getLogger(RefBox.class.getName());
	
	private RefBoxEngine engine = new RefBoxEngine();
	
	private Map<ETeamColor, Integer> keeperIds = new EnumMap<>(ETeamColor.class);
	
	static
	{
		ConfigRegistration.registerClass("user", RefBox.class);
	}
	
	
	/** Constructor */
	public RefBox()
	{
		super(ERefereeMessageSource.INTERNAL_REFBOX);
		keeperIds.put(ETeamColor.YELLOW, 0);
		keeperIds.put(ETeamColor.BLUE, 0);
	}
	
	
	@Override
	public void start()
	{
		// nothing to do
	}
	
	
	@Override
	public void stop()
	{
		// nothing to do
	}
	
	
	public void update()
	{
		// update keeper ids
		engine.setKeeperId(ETeamColor.YELLOW, keeperIds.get(ETeamColor.YELLOW));
		engine.setKeeperId(ETeamColor.BLUE, keeperIds.get(ETeamColor.BLUE));
		
		// spin the engine
		SSL_Referee msg = engine.spin();
		
		// send referee message
		notifyNewRefereeMessage(msg);
	}
	
	
	@Override
	public void handleControlRequest(final SSL_RefereeRemoteControlRequest request)
	{
		Outcome outcome = engine.handleControlRequest(request);
		if (outcome != Outcome.OK)
		{
			log.warn("Invalid outcome: " + outcome);
		}
		update();
	}
	
	
	/**
	 * Set new time provider.
	 * 
	 * @param provider
	 */
	public void setTimeProvider(final ITimeProvider provider)
	{
		engine.setTimeProvider(provider);
	}
	
	
	@Override
	public void updateKeeperId(BotID keeperId)
	{
		keeperIds.put(keeperId.getTeamColor(), keeperId.getNumber());
	}
}
