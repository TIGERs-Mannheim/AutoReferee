/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.wp.ball.trajectory.flat;

import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.planarcurve.PlanarCurve;
import edu.tigers.sumatra.planarcurve.PlanarCurveSegment;
import edu.tigers.sumatra.vision.data.BallTrajectoryState;
import edu.tigers.sumatra.wp.ball.trajectory.ABallTrajectory;
import org.apache.commons.lang.Validate;

import java.util.ArrayList;
import java.util.List;


/**
 * Ball trajectory for straight kicks with two phases, separated by a dynamically calculated switch velocity.
 */
public class TwoPhaseDynamicVelBallTrajectory extends ABallTrajectory
{
	private final IVector2 kickPos;
	private final IVector2 kickVel;
	private final double tSwitch;
	private final double tKickToNow;
	private final TwoPhaseDynamicVelParameters params;

	private final IVector2 posSwitch;
	private final IVector2 velSwitch;


	private TwoPhaseDynamicVelBallTrajectory(
			final IVector2 kickPos,
			final IVector2 kickVel,
			final double tSwitch,
			final double tKickToNow,
			final TwoPhaseDynamicVelParameters params)
	{
		this.kickPos = kickPos;
		this.kickVel = kickVel;
		this.tSwitch = tSwitch;
		this.tKickToNow = tKickToNow;
		this.params = params;

		Vector2 intAcc = kickVel.getXYVector().scaleToNew(params.getAccSlide() * tSwitch);
		velSwitch = kickVel.getXYVector().addNew(intAcc);
		posSwitch = kickVel.getXYVector().multiplyNew(tSwitch)
				.add(kickPos)
				.add(intAcc.multiply(0.5 * tSwitch));
	}


	/**
	 * Create a straight ball trajectory from a ball where kick position/velocity is known.
	 *
	 * @param kickPos position in [mm]
	 * @param kickVel velocity in [mm/s]
	 * @param params
	 * @return
	 */
	public static TwoPhaseDynamicVelBallTrajectory fromKick(final IVector2 kickPos, final IVector2 kickVel,
			final TwoPhaseDynamicVelParameters params)
	{
		double tSwitch = (kickVel.getLength2() * (params.getKSwitch() - 1)) / params.getAccSlide();

		return new TwoPhaseDynamicVelBallTrajectory(kickPos, kickVel, tSwitch, 0, params);
	}


	/**
	 * @param posNow
	 * @param velNow
	 * @param vSwitch
	 * @param params
	 * @return
	 */
	public static TwoPhaseDynamicVelBallTrajectory fromState(final IVector2 posNow, final IVector2 velNow,
			final double vSwitch, final TwoPhaseDynamicVelParameters params)
	{
		double tSwitch;
		double tKickToNow;
		IVector2 kickPos;
		IVector2 kickVel;

		if (velNow.getLength2() > vSwitch)
		{
			// ball is still in sliding phase
			double timeToSwitch = -(velNow.getLength2() - vSwitch) / params.getAccSlide();
			IVector2 acc = velNow.normalizeNew().multiply(params.getAccSlide());
			IVector2 velSwitch = velNow.addNew(acc.multiplyNew(timeToSwitch));

			kickVel = velSwitch.multiplyNew(1.0 / params.getKSwitch());
			double timeToKick = (kickVel.getLength2() - velNow.getLength2()) / params.getAccSlide();
			kickPos = posNow.addNew(velNow.multiplyNew(timeToKick)).add(acc.multiplyNew(0.5 * timeToKick * timeToKick));
			tKickToNow = -timeToKick;
		} else
		{
			// ball is in rolling phase
			double timeToSwitch = (vSwitch - velNow.getLength2()) / params.getAccRoll();
			IVector2 acc = velNow.normalizeNew().multiply(params.getAccRoll());
			IVector2 posSwitch = posNow.addNew(velNow.multiplyNew(timeToSwitch))
					.add(acc.multiplyNew(0.5 * timeToSwitch * timeToSwitch));
			IVector2 velSwitch = velNow.addNew(acc.multiplyNew(timeToSwitch));

			acc = velNow.normalizeNew().multiply(params.getAccSlide());
			kickVel = velSwitch.multiplyNew(1.0 / params.getKSwitch());
			double tSlide = (kickVel.getLength2() - velSwitch.getLength2()) / params.getAccSlide(); // negative
			kickPos = posSwitch.addNew(velSwitch.multiplyNew(tSlide)).add(acc.multiplyNew(0.5 * tSlide * tSlide));
			tKickToNow = -(timeToSwitch + tSlide);
		}

		tSwitch = (kickVel.getLength2() * (params.getKSwitch() - 1)) / params.getAccSlide();

		return new TwoPhaseDynamicVelBallTrajectory(kickPos, kickVel, tSwitch, tKickToNow, params);
	}


	@Override
	public ABallTrajectory mirrored()
	{
		IVector2 vel = kickVel.getXYVector().multiplyNew(-1);
		IVector2 pos = kickPos.getXYVector().multiplyNew(-1);

		return TwoPhaseDynamicVelBallTrajectory.fromKick(pos, vel, params);
	}


	@Override
	public BallTrajectoryState getMilliStateAtTime(final double time)
	{
		if (time < 0)
		{
			return BallTrajectoryState.builder()
					.withPos(kickPos.getXYZVector())
					.withVel(kickVel.getXYZVector())
					.withAcc(Vector3.zero())
					.withVSwitchToRoll(kickVel.getLength2())
					.withChipped(false)
					.build();
		}

		if (time < tSwitch)
		{
			var accNow = kickVel.getXYVector().scaleToNew(params.getAccSlide());
			var intAcc = accNow.multiplyNew(time);
			var velNow = kickVel.getXYVector().addNew(intAcc);
			var posNow = kickVel.getXYVector()
					.multiplyNew(time)
					.add(intAcc.multiplyNew(0.5 * time))
					.add(kickPos);

			return BallTrajectoryState.builder()
					.withPos(posNow.getXYZVector())
					.withVel(velNow.getXYZVector())
					.withAcc(accNow.getXYZVector())
					.withVSwitchToRoll(velSwitch.getLength2())
					.withChipped(false)
					.build();
		}

		double t2 = time - tSwitch;
		if (time > getTimeAtRest())
		{
			t2 = getTimeAtRest() - tSwitch;
		}

		IVector2 acc = kickVel.getXYVector().scaleToNew(params.getAccRoll());
		Vector2 intAcc = acc.multiplyNew(t2);
		IVector velNow = velSwitch.addNew(intAcc);
		IVector posNow = velSwitch.multiplyNew(t2)
				.add(intAcc.multiplyNew(0.5 * t2))
				.add(posSwitch);

		return BallTrajectoryState.builder()
				.withPos(posNow.getXYZVector())
				.withVel(velNow.getXYZVector())
				.withAcc(acc.getXYZVector())
				.withVSwitchToRoll(velSwitch.getLength2())
				.withChipped(false)
				.build();
	}


	@Override
	public PlanarCurve getPlanarCurve()
	{
		List<PlanarCurveSegment> segments = new ArrayList<>();
		BallTrajectoryState state = getMilliStateAtTime(tKickToNow);

		double tRest = getTimeAtRest();

		if (tKickToNow > tRest)
		{
			segments.add(PlanarCurveSegment.fromPoint(state.getPos().getXYVector(), 0, 1.0));
			return new PlanarCurve(segments);
		}

		if (tKickToNow < tSwitch)
		{
			// add sliding phase
			PlanarCurveSegment slide = PlanarCurveSegment.fromSecondOrder(state.getPos().getXYVector(),
					state.getVel().getXYVector(),
					state.getAcc().getXYVector(),
					0, tSwitch - tKickToNow);
			segments.add(slide);
		}

		PlanarCurveSegment roll = PlanarCurveSegment.fromSecondOrder(posSwitch.getXYVector(),
				velSwitch.getXYVector(),
				kickVel.getXYVector().normalizeNew().multiply(params.getAccRoll()),
				tSwitch - tKickToNow, tRest - tKickToNow);
		segments.add(roll);

		return new PlanarCurve(segments);
	}


	@Override
	public double getTimeAtRest()
	{
		double tStop = -velSwitch.getLength2() / params.getAccRoll();
		return tSwitch + tStop;
	}


	@Override
	protected double getTimeByDistanceInMillimeters(final double distance)
	{
		double distToSwitch = ((kickVel.getLength2() + velSwitch.getLength2()) / 2.0) * tSwitch;

		if (distance < distToSwitch)
		{
			// queried distance is in sliding phase
			double v = kickVel.getLength2();
			double a = params.getAccSlide();
			return (SumatraMath.sqrt((v * v) + (2.0 * a * distance)) - v) / a;
		}

		double v = velSwitch.getLength2();
		double a = params.getAccRoll();
		double tRoll = -v / a;
		double distRoll = (v / 2.0) * tRoll;

		if (distance > (distToSwitch + distRoll))
		{
			// queried distance is beyond total distance
			return Double.POSITIVE_INFINITY;
		}

		// distance is in rolling phase
		double p = distance - distToSwitch;
		double timeToDist = ((SumatraMath.sqrt((v * v) + (2.0 * a * p) + 1e-6) - v) / a) + 1e-6;
		if (timeToDist < 1e-3)
		{
			timeToDist = 0.0; // numerical issues...
		}
		assert timeToDist >= 0 : timeToDist;

		return tSwitch + timeToDist;
	}


	@Override
	protected double getTimeByVelocityInMillimetersPerSec(final double velocity)
	{
		if (velocity > kickVel.getLength2())
		{
			return 0;
		}

		if (velocity > velSwitch.getLength2())
		{
			// requested velocity is during sliding phase
			double tToVel = -(kickVel.getLength2() - velocity) / params.getAccSlide();
			Validate.isTrue(tToVel >= 0);

			return tToVel;
		}

		// requested velocity is during rolling phase
		double tToVel = -(velSwitch.getLength2() - velocity) / params.getAccRoll();
		Validate.isTrue(tToVel >= 0);

		return tSwitch + tToVel;
	}


	@Override
	public IVector2 getKickPos()
	{
		return kickPos;
	}


	@Override
	public double getTKickToNow()
	{
		return tKickToNow;
	}
}
