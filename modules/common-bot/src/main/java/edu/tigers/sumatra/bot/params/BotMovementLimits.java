package edu.tigers.sumatra.bot.params;

/**
 * Robot movement limitations.
 */
public class BotMovementLimits implements IBotMovementLimits
{
	private double velMax = 0.1;
	private double accMax = 0.1;
	private double brkMax = 0.1;

	private double velMaxW = 0.1;
	private double accMaxW = 0.1;


	/**
	 * Default constructor.
	 */
	public BotMovementLimits()
	{
	}


	/**
	 * Copy constructor.
	 *
	 * @param limits
	 */
	public BotMovementLimits(final IBotMovementLimits limits)
	{
		velMax = limits.getVelMax();
		accMax = limits.getAccMax();
		brkMax = limits.getBrkMax();
		velMaxW = limits.getVelMaxW();
		accMaxW = limits.getAccMaxW();
	}


	@Override
	public double getVelMax()
	{
		return velMax;
	}


	public void setVelMax(final double velMax)
	{
		this.velMax = velMax;
	}


	@Override
	public double getAccMax()
	{
		return accMax;
	}


	public void setAccMax(final double accMax)
	{
		this.accMax = accMax;
	}


	@Override
	public double getBrkMax()
	{
		return brkMax;
	}


	public void setBrkMax(final double brkMax)
	{
		this.brkMax = brkMax;
	}


	@Override
	public double getVelMaxW()
	{
		return velMaxW;
	}


	public void setVelMaxW(final double velMaxW)
	{
		this.velMaxW = velMaxW;
	}


	@Override
	public double getAccMaxW()
	{
		return accMaxW;
	}


	public void setAccMaxW(final double accMaxW)
	{
		this.accMaxW = accMaxW;
	}

}
