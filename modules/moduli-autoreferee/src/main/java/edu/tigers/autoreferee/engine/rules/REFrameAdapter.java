/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 8, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.rules;

import java.util.List;
import java.util.Optional;

import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.IRuleEngineFrame;
import edu.tigers.autoreferee.engine.calc.BotPosition;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.referee.RefereeMsg;
import edu.tigers.sumatra.wp.data.EGameStateNeutral;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;


/**
 * @author "Lukas Magel"
 */
public class REFrameAdapter implements IRuleEngineFrame
{
	private final IAutoRefFrame	autorefFrame;
	private final FollowUpAction	followUp;
	private final FollowUpAction	lastFollowUp;
	
	
	/**
	 * @param autorefFrame
	 * @param followUp
	 * @param lastFollowUp
	 */
	public REFrameAdapter(final IAutoRefFrame autorefFrame, final FollowUpAction followUp,
			final FollowUpAction lastFollowUp)
	{
		this.autorefFrame = autorefFrame;
		this.followUp = followUp;
		this.lastFollowUp = lastFollowUp;
	}
	
	
	@Override
	public SimpleWorldFrame getWorldFrame()
	{
		return autorefFrame.getWorldFrame();
	}
	
	
	@Override
	public EGameStateNeutral getGameState()
	{
		return autorefFrame.getGameState();
	}
	
	
	@Override
	public BotPosition getBotLastTouchedBall()
	{
		return autorefFrame.getBotLastTouchedBall();
	}
	
	
	@Override
	public Optional<BotPosition> getBotTouchedBall()
	{
		return autorefFrame.getBotTouchedBall();
	}
	
	
	@Override
	public IVector2 getBallLeftFieldPos()
	{
		return autorefFrame.getBallLeftFieldPos();
	}
	
	
	@Override
	public void cleanUp()
	{
	}
	
	
	@Override
	public Optional<FollowUpAction> getFollowUp()
	{
		return Optional.ofNullable(followUp);
	}
	
	
	@Override
	public IRuleEngineFrame getPreviousFrame()
	{
		return new REFrameAdapter(autorefFrame.getPreviousFrame(), lastFollowUp, null);
	}
	
	
	@Override
	public List<EGameStateNeutral> getStateHistory()
	{
		return autorefFrame.getStateHistory();
	}
	
	
	@Override
	public long getTimestamp()
	{
		return autorefFrame.getTimestamp();
	}
	
	
	@Override
	public RefereeMsg getRefereeMsg()
	{
		return autorefFrame.getRefereeMsg();
	}
}
