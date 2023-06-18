package edu.tigers.sumatra.math.circle;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.math.line.ILineBase;
import edu.tigers.sumatra.math.vector.IVector2;

import java.util.List;


/**
 * Implementations for circular shapes
 */
@Persistent
public abstract class ACircular implements ICircular
{

	@Override
	public final List<IVector2> lineIntersections(ILineBase line)
	{
		return CircleMath.lineIntersections(this, line);
	}


	@Override
	public final IVector2 nearestPointOutside(final IVector2 point)
	{
		return CircleMath.nearestPointOutsideCircle(this, point);
	}
}
