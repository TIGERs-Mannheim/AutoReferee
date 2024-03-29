/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.vis;

import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableFieldBackground;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.DrawableRectangle;
import edu.tigers.sumatra.drawable.DrawableShapeBoundary;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.Goal;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;

import java.awt.Color;
import java.util.List;


/**
 * Generate field lines
 */
public class BorderVisCalc implements IWpCalc
{
	@Override
	public void process(final WorldFrameWrapper wfw, final ShapeMap shapeMap)
	{
		List<IDrawableShape> shapes = shapeMap.get(EWpShapesLayer.FIELD_BORDERS);

		shapes.add(new DrawableFieldBackground(Geometry.getField(), Geometry.getBoundaryWidth()));
		shapes.add(new DrawableRectangle(Geometry.getField(), Color.WHITE));
		shapes.add(new DrawableCircle(Geometry.getCenterCircle(), Color.WHITE));
		shapes.add(new DrawableLine(Vector2.fromXY(0, -Geometry.getFieldWidth() / 2.0),
				Vector2.fromXY(0, Geometry.getFieldWidth() / 2.0), Color.WHITE));

		List<IDrawableShape> additionalShapes = shapeMap.get(EWpShapesLayer.FIELD_BORDERS_ADDITIONAL);
		additionalShapes.add(new DrawableLine(Vector2.fromXY(-Geometry.getFieldLength() / 2, 0),
				Vector2.fromXY(Geometry.getFieldLength() / 2.0, 0), Color.WHITE));
		additionalShapes.add(new DrawableLine(
				Lines.segmentFromPoints(Vector2.fromXY(-Geometry.getFieldLength() / 4, -Geometry.getFieldWidth() / 2.0),
						Vector2.fromXY(-Geometry.getFieldLength() / 4, Geometry.getFieldWidth() / 2.0)),
				Color.WHITE));
		additionalShapes.add(new DrawableLine(
				Lines.segmentFromPoints(Vector2.fromXY(Geometry.getFieldLength() / 4, -Geometry.getFieldWidth() / 2.0),
						Vector2.fromXY(Geometry.getFieldLength() / 4, Geometry.getFieldWidth() / 2.0)),
				Color.WHITE));

		shapes.add(new DrawableShapeBoundary(Geometry.getPenaltyAreaOur(), Color.WHITE));
		shapes.add(new DrawableShapeBoundary(Geometry.getPenaltyAreaTheir(), Color.WHITE));

		Color ourColor = wfw.getRefereeMsg().getNegativeHalfTeam() == ETeamColor.BLUE ? Color.blue : Color.yellow;
		drawGoal(Geometry.getGoalOur(), shapes, ourColor);

		Color theirColor = wfw.getRefereeMsg().getNegativeHalfTeam() != ETeamColor.BLUE ? Color.blue : Color.yellow;
		drawGoal(Geometry.getGoalTheir(), shapes, theirColor);

		if (wfw.getGameState().isPenaltyOrPreparePenalty())
		{
			shapes.add(
					new DrawableCircle(Circle.createCircle(Geometry.getPenaltyMarkTheir(), Geometry.getBallRadius() + 10),
							Color.white).setFill(true));
			shapes.add(new DrawableCircle(Circle.createCircle(Geometry.getPenaltyMarkOur(), Geometry.getBallRadius() + 10),
					Color.white).setFill(true));
		}
	}


	private void drawGoal(final Goal goal, final List<IDrawableShape> shapes, final Color color)
	{
		IVector2 gpl = goal.getLeftPost();
		double inv = -Math.signum(gpl.x());
		IVector2 gplb = gpl.addNew(Vector2.fromXY(-Geometry.getGoalOur().getDepth() * inv, 0));
		IVector2 gpr = goal.getRightPost();
		IVector2 gprb = gpr.addNew(Vector2.fromXY(-Geometry.getGoalOur().getDepth() * inv, 0));
		shapes.add(new DrawableLine(gpl, gplb, color));
		shapes.add(new DrawableLine(gpr, gprb, color));
		shapes.add(new DrawableLine(gplb, gprb, color));
	}
}
