/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.drawable;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.math.vector.IVector;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.trajectory.ITrajectory;

import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;


/**
 * Drawable trajectory path. Uses sampled points on a bang bang trajectory.
 */
@Persistent
public class DrawableTrajectoryPath extends ADrawableWithStroke
{
	private static final double PRECISION = 0.1;
	private static final double STEP_SIZE = 0.2;

	private final List<IVector2> points = new ArrayList<>();


	@SuppressWarnings("unused")
	private DrawableTrajectoryPath()
	{
	}


	public DrawableTrajectoryPath(ITrajectory<? extends IVector> trajXY)
	{
		IVector2 vLast = null;

		IVector2 first = trajXY.getPositionMM(0).getXYVector();
		IVector2 last = trajXY.getPositionMM(trajXY.getTotalTime()).getXYVector();
		points.add(first);

		for (double t = STEP_SIZE; t < (trajXY.getTotalTime() - STEP_SIZE); t += STEP_SIZE)
		{
			IVector2 pos = trajXY.getPositionMM(t).getXYVector();
			IVector2 vel = trajXY.getVelocity(t).getXYVector();
			if (shouldAddPoint(vLast, vel))
			{
				points.add(pos);
				vLast = vel;
			}
		}
		if (!first.equals(last))
		{
			points.add(last);
		}
	}


	private boolean shouldAddPoint(IVector2 vLast, IVector2 vCur)
	{
		if (vLast == null)
		{
			// first point
			return true;
		}

		var vDiff = vCur.angleToAbs(vLast);
		if (vDiff.isEmpty())
		{
			// vCur or vLast is zero, this only happens at the beginning or end of the trajectory
			return true;
		}
		return vDiff.get() > PRECISION;
	}


	@Override
	public void paintShape(final Graphics2D g, final IDrawableTool tool, final boolean invert)
	{
		super.paintShape(g, tool, invert);

		final GeneralPath drawPath = new GeneralPath();
		IVector2 pLast = points.get(0);
		IVector2 posTrans = tool.transformToGuiCoordinates(pLast, invert);
		drawPath.moveTo(posTrans.x(), posTrans.y());

		for (int i = 1; i < points.size(); i++)
		{
			IVector2 pos = points.get(i);
			posTrans = tool.transformToGuiCoordinates(pos, invert);
			drawPath.lineTo(posTrans.x(), posTrans.y());

			if (VectorMath.distancePP(pLast, pos) > STEP_SIZE)
			{
				pLast = pos;
			}
		}
		g.draw(drawPath);
	}
}
