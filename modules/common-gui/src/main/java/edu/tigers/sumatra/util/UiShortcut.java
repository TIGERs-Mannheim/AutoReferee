/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.util;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import javax.swing.KeyStroke;
import java.awt.Component;
import java.awt.KeyEventDispatcher;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;


@Value
@Builder
public class UiShortcut
{
	@NonNull
	String name;
	@NonNull
	KeyStroke keyStroke;
	@NonNull
	Component component;
	@NonNull
	KeyEventDispatcher dispatcher;


	/**
	 * This method returns the description of the key Combo.
	 * This is for display purposes only. DO NOT USE THIS TO IDENTIFY THE KEYBIND
	 * Use the keyStroke for that.
	 */
	public String getKeys()
	{
		//if there is no modifier, there is no need for the +, just return the keyCode
		if (keyStroke.getModifiers() == 0)
		{
			return KeyEvent.getKeyText(keyStroke.getKeyCode());
		}
		//else, return modifier + keycode
		return InputEvent.getModifiersExText(keyStroke.getModifiers())
				+ "+"
				+ KeyEvent.getKeyText(keyStroke.getKeyCode());
	}
}
