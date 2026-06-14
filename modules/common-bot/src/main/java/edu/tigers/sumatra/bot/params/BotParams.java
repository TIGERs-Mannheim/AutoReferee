package edu.tigers.sumatra.bot.params;

/**
 * Data holder for all parameters of a robot.
 * Includes movement limits and physical properties.
 */
public class BotParams implements IBotParams
{
	private BotMovementLimits movementLimits = BotMovementLimits.ZERO;
	private final BotDimensions dimensions = new BotDimensions();
	private final BotKickerSpecs kickerSpecs = new BotKickerSpecs();
	private final BotDribblerSpecs dribblerSpecs = new BotDribblerSpecs();


	@Override
	public BotMovementLimits getMovementLimits()
	{
		return movementLimits;
	}


	public void setMovementLimits(final BotMovementLimits movementLimits)
	{
		this.movementLimits = movementLimits;
	}


	@Override
	public IBotDimensions getDimensions()
	{
		return dimensions;
	}


	@Override
	public IBotKickerSpecs getKickerSpecs()
	{
		return kickerSpecs;
	}


	@Override
	public BotDribblerSpecs getDribblerSpecs()
	{
		return dribblerSpecs;
	}
}
