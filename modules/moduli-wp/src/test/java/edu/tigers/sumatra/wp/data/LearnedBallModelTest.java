/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 9, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.wp.data;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import Jama.Matrix;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.Vector2;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Ignore
public class LearnedBallModelTest
{
	@SuppressWarnings("unused")
	private static final Logger	log	= Logger.getLogger(LearnedBallModelTest.class.getName());
													
	private static final double	acc	= 0.2;
													
													
	private IVector2 getPosFromMatrix(final Matrix state)
	{
		return new Vector2(state.get(0, 0), state.get(1, 0));
	}
	
	
	private IVector2 getVelFromMatrix(final Matrix state)
	{
		return new Vector2(state.get(3, 0) / 1000, state.get(4, 0) / 1000.0);
	}
	
	
	private Matrix getState(final IVector2 pos, final IVector2 vel)
	{
		return new Matrix(new double[] { pos.x(), pos.y(), 0, vel.x() * 1000, vel.y() * 1000, 0, 0, 0, 0 }, 9);
	}
	
	
	private double simulate(final Matrix initState, final Matrix finalState, final double maxt)
	{
		BallDynamicsModel model = new BallDynamicsModel(acc);
		double dt = 0.016;
		double refTime = 0;
		Matrix state = initState;
		while ((Math.sqrt((state.get(3, 0) * state.get(3, 0)) + (state.get(4, 0) * state.get(4, 0))) > 0)
				&& (refTime <= maxt))
		{
			state = model.dynamics(state, dt, new MotionContext());
			refTime += dt;
			if (refTime > 120)
			{
				log.error("Dynamics simulation run for more than 2min.");
				break;
			}
		}
		finalState.setMatrix(0, 8, 0, 0, state);
		return refTime;
	}
	
	
	/**
	 */
	@Test
	public void testgetPosByTime()
	{
		IVector2 pos = new Vector2(100, 200);
		IVector2 vel = new Vector2(-1, 0.5);
		double time = 1.5;
		IVector2 dest = Geometry.getBallModel().getPosByTime(pos, vel, time);
		double dist = GeoMath.distancePP(pos, dest);
		double ref = Geometry.getBallModel().getDistByTime(vel.getLength2(), time);
		Assert.assertTrue(SumatraMath.isEqual(ref, dist));
	}
	
	
	/**
	 * 
	 */
	@Test
	public void testgetDistByTime()
	{
		double time = 1.6;
		double vel = 2.0;
		Matrix initState = getState(new Vector2(1000, 2000), new Vector2(1, 1).scaleTo(vel));
		Matrix finalState = initState.copy();
		simulate(initState, finalState, time);
		IVector2 initPos = getPosFromMatrix(initState);
		IVector2 finalPos = getPosFromMatrix(finalState);
		double refDist = GeoMath.distancePP(initPos, finalPos);
		
		double dist = Geometry.getBallModel().getDistByTime(vel, time);
		Assert.assertEquals(refDist, dist, 50);
	}
	
	
	/**
	 * 
	 */
	@Test
	public void testgetPosByVel()
	{
		IVector2 currentPos = new Vector2(1000, 2000);
		IVector2 currentVel = new Vector2(0.5f, 1.1);
		double velocity = currentVel.getLength2() - 0.5;
		Matrix initState = getState(currentPos, currentVel);
		BallDynamicsModel model = new BallDynamicsModel(acc);
		double dt = 0.016;
		Matrix state = initState;
		double refTime = 0;
		while ((Math.sqrt((state.get(3, 0) * state.get(3, 0)) + (state.get(4, 0) * state.get(4, 0))) > (velocity * 1000)))
		{
			state = model.dynamics(state, dt, new MotionContext());
			refTime += dt;
			if (refTime > 120)
			{
				log.error("Dynamics simulation run for more than 2min.");
				break;
			}
		}
		IVector2 pos = Geometry.getBallModel().getPosByVel(currentPos, currentVel, velocity);
		Assert.assertEquals(0, GeoMath.distancePP(pos, getPosFromMatrix(state)), 50);
		Assert.assertEquals(velocity, getVelFromMatrix(state).getLength2(), 0.01);
	}
	
	
	/**
	 * 
	 */
	@Test
	public void testgetDistByVel()
	{
		double currentVel = 1.5;
		double velocity = 0.0;
		Matrix initState = getState(new Vector2(0, 2000), new Vector2(currentVel, 0));
		BallDynamicsModel model = new BallDynamicsModel(acc);
		double dt = 0.016;
		Matrix state = initState;
		double refTime = 0;
		while ((Math.sqrt((state.get(3, 0) * state.get(3, 0)) + (state.get(4, 0) * state.get(4, 0))) > (velocity * 1000)))
		{
			state = model.dynamics(state, dt, new MotionContext());
			refTime += dt;
			if (refTime > 120)
			{
				log.error("Dynamics simulation run for more than 2min.");
				break;
			}
		}
		double dist = Geometry.getBallModel().getDistByVel(currentVel, velocity);
		double refDist = getPosFromMatrix(state).x();
		Assert.assertEquals(refDist, dist, 50);
		Assert.assertEquals(velocity, getVelFromMatrix(state).getLength2(), 0.01);
	}
	
	
	/**
	 * 
	 */
	@Test
	public void testgetTimeByDist()
	{
		double currentVel = 1.5;
		double dist = 1000;
		double time = Geometry.getBallModel().getTimeByDist(currentVel, dist);
		
		Matrix initState = getState(new Vector2(0, 2000), new Vector2(currentVel, 0));
		BallDynamicsModel model = new BallDynamicsModel(acc);
		double dt = 0.016;
		Matrix state = initState;
		double refTime = 0;
		while (getPosFromMatrix(state).x() < dist)
		{
			state = model.dynamics(state, dt, new MotionContext());
			refTime += dt;
			if (refTime > 120)
			{
				log.error("Dynamics simulation run for more than 2min.");
				break;
			}
		}
		Assert.assertEquals(refTime, time, dt);
	}
	
	
	/**
	 * 
	 */
	@Test
	public void testgetTimeByVel()
	{
		double currentVel = 1.5;
		double velocity = 1.0;
		double time = Geometry.getBallModel().getTimeByVel(currentVel, velocity);
		
		Matrix initState = getState(new Vector2(0, 2000), new Vector2(currentVel, 0));
		BallDynamicsModel model = new BallDynamicsModel(acc);
		double dt = 0.016;
		Matrix state = initState;
		double refTime = 0;
		while (getVelFromMatrix(state).getLength2() > velocity)
		{
			state = model.dynamics(state, dt, new MotionContext());
			refTime += dt;
			if (refTime > 120)
			{
				log.error("Dynamics simulation run for more than 2min.");
				break;
			}
		}
		Assert.assertEquals(refTime, time, dt);
	}
	
	
	/**
	 * 
	 */
	@Test
	public void testgetVelByDist()
	{
		double currentVel = 1.5;
		double dist = 1000;
		double vel = Geometry.getBallModel().getVelByDist(currentVel, dist);
		
		Matrix initState = getState(new Vector2(0, 2000), new Vector2(currentVel, 0));
		BallDynamicsModel model = new BallDynamicsModel(acc);
		double dt = 0.016;
		Matrix state = initState;
		double time = 0;
		while (getPosFromMatrix(state).getLength2() < dist)
		{
			state = model.dynamics(state, dt, new MotionContext());
			time += dt;
			if (time > 120)
			{
				log.error("Dynamics simulation run for more than 2min.");
				break;
			}
		}
		Assert.assertEquals(getVelFromMatrix(state).getLength2(), vel, 0.1);
	}
	
	
	/**
	 * 
	 */
	@Test
	public void testgetVelByTime()
	{
		double currentVel = 1.5;
		double time = 1.0;
		double vel = Geometry.getBallModel().getVelByTime(currentVel, time);
		
		Matrix initState = getState(new Vector2(0, 2000), new Vector2(currentVel, 0));
		BallDynamicsModel model = new BallDynamicsModel(acc);
		double dt = 0.016;
		double refTime = 0;
		Matrix state = initState;
		while (refTime < time)
		{
			state = model.dynamics(state, dt, new MotionContext());
			refTime += dt;
			if (refTime > 120)
			{
				log.error("Dynamics simulation run for more than 2min.");
				break;
			}
		}
		Assert.assertEquals(getVelFromMatrix(state).getLength2(), vel, 0.1);
	}
	
	
	/**
	 * 
	 */
	@Test
	public void testgetVelForTime()
	{
		double endVel = 1.1;
		double time = 1.5;
		double vel = Geometry.getBallModel().getVelForTime(endVel, time);
		
		Matrix initState = getState(new Vector2(0, 2000), new Vector2(vel, 0));
		BallDynamicsModel model = new BallDynamicsModel(acc);
		double dt = 0.016;
		double refTime = 0;
		Matrix state = initState;
		while (getVelFromMatrix(state).getLength2() > endVel)
		{
			state = model.dynamics(state, dt, new MotionContext());
			refTime += dt;
			if (refTime > 120)
			{
				log.error("Dynamics simulation run for more than 2min.");
				break;
			}
		}
		Assert.assertEquals(time, refTime, dt);
	}
	
	
	/**
	 * 
	 */
	@Test
	public void testgetVelForDist()
	{
		double endVel = 1.1;
		double dist = 2000;
		double vel = Geometry.getBallModel().getVelForDist(dist, endVel);
		
		Matrix initState = getState(new Vector2(0, 2000), new Vector2(vel, 0));
		BallDynamicsModel model = new BallDynamicsModel(acc);
		double dt = 0.016;
		Matrix state = initState;
		double refTime = 0;
		while (getVelFromMatrix(state).x() > endVel)
		{
			state = model.dynamics(state, dt, new MotionContext());
			refTime += dt;
			if (refTime > 120)
			{
				log.error("Dynamics simulation run for more than 2min.");
				break;
			}
		}
		Assert.assertEquals(dist, getPosFromMatrix(state).x(), 50);
	}
	
	
	/**
	 * 
	 */
	@Test
	public void testgetAcc()
	{
	}
}
