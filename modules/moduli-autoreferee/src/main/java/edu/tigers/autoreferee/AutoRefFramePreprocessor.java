/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee;

import java.util.ArrayList;
import java.util.List;

import edu.tigers.autoreferee.engine.calc.AllowedDistancesVisCalc;
import edu.tigers.autoreferee.engine.calc.BallLeftFieldCalc;
import edu.tigers.autoreferee.engine.calc.BotBallContactCalc;
import edu.tigers.autoreferee.engine.calc.GameStateHistoryCalc;
import edu.tigers.autoreferee.engine.calc.IRefereeCalc;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * The preprocessor runs some calculators to gather some generic information.
 */
public class AutoRefFramePreprocessor
{
	private List<IRefereeCalc> calculators = new ArrayList<>();
	private AutoRefFrame lastFrame;
	
	
	public AutoRefFramePreprocessor()
	{
		calculators.add(new BallLeftFieldCalc());
		calculators.add(new BotBallContactCalc());
		calculators.add(new GameStateHistoryCalc());
		calculators.add(new AllowedDistancesVisCalc());
	}
	
	
	public AutoRefFrame process(final WorldFrameWrapper wFrame)
	{
		AutoRefFrame frame = new AutoRefFrame(lastFrame, wFrame);
		
		if (lastFrame != null)
		{
			// We can only run the calculators if we have a last frame.
			runCalculators(frame);
		}
		setLastFrame(frame);
		return frame;
	}
	
	
	private void setLastFrame(final AutoRefFrame frame)
	{
		if (lastFrame != null)
		{
			lastFrame.cleanUp();
		}
		lastFrame = frame;
	}
	
	
	private void runCalculators(final AutoRefFrame frame)
	{
		for (IRefereeCalc calc : calculators)
		{
			calc.process(frame);
		}
	}
	
	
	public boolean hasLastFrame()
	{
		return lastFrame != null;
	}
}
