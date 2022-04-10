/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.visualizer.field.ruler;

import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.drawable.IDrawableTool;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;

import java.awt.Graphics2D;
import java.util.List;


public class Ruler implements IDrawableShape
{
	private final List<IDrawableShape> shapes;


	public Ruler(IVector2 start, IVector2 end)
	{
		IVector2 start2End = end.subtractNew(start);
		shapes = List.of(
				new DrawableLine(Line.fromPoints(start, end)),
				new DrawableAnnotation(start, String.format("%.1f/%.1f", start.x(), start.y())),
				new DrawableAnnotation(end, String.format("%.1f/%.1f", end.x(), end.y())),
				new DrawableAnnotation(start.addNew(start2End.multiplyNew(0.5)), start2End.toString())
		);
	}


	@Override
	public void paintShape(Graphics2D g, IDrawableTool tool, boolean invert)
	{
		shapes.forEach(shape -> shape.paintShape(g, tool, invert));
	}
}
