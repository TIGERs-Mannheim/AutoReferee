/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 13, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.events.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.Configurable;

import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.AutoRefMath;
import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.FollowUpAction.EActionType;
import edu.tigers.autoreferee.engine.NGeometry.BotDistanceComparator;
import edu.tigers.autoreferee.engine.calc.BotPosition;
import edu.tigers.autoreferee.engine.events.EGameEvent;
import edu.tigers.autoreferee.engine.events.GameEvent;
import edu.tigers.autoreferee.engine.events.IGameEvent;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.wp.data.EGameStateNeutral;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;


/**
 * This class detects a violation of the Double Touch Rule which can occur if the bot who performs a
 * kickoff/direct/indirect touches the ball a second time.
 * 
 * @author "Simon Sander"
 */
public class DoubleTouchDetector extends APreparingGameEventDetector
{
	private static final int					priority						= 1;
	private static final Logger				log							= Logger.getLogger(DoubleTouchDetector.class);
	
	@Configurable(comment = "[mm] The bot may only once approach the ball by less than this value")
	private static double						SEPARATION_DISTANCE		= 75;
	
	@Configurable(comment = "[mm] The kick is executed if the ball has moved by this distance")
	private static double						KICK_EXECUTED_DISTANCE	= 120;
	
	private boolean								hasMovedAwayFromBall		= false;
	private boolean								kickExecuted				= false;
	private long									kickStamp					= 0;
	
	private IVector2								ballKickPos					= null;
	private BotID									kickerID						= null;
	
	private static Set<EGameStateNeutral>	VALID_PREVIOUS_STATES;
	
	static
	{
		AGameEventDetector.registerClass(DoubleTouchDetector.class);
		
		Set<EGameStateNeutral> states = EnumSet.of(
				EGameStateNeutral.KICKOFF_BLUE, EGameStateNeutral.KICKOFF_YELLOW,
				EGameStateNeutral.DIRECT_KICK_BLUE, EGameStateNeutral.DIRECT_KICK_YELLOW,
				EGameStateNeutral.INDIRECT_KICK_BLUE, EGameStateNeutral.INDIRECT_KICK_YELLOW);
		VALID_PREVIOUS_STATES = Collections.unmodifiableSet(states);
	}
	
	
	/**
	 * 
	 */
	public DoubleTouchDetector()
	{
		super(EGameStateNeutral.RUNNING);
	}
	
	
	@Override
	public int getPriority()
	{
		return priority;
	}
	
	
	@Override
	protected void prepare(final IAutoRefFrame frame)
	{
		List<EGameStateNeutral> stateHistory = frame.getStateHistory();
		if ((stateHistory.size() > 1) && VALID_PREVIOUS_STATES.contains(stateHistory.get(1)))
		{
			IAutoRefFrame lastFrame = frame.getPreviousFrame();
			ballKickPos = lastFrame.getLastStopBallPosition();
			
			SimpleWorldFrame wFrame = lastFrame.getWorldFrame();
			Collection<ITrackedBot> robots = wFrame.getBots().values();
			kickerID = robots.stream().sorted(new BotDistanceComparator(ballKickPos))
					.findFirst().map(bot -> bot.getBotId()).orElse(null);
			kickStamp = frame.getTimestamp();
		}
	}
	
	
	@Override
	public Optional<IGameEvent> doUpdate(final IAutoRefFrame frame, final List<IGameEvent> violations)
	{
		if (kickerID == null)
		{
			return Optional.empty();
		}
		
		BotPosition curLastCloseBotPos = frame.getLastBotCloseToBall();
		if (!kickerID.equals(curLastCloseBotPos.getId()))
		{
			// The ball has been touched by another robot
			doReset();
			return Optional.empty();
		}
		
		ITrackedBot kickerBot = frame.getWorldFrame().getBot(kickerID);
		if (kickerBot == null)
		{
			log.debug("Tracked bot disappeard from the field: " + kickerID);
			return Optional.empty();
		}
		
		IVector2 ballPos = frame.getWorldFrame().getBall().getPos();
		double botBallDist = GeoMath.distancePP(ballPos, kickerBot.getPos());
		double ballToKickPosDist = GeoMath.distancePP(ballPos, ballKickPos);
		double botBallRadius = Geometry.getBallRadius() + Geometry.getBotRadius();
		
		if (botBallDist > (SEPARATION_DISTANCE + botBallRadius))
		{
			hasMovedAwayFromBall = true;
		}
		
		if (ballToKickPosDist > (KICK_EXECUTED_DISTANCE + botBallRadius))
		{
			kickExecuted = true;
		}
		
		
		if (kickExecuted && (
				!hasMovedAwayFromBall
				|| ((curLastCloseBotPos.getTs() > kickStamp) && curLastCloseBotPos.getId().equals(kickerID))
				))
		{
			ETeamColor kickerColor = kickerID.getTeamColor();
			
			IVector2 kickPos = AutoRefMath.getClosestFreekickPos(ballPos, kickerColor.opposite());
			FollowUpAction followUp = new FollowUpAction(EActionType.INDIRECT_FREE, kickerColor.opposite(),
					kickPos);
			GameEvent violation = new GameEvent(EGameEvent.DOUBLE_TOUCH, frame.getTimestamp(),
					kickerID, followUp);
			doReset();
			return Optional.of(violation);
		}
		
		return Optional.empty();
	}
	
	
	@Override
	public void doReset()
	{
		kickExecuted = false;
		hasMovedAwayFromBall = false;
		kickStamp = 0;
		ballKickPos = null;
		kickerID = null;
	}
	
	
}
