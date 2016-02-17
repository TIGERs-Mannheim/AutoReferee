package edu.tigers.sumatra.wp.kalman.motionModels;

import Jama.Matrix;
import edu.tigers.sumatra.wp.data.MotionContext;
import edu.tigers.sumatra.wp.kalman.data.AMotionResult;
import edu.tigers.sumatra.wp.kalman.data.AWPCamObject;
import edu.tigers.sumatra.wp.kalman.data.IControl;


/**
 *
 */
public interface IMotionModel
{
	/**
	 * @param state
	 * @param control
	 * @param dt
	 * @param context
	 * @return
	 */
	Matrix dynamics(Matrix state, Matrix control, double dt, MotionContext context);
	
	
	/**
	 * @param state
	 * @param control
	 * @return
	 */
	Matrix sample(Matrix state, Matrix control);
	
	
	/**
	 * @param stateNew
	 * @param stateOld
	 * @param control
	 * @return
	 */
	double transitionProbability(Matrix stateNew, Matrix stateOld, Matrix control);
	
	
	/**
	 * @param state
	 * @param measurement
	 * @param dt
	 * @return
	 */
	double measurementProbability(Matrix state, Matrix measurement, double dt);
	
	
	/**
	 * @param state
	 * @param dt
	 * @return
	 */
	Matrix getDynamicsJacobianWRTstate(Matrix state, double dt);
	
	
	/**
	 * @param state
	 * @param dt
	 * @return
	 */
	Matrix getDynamicsJacobianWRTnoise(Matrix state, double dt);
	
	
	/**
	 * @param state
	 * @param dt
	 * @return
	 */
	Matrix getDynamicsCovariance(Matrix state, double dt);
	
	
	/**
	 * @param id
	 * @param state
	 * @param onCam
	 * @return
	 */
	AMotionResult generateMotionResult(int id, Matrix state, boolean onCam);
	
	
	/**
	 * @param observation
	 * @param state
	 * @return
	 */
	Matrix generateMeasurementMatrix(AWPCamObject observation, Matrix state);
	
	
	/**
	 * @param measurement
	 * @param control
	 * @return
	 */
	Matrix generateStateMatrix(Matrix measurement, Matrix control);
	
	
	/**
	 * @param control
	 * @param state
	 * @return
	 */
	Matrix updateStateOnNewControl(IControl control, Matrix state);
	
	
	/**
	 * @param control
	 * @param covariance
	 * @return
	 */
	Matrix updateCovarianceOnNewControl(IControl control, Matrix covariance);
	
	
	/**
	 * @param control
	 * @param state
	 * @return
	 */
	Matrix generateControlMatrix(IControl control, Matrix state);
	
	
	/**
	 * @param state
	 * @return
	 */
	Matrix generateCovarianceMatrix(Matrix state);
	
	
	/**
	 * @param observation
	 * @return
	 */
	int extraxtObjectID(AWPCamObject observation);
	
	
	/**
	 * @param state
	 * @return
	 */
	Matrix measurementDynamics(Matrix state);
	
	
	/**
	 * @param state
	 * @return
	 */
	Matrix getMeasurementJacobianWRTstate(Matrix state);
	
	
	/**
	 * @param state
	 * @return
	 */
	Matrix getMeasurementJacobianWRTnoise(Matrix state);
	
	
	/**
	 * @param measurement
	 * @return
	 */
	Matrix getMeasurementCovariance(Matrix measurement);
	
	
	/**
	 * @param state
	 * @return
	 */
	Matrix getStateOnNoObservation(Matrix state);
	
	
	/**
	 * @param covariance
	 * @return
	 */
	Matrix getCovarianceOnNoObservation(Matrix covariance);
	
	
	/**
	 * @param control
	 * @return
	 */
	Matrix getControlOnNoObservation(Matrix control);
	
	
	/**
	 * @param state
	 * @param preState
	 * @return
	 */
	Matrix statePostProcessing(Matrix state, Matrix preState);
	
	
	/**
	 * @param measurement
	 * @param matrix
	 * @param dt
	 */
	default void newMeasurement(final Matrix measurement, final Matrix matrix, final double dt)
	{
	}
}
