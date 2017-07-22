/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.vision.data;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.Validate;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.BotMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector3;


/**
 * @author AndreR <andre@ryll.cc>
 */
public class RobotCollisionShape
{
	private IVector2		pos;
	private double			orient;
	private final double	radius;
	private final double	center2Dribbler;
	
	
	/**
	 * @param pos
	 * @param orient
	 * @param radius
	 * @param center2Dribbler
	 */
	public RobotCollisionShape(final IVector2 pos, final double orient, final double radius,
			final double center2Dribbler)
	{
		this.pos = pos;
		this.orient = orient;
		this.radius = radius;
		this.center2Dribbler = center2Dribbler;
	}
	
	
	/**
	 * @param ballPos Position [mm]
	 * @param ballVel Velocity [mm/s]
	 * @return
	 */
	public CollisionResult getCollision(final IVector2 ballPos, final IVector2 ballVel)
	{
		final double ballRadius = Geometry.getBallRadius();
		IVector2 ballVelUsed = ballVel;
		
		// check if outside of bot anyway (bounding circle)
		if (ballPos.distanceTo(pos) > (radius + ballRadius))
		{
			return new CollisionResult(ballPos, ECollisionLocation.NONE);
		}
		
		if (ballVelUsed.getLength2() < 10)
		{
			// ball very slow, project outside and generate new inbound vel
			ICircle botCircle = Circle.createCircle(pos, radius + ballRadius);
			
			IVector2 outside = botCircle.nearestPointOutside(ballPos);
			ballVelUsed = ballPos.subtractNew(outside);
		}
		
		Line frontLine = BotMath.getDribblerFrontLine(Vector3.from2d(pos, orient), radius + ballRadius,
				center2Dribbler + ballRadius);
		
		Line ballVelLine = Line.fromDirection(ballPos, ballVelUsed.multiplyNew(-1.0).scaleTo(radius * 5.0));
		
		Optional<IVector2> frontIntersect = frontLine.intersectionOfSegments(ballVelLine);
		
		if (frontIntersect.isPresent())
		{
			return new CollisionResult(frontIntersect.get(), ECollisionLocation.FRONT);
		}
		
		ICircle botCircle = Circle.createCircle(pos, radius + ballRadius);
		List<IVector2> intersect = botCircle.lineSegmentIntersections(ballVelLine);
		
		if (!intersect.isEmpty())
		{
			return new CollisionResult(intersect.get(0), ECollisionLocation.CIRCLE);
		}
		
		// this should actually not happen
		Validate.isTrue(false);
		
		return new CollisionResult(ballPos, ECollisionLocation.NONE);
	}
	
	/**
	 * Collision result.
	 * 
	 * @author AndreR <andre@ryll.cc>
	 */
	public static class CollisionResult
	{
		private final IVector2				outsidePos;
		private final ECollisionLocation	location;
		
		
		/**
		 * @param outsidePos
		 * @param location
		 */
		public CollisionResult(final IVector2 outsidePos, final ECollisionLocation location)
		{
			super();
			this.outsidePos = outsidePos;
			this.location = location;
		}
		
		
		public IVector2 getOutsidePos()
		{
			return outsidePos;
		}
		
		
		public ECollisionLocation getLocation()
		{
			return location;
		}
	}
	
	/**
	 * Collision location.
	 * 
	 * @author AndreR <andre@ryll.cc>
	 */
	public enum ECollisionLocation
	{
		NONE,
		FRONT,
		CIRCLE
	}
}
