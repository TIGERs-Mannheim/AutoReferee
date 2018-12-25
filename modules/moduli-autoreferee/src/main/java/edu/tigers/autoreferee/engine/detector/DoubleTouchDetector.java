/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.detector;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import edu.tigers.autoreferee.engine.events.AttackerDoubleTouchedBall;
import edu.tigers.autoreferee.engine.events.IGameEvent;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.wp.util.GameStateCalculator;


/**
 * <p>
 * This class detects a violation of the Double Touch Rule which can occur if the bot who performs a
 * kickoff/direct/indirect touches the ball a second time before any other bot touched it
 * -> according to rules from 2017: the bot is allowed to touch the ball more than ones before the ball moved 50mm
 * </p>
 * <p>
 * From the rules (as of 2018):
 * For all restarts where the Laws stipulate that the ball is in play when it is kicked and moves,
 * the robot must clearly tap or kick the ball to make it move. It is understood that the ball
 * may remain in contact with the robot or be bumped by the robot multiple times over a short
 * distance while the kick is being taken, but under no circumstances should the robot remain
 * in contact or touch the ball after it has traveled 50 mm, unless the ball has previously touched
 * another robot. Robots may use dribbling and kicking devices in taking the free kick.
 * </p>
 * <p>
 * This detector assumes that the ball has moved by 50mm, because it is activated when the game switched to running.
 * The {@link GameStateCalculator} uses the same 50mm distance to switch to running.
 * </p>
 */
public class DoubleTouchDetector extends AGameEventDetector
{
	private static final Set<EGameState> VALID_STATES = Collections.unmodifiableSet(EnumSet.of(
			EGameState.KICKOFF, EGameState.DIRECT_FREE, EGameState.INDIRECT_FREE, EGameState.RUNNING));
	
	static
	{
		AGameEventDetector.registerClass(DoubleTouchDetector.class);
	}
	
	private BotID kickerID = null;
	private boolean stillTouching = true;
	private boolean violationRaised = false;
	
	
	public DoubleTouchDetector()
	{
		super(EGameEventDetectorType.DOUBLE_TOUCH, VALID_STATES);
	}
	
	
	@Override
	protected void doPrepare()
	{
		kickerID = null;
		stillTouching = true;
		violationRaised = false;
	}
	
	
	@Override
	public Optional<IGameEvent> doUpdate()
	{
		if (violationRaised)
		{
			return Optional.empty();
		}
		if (kickerID == null
				&& !frame.getGameState().isRunning()
				&& !frame.getBotsTouchingBall().isEmpty())
		{
			// find the kicker bot
			kickerID = frame.getBotsTouchingBall().get(0).getBotID();
		}
		
		if (kickerID == null)
		{
			return Optional.empty();
		}
		
		if (frame.getBotsLastTouchedBall().stream().noneMatch(b -> b.getBotID().equals(kickerID)))
		{
			// The ball has been touched by another robot
			kickerID = null;
			return Optional.empty();
		}
		
		boolean touching = frame.getBotsTouchingBall().stream().anyMatch(b -> b.getBotID().equals(kickerID));
		if (!touching)
		{
			// kicker is not touching ball anymore, next time it touches the ball again, it is a violation
			stillTouching = false;
			return Optional.empty();
		}
		
		if (!stillTouching)
		{
			// kicker touched the ball again
			AttackerDoubleTouchedBall violation = new AttackerDoubleTouchedBall(kickerID, getBall().getPos());
			kickerID = null;
			violationRaised = true;
			return Optional.of(violation);
		}
		
		// situation is not decided yet
		return Optional.empty();
	}
}
