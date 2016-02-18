/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 18, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.autoref;

import org.apache.log4j.Logger;

import edu.tigers.autoreferee.AutoRefModule;
import edu.tigers.autoreferee.AutoRefModule.AutoRefState;
import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.IAutoRefStateObserver;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.visualizer.VisualizerPresenter;
import edu.tigers.sumatra.visualizer.view.field.EShapeLayerSource;
import edu.tigers.sumatra.wp.data.ShapeMap.IShapeLayer;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class VisualizerRefPresenter extends VisualizerPresenter implements IAutoRefStateObserver
{
	@SuppressWarnings("unused")
	private static final Logger	log				= Logger.getLogger(VisualizerRefPresenter.class.getName());
	
	private IAutoRefFrame			latestRefFrame	= null;
	
	
	@Override
	public void onModuliStateChanged(final ModulesState state)
	{
		super.onModuliStateChanged(state);
		switch (state)
		{
			case ACTIVE:
				try
				{
					AutoRefModule ref = (AutoRefModule) SumatraModel.getInstance().getModule(AutoRefModule.MODULE_ID);
					ref.addObserver(this);
				} catch (ModuleNotFoundException e)
				{
					log.error("Could not find auto referee module", e);
				}
				break;
			case NOT_LOADED:
				break;
			case RESOLVED:
				try
				{
					AutoRefModule ref = (AutoRefModule) SumatraModel.getInstance().getModule(AutoRefModule.MODULE_ID);
					ref.removeObserver(this);
				} catch (ModuleNotFoundException e)
				{
					log.error("Could not find auto referee module", e);
				}
				break;
			default:
				break;
		}
	}
	
	
	@Override
	public void onAutoRefStateChanged(final AutoRefState state)
	{
		if (state != AutoRefState.RUNNING)
		{
			// To clear out all old drawings
			latestRefFrame = null;
		}
	}
	
	
	@Override
	public void onNewAutoRefFrame(final IAutoRefFrame frame)
	{
		latestRefFrame = frame;
	}
	
	
	@Override
	protected void updateVisFrameShapes()
	{
		super.updateVisFrameShapes();
		
		if (latestRefFrame == null)
		{
			getPanel().getFieldPanel().clearField(EShapeLayerSource.AUTOREFEREE);
		} else
		{
			for (IShapeLayer sl : latestRefFrame.getShapes().getAllShapeLayers())
			{
				getPanel().getOptionsMenu().addMenuEntry(sl);
			}
			getPanel().getFieldPanel().setShapeMap(EShapeLayerSource.AUTOREFEREE, latestRefFrame.getShapes(),
					false);
		}
	}
}
