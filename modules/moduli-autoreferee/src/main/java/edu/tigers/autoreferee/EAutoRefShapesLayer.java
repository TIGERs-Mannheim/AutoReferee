/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 30, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee;

import edu.tigers.sumatra.wp.data.ShapeMap.IShapeLayer;


/**
 * @author "Lukas Magel"
 */
public enum EAutoRefShapesLayer implements IShapeLayer
{
	/**  */
	ENGINE("Engine", "AUTOREFEREE", true),
	/**  */
	LAST_BALL_CONTACT("Ball Contact", "AUTOREFEREE", true),
	/**  */
	LAST_BALL_CONTACT_EXT("Ball Contact ext", "AUTOREFEREE", false);
	
	private final String		name;
	private final String		category;
	private final boolean	visible;
	
	
	private EAutoRefShapesLayer(final String name, final String category, final boolean visible)
	{
		this.name = name;
		this.category = category;
		this.visible = visible;
	}
	
	
	@Override
	public String getCategory()
	{
		return category;
	}
	
	
	@Override
	public String getLayerName()
	{
		return name;
	}
	
	
	@Override
	public String getId()
	{
		return null;
	}
	
	
	@Override
	public boolean isVisibleByDefault()
	{
		return visible;
	}
	
}
