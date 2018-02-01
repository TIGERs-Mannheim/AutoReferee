/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.vis;

import static edu.tigers.sumatra.drawable.ShapeMap.EShapeLayerPersistenceType.NEVER_PERSIST;

import edu.tigers.sumatra.drawable.IShapeLayer;
import edu.tigers.sumatra.drawable.ShapeMap.EShapeLayerPersistenceType;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@SuppressWarnings("squid:S1192") // duplicated strings
public enum EWpShapesLayer implements IShapeLayer
{
	/**  */
	FIELD_BORDERS("Field Borders", "Field", true),
	/**  */
	REFEREE("Referee", "Field", true, 1000),
	/**  */
	BALL_BUFFER("Ball buffer", "Field", false, NEVER_PERSIST),
	/**  */
	BOT_BUFFER("Bot buffer", "Field", false, NEVER_PERSIST),
	/**  */
	BOTS("Bots", "Field", true),
	/**  */
	BALL("Ball", "Field", true),
	/** */
	BALL_PREDICTION("Ball prediction", "Field", false),
	/**  */
	VELOCITY("Velocities", "Field"),
	
	;
	
	
	private final String name;
	private final String category;
	private final boolean visible;
	private final int orderId;
	private final String id;
	private final EShapeLayerPersistenceType persistenceType;
	
	
	/**
	 * 
	 */
	EWpShapesLayer(final String name, final String category)
	{
		this(name, category, false);
	}
	
	
	/**
	 * 
	 */
	EWpShapesLayer(final String name, final String category, final boolean visible)
	{
		this.name = name;
		this.category = category;
		this.visible = visible;
		this.persistenceType = EShapeLayerPersistenceType.ALWAYS_PERSIST;
		orderId = 10 + ordinal();
		id = EWpShapesLayer.class.getCanonicalName() + name;
	}
	
	
	/**
	 *
	 */
	EWpShapesLayer(final String name, final String category, final boolean visible, final int orderId)
	{
		this.name = name;
		this.category = category;
		this.visible = visible;
		this.persistenceType = EShapeLayerPersistenceType.ALWAYS_PERSIST;
		this.orderId = orderId;
		id = EWpShapesLayer.class.getCanonicalName() + name;
	}
	
	
	/**
	 * 
	 */
	EWpShapesLayer(final String name, final String category, final boolean visible,
			final EShapeLayerPersistenceType persistenceType)
	{
		this.name = name;
		this.category = category;
		this.visible = visible;
		this.persistenceType = persistenceType;
		this.orderId = 10 + ordinal();
		id = EWpShapesLayer.class.getCanonicalName() + name;
	}
	
	
	@Override
	public String getLayerName()
	{
		return name;
	}
	
	
	@Override
	public final String getCategory()
	{
		return category;
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
	public EShapeLayerPersistenceType getPersistenceType()
	{
		return persistenceType;
	}
	
	
	@Override
	public boolean isVisibleByDefault()
	{
		return visible;
	}
	
	
}
