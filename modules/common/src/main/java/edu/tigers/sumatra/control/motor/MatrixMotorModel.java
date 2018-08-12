/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.control.motor;

import org.apache.commons.math3.analysis.function.Cos;
import org.apache.commons.math3.analysis.function.Sin;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;

import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.IVectorN;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.math.vector.VectorN;


/**
 */
public class MatrixMotorModel extends AMotorModel
{
	private static final double	BOT_RADIUS		= 0.076;
	private static final double	WHEEL_RADIUS	= 0.025;
	
	private final RealMatrix d;
	private final RealMatrix dInv;
	
	
	/**
	 * Default constructor
	 */
	public MatrixMotorModel()
	{
		this(30, 45);
	}
	
	
	/**
	 * @param d
	 */
	public MatrixMotorModel(final RealMatrix d)
	{
		this.d = d;
		dInv = new SingularValueDecomposition(d).getSolver().getInverse();
	}
	
	
	/**
	 * Create motor model with given angles.
	 *
	 * @param frontAngleDeg
	 * @param backAngleDeg
	 */
	public MatrixMotorModel(final double frontAngleDeg, final double backAngleDeg)
	{
		// convert to radian
		final double frontAngleRad = frontAngleDeg * Math.PI / 180.0;
		final double backAngleRad = backAngleDeg * Math.PI / 180.0;
		
		// construct angle vector
		RealVector theta = new ArrayRealVector(
				new double[] { frontAngleRad, Math.PI - frontAngleRad, Math.PI + backAngleRad,
						(2 * Math.PI) - backAngleRad });
		
		// construct matrix for conversion from XYW to M1..M4
		d = new Array2DRowRealMatrix(4, 3);
		d.setColumnVector(0, theta.map(new Sin()).mapMultiplyToSelf(-1.0));
		d.setColumnVector(1, theta.map(new Cos()));
		d.setColumnVector(2, new ArrayRealVector(4, BOT_RADIUS));
		dInv = new SingularValueDecomposition(d).getSolver().getInverse();
	}
	
	
	/**
	 * @param dw
	 * @return
	 */
	public static MatrixMotorModel fromMatrixWithWheelVel(final RealMatrix dw)
	{
		RealMatrix d = dw.copy();
		for (int i = 0; i < 4; i++)
		{
			for (int j = 0; j < 3; j++)
			{
				d.multiplyEntry(i, j, WHEEL_RADIUS);
			}
		}
		return new MatrixMotorModel(d);
	}
	
	
	@Override
	protected VectorN getWheelSpeedInternal(final IVector3 targetVel)
	{
		RealMatrix xyw = new Array2DRowRealMatrix(targetVel.toArray());
		RealMatrix speedOverGround = d.multiply(xyw);
		RealVector wheelSpeed = speedOverGround.getColumnVector(0).mapMultiply(1.0 / WHEEL_RADIUS);
		return VectorN.fromReal(wheelSpeed);
	}
	
	
	@Override
	protected Vector3 getXywSpeedInternal(final IVectorN wheelSpeed)
	{
		RealMatrix wheel = new Array2DRowRealMatrix(wheelSpeed.toArray());
		RealVector result = dInv.multiply(wheel).getColumnVector(0).mapMultiply(WHEEL_RADIUS);
		return Vector3.fromXYZ(result.getEntry(0),
				result.getEntry(1),
				result.getEntry(2));
	}
	
	
	@Override
	public EMotorModel getType()
	{
		return EMotorModel.MATRIX;
	}
}
