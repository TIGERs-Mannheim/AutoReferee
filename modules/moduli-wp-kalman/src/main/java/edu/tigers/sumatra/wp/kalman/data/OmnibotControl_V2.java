package edu.tigers.sumatra.wp.kalman.data;

/**
 */
public class OmnibotControl_V2 implements IControl
{
	/** */
	public final double	vt;
	/** */
	public final double	vo;
	/** positive is clockwise */
	public final double	omega;
	/** positive is clockwise */
	public final double	eta;
	
	
	/**
	 * @param vt
	 * @param vo
	 * @param omega
	 * @param eta
	 */
	public OmnibotControl_V2(final double vt, final double vo, final double omega, final double eta)
	{
		this.vt = vt;
		this.vo = vo;
		this.omega = omega;
		this.eta = eta;
	}
}
