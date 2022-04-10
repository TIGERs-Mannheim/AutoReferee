/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.visualizer.field.coordinates;

import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.visualizer.field.FieldPanel;
import lombok.RequiredArgsConstructor;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.stream.Stream;


@RequiredArgsConstructor
public class CoordinatesMouseAdapter extends MouseAdapter
{
	private final FieldPanel fieldPanel;


	@Override
	public void mouseMoved(final MouseEvent e)
	{
		IVector2 guiPos = fieldPanel.getFieldPos(e.getX(), e.getY());
		IVector2 lastMousePoint = fieldPanel.transformToGlobalCoordinates(guiPos);
		List<Coordinates> coordinates = Stream.of(ETeamColor.values())
				.map(team -> new Coordinates(lastMousePoint, team, fieldPanel.getSize()))
				.toList();
		fieldPanel.setCoordinates(coordinates);
	}
}
