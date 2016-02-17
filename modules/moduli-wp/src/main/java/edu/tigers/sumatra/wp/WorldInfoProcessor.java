/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Sep 2, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.wp;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.AReferee;
import edu.tigers.sumatra.referee.IRefereeObserver;
import edu.tigers.sumatra.referee.RefereeMsg;
import edu.tigers.sumatra.wp.data.ShapeMap;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;
import edu.tigers.sumatra.wp.vis.BallVisCalc;
import edu.tigers.sumatra.wp.vis.BorderVisCalc;
import edu.tigers.sumatra.wp.vis.BotVisCalc;
import edu.tigers.sumatra.wp.vis.BufferCalc;
import edu.tigers.sumatra.wp.vis.GameStateCalc;
import edu.tigers.sumatra.wp.vis.IVisCalc;
import edu.tigers.sumatra.wp.vis.RefereeVisCalc;
import edu.tigers.sumatra.wp.vis.VelocityVisCalc;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class WorldInfoProcessor implements IRefereeObserver
{
	@SuppressWarnings("unused")
	private static final Logger	log					= Logger.getLogger(WorldInfoProcessor.class.getName());
	
	private final List<IVisCalc>	calculators			= new ArrayList<>();
	
	private RefereeMsg				latestRefereeMsg	= new RefereeMsg();
	
	
	/**
	 * 
	 */
	public WorldInfoProcessor()
	{
		calculators.add(new BallVisCalc());
		calculators.add(new BorderVisCalc());
		calculators.add(new GameStateCalc());
		calculators.add(new BotVisCalc());
		calculators.add(new BufferCalc());
		calculators.add(new RefereeVisCalc());
		calculators.add(new VelocityVisCalc());
	}
	
	
	/**
	 * 
	 */
	public void start()
	{
		latestRefereeMsg = new RefereeMsg();
		try
		{
			AReferee referee = (AReferee) SumatraModel.getInstance().getModule(AReferee.MODULE_ID);
			referee.addObserver(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find referee module");
		}
	}
	
	
	/**
	 * 
	 */
	public void stop()
	{
		try
		{
			AReferee referee = (AReferee) SumatraModel.getInstance().getModule(AReferee.MODULE_ID);
			referee.removeObserver(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("Could not find referee module");
		}
	}
	
	
	@Override
	public void onNewRefereeMsg(final SSL_Referee refMsg)
	{
		latestRefereeMsg = new RefereeMsg(refMsg);
	}
	
	
	/**
	 * @param swf
	 * @return
	 */
	public WorldFrameWrapper processSimpleWorldFrame(final SimpleWorldFrame swf)
	{
		WorldFrameWrapper wrapper = new WorldFrameWrapper(swf, latestRefereeMsg, new ShapeMap());
		for (IVisCalc calc : calculators)
		{
			calc.process(wrapper);
		}
		return wrapper;
	}
}
