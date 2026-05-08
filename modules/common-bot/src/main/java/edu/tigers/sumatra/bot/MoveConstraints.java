package edu.tigers.sumatra.bot;

import edu.tigers.sumatra.bot.params.IBotMovementLimits;
import edu.tigers.sumatra.data.collector.IExportable;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.With;
import org.apache.commons.lang.Validate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Constraints on the movement of robots.
 */
@Value
@Builder(toBuilder = true)
@With
public class MoveConstraints implements IExportable
{
	double velMax;
	double accMax;
	double brkMax;
	double velMaxW;
	double accMaxW;

	@NonNull
	@Builder.Default
	IVector2 primaryDirection = Vector2f.ZERO_VECTOR;


	public static final MoveConstraints ZERO = MoveConstraints.builder().build();


	MoveConstraints(
			double velMax,
			double accMax,
			double brkMax,
			double velMaxW,
			double accMaxW,
			@NonNull IVector2 primaryDirection
	)
	{
		Validate.isTrue(velMax >= 0, "velMax must be >=0: ", velMax);
		Validate.isTrue(accMax >= 0, "accMax must be >=0: ", accMax);
		Validate.isTrue(brkMax >= 0, "brkMax must be >=0: ", brkMax);
		Validate.isTrue(velMaxW >= 0, "velMaxW must be >=0: ", velMaxW);
		Validate.isTrue(accMaxW >= 0, "accMaxW must be >=0: ", accMaxW);
		Validate.isTrue(primaryDirection.isFinite(), "primaryDirection must be finite: ", primaryDirection);
		this.velMax = velMax;
		this.accMax = accMax;
		this.brkMax = brkMax;
		this.velMaxW = velMaxW;
		this.accMaxW = accMaxW;
		this.primaryDirection = primaryDirection;
	}


	public static MoveConstraints from(IBotMovementLimits limits)
	{
		return MoveConstraints.builder()
				.velMax(limits.getVelMax())
				.accMax(limits.getAccMax())
				.brkMax(limits.getBrkMax())
				.velMaxW(limits.getVelMaxW())
				.accMaxW(limits.getAccMaxW())
				.build();
	}


	public MoveConstraints limitedBy(IBotMovementLimits limits)
	{
		return toBuilder()
				.velMax(Math.min(velMax, limits.getVelMax()))
				.accMax(Math.min(accMax, limits.getAccMax()))
				.brkMax(Math.min(brkMax, limits.getBrkMax()))
				.velMaxW(Math.min(velMaxW, limits.getVelMaxW()))
				.accMaxW(Math.min(accMaxW, limits.getAccMaxW()))
				.build();
	}


	@Override
	public List<Number> getNumberList()
	{
		List<Number> nbrs = new ArrayList<>();
		nbrs.add(velMax);
		nbrs.add(accMax);
		nbrs.add(brkMax);
		nbrs.add(velMaxW);
		nbrs.add(accMaxW);
		nbrs.add(primaryDirection.x());
		nbrs.add(primaryDirection.y());
		return nbrs;
	}


	@Override
	public List<String> getHeaders()
	{
		return Arrays.asList(
				"velMax", "accMax", "brkMax", "velMaxW", "accMaxW",
				"primaryDirectionX", "primaryDirectionY"
		);
	}
}
