/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.vision.data;

import edu.tigers.sumatra.drawable.IShapeLayer;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public enum EVisionFilterShapesLayer implements IShapeLayer
{
	QUALITY_SHAPES("Quality Inspector"),
	CAM_INFO_SHAPES("Cam Info"),
	VIEWPORT_SHAPES("Viewports"),
	ROBOT_TRACKER_SHAPES("Robot Trackers"),
	BALL_TRACKER_SHAPES("Ball Trackers");
	
	private final String id;
	private final String		name;
	private final boolean	visible;
	private final int			orderId;
	
	
	/**
	 *
	 */
	EVisionFilterShapesLayer(final String name)
	{
		this(name, false);
	}
	
	
	/**
	 *
	 */
	EVisionFilterShapesLayer(final String name, final boolean visible)
	{
		this.name = name;
		this.visible = visible;
		orderId = 10 + ordinal();
		id = EVisionFilterShapesLayer.class.getCanonicalName() + name();
	}
	
	
	/**
	 * @return
	 */
	@Override
	public String getLayerName()
	{
		return name;
	}
	
	
	/**
	 * @return the category
	 */
	@Override
	public final String getCategory()
	{
		return "Vision Filter";
	}
	
	
	@Override
	public int getOrderId()
	{
		return orderId;
	}
	
	
	@Override
	public String getId()
	{
		return id;
	}
	
	
	@Override
	public boolean isVisibleByDefault()
	{
		return visible;
	}
}
