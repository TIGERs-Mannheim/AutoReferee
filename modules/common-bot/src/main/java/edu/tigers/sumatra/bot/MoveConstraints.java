/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.bot;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.bot.params.IBotMovementLimits;
import edu.tigers.sumatra.data.collector.IExportable;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Constraints on the movement of robots.
 */
@Persistent
public class MoveConstraints implements IExportable, IMoveConstraints
{
	private double velMax;
	private double accMax;
	private double brkMax;
	private double jerkMax;
	private double velMaxW;
	private double accMaxW;
	private double jerkMaxW;
	private double velMaxFast;
	private double accMaxFast;

	private boolean fastMove;
	private IVector2 primaryDirection = Vector2f.ZERO_VECTOR;


	/**
	 * Create a dummy instance
	 */
	public MoveConstraints()
	{
	}


	public MoveConstraints(MoveConstraints mc)
	{
		velMax = mc.velMax;
		accMax = mc.accMax;
		brkMax = mc.brkMax;
		jerkMax = mc.jerkMax;
		velMaxW = mc.velMaxW;
		accMaxW = mc.accMaxW;
		jerkMaxW = mc.jerkMaxW;
		velMaxFast = mc.velMaxFast;
		accMaxFast = mc.accMaxFast;
		fastMove = mc.fastMove;
		primaryDirection = mc.primaryDirection;
	}


	/**
	 * Create move constraints from bot individual movement limits.
	 *
	 * @param moveLimits
	 */
	public MoveConstraints(final IBotMovementLimits moveLimits)
	{
		resetLimits(moveLimits);
	}


	public void resetLimits(final IBotMovementLimits moveLimits)
	{
		velMax = moveLimits.getVelMax();
		accMax = moveLimits.getAccMax();
		brkMax = moveLimits.getBrkMax();
		jerkMax = moveLimits.getJerkMax();
		velMaxW = moveLimits.getVelMaxW();
		accMaxW = moveLimits.getAccMaxW();
		jerkMaxW = moveLimits.getJerkMaxW();
		velMaxFast = moveLimits.getVelMaxFast();
		accMaxFast = moveLimits.getAccMaxFast();
	}


	public void limit(final IBotMovementLimits movementLimits)
	{
		velMax = Math.min(velMax, movementLimits.getVelMax());
		accMax = Math.min(accMax, movementLimits.getAccMax());
		brkMax = Math.min(brkMax, movementLimits.getBrkMax());
		jerkMax = Math.min(jerkMax, movementLimits.getJerkMax());
		velMaxW = Math.min(velMaxW, movementLimits.getVelMaxW());
		accMaxW = Math.min(accMaxW, movementLimits.getAccMaxW());
		jerkMaxW = Math.min(jerkMaxW, movementLimits.getJerkMaxW());
		velMaxFast = Math.min(velMaxFast, movementLimits.getVelMaxFast());
		accMaxFast = Math.min(accMaxFast, movementLimits.getAccMaxFast());
	}


	@Override
	public String toString()
	{
		return "MoveConstraints{" +
				"velMax=" + velMax +
				", accMax=" + accMax +
				", brkMax=" + brkMax +
				", jerkMax=" + jerkMax +
				", velMaxW=" + velMaxW +
				", accMaxW=" + accMaxW +
				", jerkMaxW=" + jerkMaxW +
				", velMaxFast=" + velMaxFast +
				", accMaxFast=" + accMaxFast +
				", fastMove=" + fastMove +
				", primaryDirection=" + primaryDirection +
				'}';
	}


	@Override
	public List<Number> getNumberList()
	{
		List<Number> nbrs = new ArrayList<>();
		nbrs.add(velMax);
		nbrs.add(accMax);
		nbrs.add(jerkMax);
		nbrs.add(velMaxW);
		nbrs.add(accMaxW);
		nbrs.add(jerkMaxW);
		nbrs.add(velMaxFast);
		nbrs.add(accMaxFast);
		nbrs.add(fastMove ? 1 : 0);
		nbrs.add(primaryDirection.x());
		nbrs.add(primaryDirection.y());
		return nbrs;
	}


	@Override
	public List<String> getHeaders()
	{
		return Arrays.asList("velMax", "accMax", "jerkMax", "velMaxW", "accMaxW", "jerkMaxW", "velMaxFast", "accMaxFast",
				"fastMove", "primaryDirectionX", "primaryDirectionY");
	}


	@Override
	public double getVelMax()
	{
		if (fastMove)
		{
			return velMaxFast;
		}
		return velMax;
	}


	public void setVelMax(final double velMax)
	{
		assert velMax >= 0 : "vel: " + velMax;
		this.velMax = velMax;
	}


	public void setVelMaxFast(final double velMaxFast)
	{
		assert velMax >= 0 : "vel: " + velMaxFast;
		this.velMaxFast = velMaxFast;
	}


	@Override
	public double getVelMaxW()
	{
		return velMaxW;
	}


	public void setVelMaxW(final double velMaxW)
	{
		assert velMaxW >= 0;
		this.velMaxW = velMaxW;
	}


	@Override
	public double getAccMax()
	{
		if (fastMove)
		{
			return accMaxFast;
		}
		return accMax;
	}


	/**
	 * @param accMax the accMax to set
	 */
	public void setAccMax(final double accMax)
	{
		assert accMax >= 0;
		this.accMax = accMax;
	}


	@Override
	public double getBrkMax()
	{
		return brkMax;
	}


	@Override
	public double getAccMaxW()
	{
		return accMaxW;
	}


	public void setAccMaxW(final double accMaxW)
	{
		assert accMaxW >= 0;
		this.accMaxW = accMaxW;
	}


	@Override
	public double getJerkMax()
	{
		return jerkMax;
	}


	public void setJerkMax(final double jerkMax)
	{
		assert jerkMax >= 0;
		this.jerkMax = jerkMax;
	}


	@Override
	public double getJerkMaxW()
	{
		return jerkMaxW;
	}


	public void setJerkMaxW(final double jerkMaxW)
	{
		assert jerkMaxW >= 0;
		this.jerkMaxW = jerkMaxW;
	}


	/**
	 * Activate the fastPos move skill.
	 *
	 * @param fastMove activate fast move skill
	 */
	public void setFastMove(final boolean fastMove)
	{
		this.fastMove = fastMove;
	}


	@Override
	public boolean isFastMove()
	{
		return fastMove;
	}


	public void setPrimaryDirection(final IVector2 primaryDirection)
	{
		assert primaryDirection != null;
		this.primaryDirection = primaryDirection;
	}


	@Override
	public IVector2 getPrimaryDirection()
	{
		return primaryDirection;
	}
}
