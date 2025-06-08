/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gui.visualizer.presenter;

import edu.tigers.sumatra.gui.visualizer.presenter.callbacks.FieldScaler;
import lombok.RequiredArgsConstructor;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseWheelEvent;


@RequiredArgsConstructor
public class ZoomMouseAdapter extends MouseAdapter
{
	private static final double SCROLL_FACTOR = 0.1;

	private final FieldScaler fieldScaler;


	@Override
	public void mouseWheelMoved(final MouseWheelEvent e)
	{
		double wheelRotation = e.getPreciseWheelRotation();
		double scroll = -wheelRotation * SCROLL_FACTOR;
		fieldScaler.scale(e.getPoint(), scroll);
	}
}
