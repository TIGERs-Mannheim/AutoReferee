/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.penarea;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.circle.Arc;
import edu.tigers.sumatra.math.circle.CircleMath;
import edu.tigers.sumatra.math.circle.IArc;
import edu.tigers.sumatra.math.line.ILineSegment;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.quadrilateral.Quadrilateral;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.rectangle.Rectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import lombok.Getter;


@Persistent
public class FinisherMoveShape
{
	private double x1;
	private double x2;
	private double width;
	private double radius;

	@Getter
	private ILineSegment lineLeft;
	@Getter
	private ILineSegment lineBottom;
	@Getter
	private ILineSegment lineRight;
	@Getter
	private IArc leftArc;
	@Getter
	private IArc rightArc;


	private FinisherMoveShape()
	{
		// empty constructor for Berkeley
	}


	public FinisherMoveShape(double x1, double x2, double width, double radius)
	{
		this.x1 = x1;
		this.x2 = x2;
		this.width = width;
		this.radius = radius;
		IVector2 topLeftCorner = Vector2.fromXY(x2, width);
		IVector2 bottomLeftCorner = Vector2.fromXY(x1, width);
		IVector2 bottomRightCorner = Vector2.fromXY(x1, -width);
		IVector2 topRightCorner = Vector2.fromXY(x2, -width);

		lineLeft = Lines.segmentFromPoints(topLeftCorner, bottomLeftCorner.addNew(Vector2.fromX(radius)));
		lineBottom = Lines.segmentFromPoints(bottomLeftCorner.subtractNew(Vector2.fromY(radius)),
				bottomRightCorner.addNew(Vector2.fromY(radius)));
		lineRight = Lines.segmentFromPoints(bottomRightCorner.addNew(Vector2.fromX(radius)), topRightCorner);

		var leftCircleCenter = bottomLeftCorner.addNew(Vector2.fromXY(radius, -radius));
		leftArc = Arc.createArc(leftCircleCenter, radius, Vector2.fromX(-1).getAngle(), -AngleMath.PI_HALF);

		var rightCircleCenter = bottomRightCorner.addNew(Vector2.fromXY(radius, radius));
		rightArc = Arc.createArc(rightCircleCenter, radius, Vector2.fromX(-1).getAngle(), AngleMath.PI_HALF);
	}


	public FinisherMoveShape withMargin(double margin)
	{
		return new FinisherMoveShape(x1 - margin, x2, width + margin, radius);
	}


	public double getMaxLength()
	{
		double lengthSide = lineLeft.getLength();
		double lengthOfArc = (0.5 * leftArc.radius() * Math.PI);
		double lengthBottom = lineBottom.getLength();
		return lengthSide * 2 + lengthOfArc * 2 + lengthBottom;
	}


	public double getStepPositionOnShape(IVector2 pos)
	{
		double lengthSide = lineLeft.getLength();
		double lengthOfArc = (0.5 * leftArc.radius() * Math.PI);
		double lengthBottom = lineBottom.getLength();

		double distLineLeft = lineLeft.distanceTo(pos);
		double distLineRight = lineRight.distanceTo(pos);
		double distLineBottom = lineBottom.distanceTo(pos);

		double distArcLeft = CircleMath.nearestPointOnArcLine(leftArc, pos).distanceTo(pos);
		double distArcRight = CircleMath.nearestPointOnArcLine(rightArc, pos).distanceTo(pos);

		if (distLineLeft < distArcLeft
				&& distLineLeft < distLineBottom
				&& distLineLeft < distArcRight
				&& distLineLeft < distLineRight)
		{
			return lineLeft.closestPointOnLine(pos).distanceTo(lineLeft.getStart());
		}

		if (distArcLeft < distLineLeft
				&& distArcLeft < distLineBottom
				&& distArcLeft < distArcRight
				&& distArcLeft < distLineRight)
		{
			IVector2 pointOnArc = CircleMath.nearestPointOutsideArc(leftArc, pos);
			IVector2 pointOnArcToCenter = leftArc.center().subtractNew(pointOnArc);

			IVector2 arcStart = CircleMath.stepAlongCircle(
					lineLeft.getEnd(),
					leftArc.center(),
					0);
			IVector2 arcStartToCenter = leftArc.center().subtractNew(arcStart);
			double angle = pointOnArcToCenter.angleToAbs(arcStartToCenter).orElse(0.0);

			double distanceOnArc = 2 * Math.PI * leftArc.radius() * (AngleMath.rad2deg(angle) / 360.0);
			return lengthSide + distanceOnArc;
		}

		if (distLineBottom < distArcLeft
				&& distLineBottom < distLineRight
				&& distLineBottom < distArcRight
				&& distLineBottom < distLineLeft)
		{
			return lengthSide + lengthOfArc + lineBottom.closestPointOnLine(pos).distanceTo(lineBottom.getStart());
		}

		if (distArcRight < distLineLeft
				&& distArcRight < distLineBottom
				&& distArcRight < distArcLeft
				&& distArcRight < distLineRight)
		{
			IVector2 pointOnArc = CircleMath.nearestPointOutsideArc(rightArc, pos);
			IVector2 pointOnArcToCenter = rightArc.center().subtractNew(pointOnArc);

			IVector2 arcStart = CircleMath.stepAlongCircle(
					lineRight.getStart(),
					rightArc.center(),
					0);
			IVector2 arcStartToCenter = rightArc.center().subtractNew(arcStart);
			double angle = pointOnArcToCenter.angleToAbs(arcStartToCenter).orElse(0.0);

			double distanceOnArc = lengthOfArc - 2 * Math.PI * rightArc.radius() * (AngleMath.rad2deg(angle) / 360.0);
			return lengthSide + distanceOnArc + lengthBottom + lengthOfArc;
		}

		if (distLineRight < distArcLeft
				&& distLineRight < distLineBottom
				&& distLineRight < distArcRight
				&& distLineRight < distLineLeft)
		{
			return lengthSide + lengthBottom + lengthOfArc * 2 +
					lineRight.closestPointOnLine(pos).distanceTo(lineRight.getStart());
		}

		return 0;
	}


	public IRectangle getBoundingRectangle()
	{
		return Rectangle.fromPoints(lineLeft.getStart(), lineRight.getStart().addNew(Vector2.fromX(-leftArc.radius())));
	}


	public IVector2 stepOnShape(double step)
	{
		double lengthSide = lineLeft.getLength();
		double lengthOfArc = (0.5 * leftArc.radius() * Math.PI);
		double lengthBottom = lineBottom.getLength();

		IVector2 position;
		position = switch (getSection(step))
				{
					case LEFT_SIDE -> lineLeft.stepAlongLine(Math.max(0, step));
					case LEFT_ARC -> CircleMath.stepAlongCircle(
							lineLeft.getEnd(),
							leftArc.center(),
							SumatraMath.relative(step - lengthSide, 0, lengthOfArc) * AngleMath.PI_HALF);
					case BOTTOM_LINE -> lineBottom.stepAlongLine(step - lengthSide - lengthOfArc);
					case RIGHT_ARC -> CircleMath.stepAlongCircle(
							lineBottom.getEnd(),
							rightArc.center(),
							SumatraMath.relative(step - lengthSide - lengthOfArc - lengthBottom, 0, lengthOfArc)
									* AngleMath.PI_HALF
					);
					case RIGHT_SIDE -> lineRight.stepAlongLine(step - lengthSide - lengthOfArc - lengthBottom - lengthOfArc);
				};
		return position;
	}


	private EFinisherMoveShapeSection getSection(double pos)
	{
		double lengthSide = lineLeft.getLength();
		double lengthOfArc = (0.5 * leftArc.radius() * Math.PI);
		double lengthBottom = lineBottom.getLength();

		if (pos < 0)
		{
			return EFinisherMoveShapeSection.LEFT_SIDE;
		}
		if (pos < lengthSide)
		{
			return EFinisherMoveShapeSection.LEFT_SIDE;
		} else if (pos < lengthSide + lengthOfArc)
		{
			return EFinisherMoveShapeSection.LEFT_ARC;
		} else if (pos < lengthSide + lengthOfArc + lengthBottom)
		{
			return EFinisherMoveShapeSection.BOTTOM_LINE;
		} else if (pos < lengthSide + lengthOfArc + lengthBottom + lengthOfArc)
		{
			return EFinisherMoveShapeSection.RIGHT_ARC;
		}
		return EFinisherMoveShapeSection.RIGHT_SIDE;
	}


	public boolean isPointInShape(IVector2 point)
	{
		if (!getBoundingRectangle().isPointInShape(point))
		{
			// optimization for most cases
			return false;
		}

		return CircleMath.isPointInArc(getLeftArc(), point, 0) ||
				CircleMath.isPointInArc(getRightArc(), point, 0) ||
				Quadrilateral.fromCorners(lineRight.getStart(), lineBottom.getEnd(), lineBottom.getStart(),
						lineLeft.getEnd()).isPointInShape(point) ||
				Quadrilateral.fromCorners(lineLeft.getStart(), lineLeft.getEnd(), lineRight.getStart(), lineRight.getEnd())
						.isPointInShape(point);
	}
}
