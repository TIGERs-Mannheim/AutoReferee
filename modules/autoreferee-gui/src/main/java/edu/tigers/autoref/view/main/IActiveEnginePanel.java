/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 24, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoref.view.main;

import edu.tigers.autoref.view.main.IActiveEnginePanel.IActiveEnginePanelObserver;
import edu.tigers.sumatra.components.IBasePanel;


/**
 * @author "Lukas Magel"
 */
public interface IActiveEnginePanel extends IBasePanel<IActiveEnginePanelObserver>
{
	/**
	 * @author Lukas Magel
	 */
	interface IActiveEnginePanelObserver
	{
		/**
		 * Called whenever the reset button is pressed
		 */
		void onResetButtonPressed();
	}
}
