/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 6, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.wp.ball;

import edu.tigers.sumatra.math.AVector2;
import edu.tigers.sumatra.math.ILine;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.math.Vector3;
import edu.tigers.sumatra.wp.ball.collision.BotCollision;
import edu.tigers.sumatra.wp.ball.collision.BotDribbleImpulse;
import edu.tigers.sumatra.wp.ball.collision.BotKickImpuls;
import edu.tigers.sumatra.wp.ball.collision.CollisionHandler;
import edu.tigers.sumatra.wp.ball.collision.ICollisionState;
import edu.tigers.sumatra.wp.ball.collision.LineCollision;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.Goal;
import edu.tigers.sumatra.wp.data.MotionContext;
import edu.tigers.sumatra.wp.data.MotionContext.BotInfo;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BallCollisionModel implements IBallCollisionModel
{
	private boolean	complexCollision	= false;
	
	
	/**
	 * 
	 */
	public BallCollisionModel()
	{
	}
	
	
	/**
	 * @param complexCollision
	 */
	public BallCollisionModel(final boolean complexCollision)
	{
		this.complexCollision = complexCollision;
	}
	
	
	/**
	 * @param state
	 * @param newState
	 * @param dt
	 * @param context
	 * @return
	 */
	@Override
	public ICollisionState processCollision(final IState state, final IState newState, final double dt,
			final MotionContext context)
	{
		CollisionHandler ch = new CollisionHandler(complexCollision);
		
		addCollisionObjects(ch, context);
		
		ICollisionState nextState = ch.process(state, newState);
		
		return nextState;
	}
	
	
	protected void addCollisionObjects(final CollisionHandler ch, final MotionContext context)
	{
		createGoalCollisionObjects(Geometry.getGoalOur(), ch);
		createGoalCollisionObjects(Geometry.getGoalTheir(), ch);
		
		context.getBots().values().stream()
				.map(info -> new BotCollision(info.getPos(), info.getVel(), info.getCenter2DribblerDist()))
				.forEach(bc -> ch.addObject(bc));
	}
	
	
	protected void createGoalCollisionObjects(final Goal goal, final CollisionHandler ch)
	{
		int sign = goal.getGoalCenter().x() < 0 ? -1 : 1;
		double depth = Geometry.getGoalDepth() * sign;
		double radius = Geometry.getBallRadius();
		double sRadius = radius * sign;
		IVector2 depthV = new Vector2(depth, 0);
		
		IVector2 leftPost = goal.getGoalPostLeft();
		IVector2 leftBackPost = leftPost.addNew(depthV);
		IVector2 rightPost = goal.getGoalPostRight();
		IVector2 rightBackPost = rightPost.addNew(depthV);
		
		/*
		 * Outer side of the goal
		 */
		IVector2 leftOuterPost = leftPost.addNew(new Vector2(-sRadius, sRadius));
		IVector2 leftOuterBackPost = leftBackPost.addNew(new Vector2(radius, sRadius));
		IVector2 rightOuterPost = rightPost.addNew(new Vector2(-sRadius, -sRadius));
		IVector2 rightOuterBackPost = rightBackPost.addNew(new Vector2(radius, -sRadius));
		
		ILine leftOuterWall = Line.newLine(leftOuterPost, leftOuterBackPost);
		ILine rightOuterWall = Line.newLine(rightOuterPost, rightOuterBackPost);
		ILine backOuterWall = Line.newLine(leftOuterBackPost, rightOuterBackPost);
		
		ch.addObject(new LineCollision(backOuterWall, AVector2.ZERO_VECTOR, new Vector2(sRadius, 0)));
		ch.addObject(new LineCollision(leftOuterWall, AVector2.ZERO_VECTOR, new Vector2(0, sRadius)));
		ch.addObject(new LineCollision(rightOuterWall, AVector2.ZERO_VECTOR, new Vector2(0, -sRadius)));
		
		/*
		 * Inner side of the goal
		 */
		IVector2 leftInnerPost = leftPost.addNew(new Vector2(-sRadius, -sRadius));
		IVector2 leftInnerBackPost = leftBackPost.addNew(new Vector2(-radius, -sRadius));
		IVector2 rightInnerPost = rightPost.addNew(new Vector2(-sRadius, sRadius));
		IVector2 rightInnerBackPost = rightBackPost.addNew(new Vector2(-radius, sRadius));
		
		ILine leftInnerWall = Line.newLine(leftInnerPost, leftInnerBackPost);
		ILine rightInnerWall = Line.newLine(rightInnerPost, rightInnerBackPost);
		ILine backInnerWall = Line.newLine(leftInnerBackPost, rightInnerBackPost);
		
		ch.addObject(new LineCollision(backInnerWall, AVector2.ZERO_VECTOR, new Vector2(-sRadius, 0)));
		ch.addObject(new LineCollision(leftInnerWall, AVector2.ZERO_VECTOR, new Vector2(0, -sRadius)));
		ch.addObject(new LineCollision(rightInnerWall, AVector2.ZERO_VECTOR, new Vector2(0, sRadius)));
		
		/*
		 * Right and left front side
		 */
		ILine frontLeftWall = Line.newLine(leftOuterPost, leftInnerPost);
		ILine frontRightWall = Line.newLine(rightInnerPost, rightOuterPost);
		
		ch.addObject(new LineCollision(frontLeftWall, AVector2.ZERO_VECTOR, new Vector2(-sRadius, 0)));
		ch.addObject(new LineCollision(frontRightWall, AVector2.ZERO_VECTOR, new Vector2(-sRadius, 0)));
	}
	
	
	@Override
	public IVector3 getImpulse(final ICollisionState state, final MotionContext context)
	{
		CollisionHandler ch = new CollisionHandler(complexCollision);
		
		addImpulseObjects(ch, context);
		
		return ch.getImpulse(state);
	}
	
	
	@Override
	public IVector3 getTorqueAcc(final IState state, final MotionContext context)
	{
		CollisionHandler ch = new CollisionHandler(complexCollision);
		
		addTorqueObjects(ch, context);
		
		return ch.getTorque(state);
	}
	
	
	protected void addImpulseObjects(final CollisionHandler ch, final MotionContext context)
	{
		for (BotInfo info : context.getBots().values())
		{
			if (info.getKickSpeed() > 0)
			{
				Vector3 kickVel = new Vector3();
				kickVel.set(new Vector2(info.getPos().z()).scaleTo(info.getKickSpeed()), 0);
				if (info.isChip())
				{
					kickVel.set(2, info.getKickSpeed());
				}
				ch.addImpulseObject(new BotKickImpuls(info.getPos(), info.getCenter2DribblerDist(), kickVel));
			}
		}
	}
	
	
	protected void addTorqueObjects(final CollisionHandler ch, final MotionContext context)
	{
		for (BotInfo info : context.getBots().values())
		{
			if (info.getDribbleRpm() > 0)
			{
				ch.addImpulseObject(
						new BotDribbleImpulse(info.getPos(), info.getCenter2DribblerDist()));
			}
		}
	}
}
