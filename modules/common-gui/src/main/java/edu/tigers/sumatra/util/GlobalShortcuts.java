/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.swing.JFrame;
import javax.swing.KeyStroke;
import java.awt.Component;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;


/**
 * Manage global shortcuts.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GlobalShortcuts
{
	private static final List<UiShortcut> UI_SHORTCUTS = new CopyOnWriteArrayList<>();
	private static final List<UiShortcut> DISABLED_UI_SHORTCUTS = new CopyOnWriteArrayList<>();


	public static void add(
			String name,
			Component component,
			Runnable runnable,
			KeyStroke keyStroke
	)
	{
		KeyEventDispatcher dispatcher = e -> {
			var eventKeyStroke = KeyStroke.getKeyStrokeForEvent(e);
			if (eventKeyStroke.equals(keyStroke))
			{
				var rootComponent = findRootComponent(component);
				Component eventComponent = e.getComponent();
				if (eventComponent != null && findRootComponent(eventComponent) == rootComponent)
				{
					runnable.run();
					return true;
				}
			}
			return false;
		};


		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher);
		UI_SHORTCUTS.add(UiShortcut.builder()
				.name(name)
				.keyStroke(keyStroke)
				.component(component)
				.dispatcher(dispatcher)
				.build()
		);
	}


	//** Moves the specified shortcut to the disabled ui shortcuts and disables the dispatcher*/
	private static void disableShortcut(UiShortcut shortcut)
	{
		KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(shortcut.getDispatcher());
	}


	//** Moves the specified shortcut to the active ui shortcuts and enables the dispatcher*/
	private static void enableShortcut(UiShortcut shortcut)
	{
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(shortcut.getDispatcher());
	}


	/**
	 * this method allows to disable shortcuts with specific keystrokes temporarily
	 * it is meant as a workaraound for keystrokes that conflict in certain contexts
	 * example: disable all space bar shortcuts that are space bar in text fields
	 * they need to be re-enabled afterward with enableAllKeyStrokes()
	 */
	public static void disableByKeyStroke(KeyStroke keyStroke)
	{
		// build the shortcut key identifier so that we can identify the keystroke we want to disable
		// find all shortcuts that match the desired keystrokes and put them into the disabled shortcuts
		DISABLED_UI_SHORTCUTS.addAll(UI_SHORTCUTS.stream().filter(s -> s.getKeyStroke().equals(keyStroke)).toList());
		// disable their dispatchers
		DISABLED_UI_SHORTCUTS.forEach(GlobalShortcuts::disableShortcut);
		// remove them from the active shortcuts
		UI_SHORTCUTS.removeAll(DISABLED_UI_SHORTCUTS);
	}


	/**
	 * This function enables all currently disabled shortcuts
	 */
	public static void enableAllShortcuts()
	{
		// enable all the shortcuts
		DISABLED_UI_SHORTCUTS.forEach(GlobalShortcuts::enableShortcut);
		// put them in the active shortcut list
		UI_SHORTCUTS.addAll(DISABLED_UI_SHORTCUTS);
		// clear out all disabled shortcuts (because they are now active)
		DISABLED_UI_SHORTCUTS.clear();
	}


	public static void removeAllForFrame(JFrame frame)
	{
		UI_SHORTCUTS.stream().filter(s -> findRootComponent(s.getComponent()) == frame).forEach(s -> {
			disableShortcut(s);
			UI_SHORTCUTS.remove(s);
		});
	}


	public static void removeAllForComponent(Component component)
	{
		UI_SHORTCUTS.stream().filter(s -> s.getComponent() == component).forEach(s -> {
			disableShortcut(s);
			UI_SHORTCUTS.remove(s);
		});
	}


	public static List<UiShortcut> getShortcuts(Component component)
	{
		var rootComponent = findRootComponent(component);
		return UI_SHORTCUTS.stream()
				.filter(s -> findRootComponent(s.getComponent()) == rootComponent)
				.collect(Collectors.toUnmodifiableList());
	}


	private static Component findRootComponent(Component component)
	{
		Component parent = component.getParent();
		if (parent == null)
		{
			return component;
		}
		return findRootComponent(parent);
	}
}
