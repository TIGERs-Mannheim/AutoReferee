/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.visualizer.field.ruler;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.visualizer.field.FieldPanel;
import lombok.RequiredArgsConstructor;

import javax.swing.SwingUtilities;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


@RequiredArgsConstructor
public class RulerMouseAdapter extends MouseAdapter
{
	private final FieldPanel fieldPanel;
	private IVector2 dragPointStart;


	@Override
	public void mousePressed(final MouseEvent e)
	{
		IVector2 guiPos = fieldPanel.getFieldPos(e.getX(), e.getY());
		dragPointStart = fieldPanel.transformToGlobalCoordinates(guiPos);
	}


	@Override
	public void mouseDragged(final MouseEvent e)
	{
		if (SwingUtilities.isLeftMouseButton(e) && (e.isControlDown() || e.isAltDown()))
		{
			IVector2 guiPos = fieldPanel.getFieldPos(e.getX(), e.getY());
			IVector2 dragPointEnd = fieldPanel.transformToGlobalCoordinates(guiPos);
			fieldPanel.setRuler(new Ruler(dragPointStart, dragPointEnd));
		}
	}


	@Override
	public void mouseReleased(final MouseEvent e)
	{
		dragPointStart = null;
		fieldPanel.setRuler(null);
	}
}
