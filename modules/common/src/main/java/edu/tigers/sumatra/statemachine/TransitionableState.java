/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.statemachine;

import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;


@RequiredArgsConstructor
public abstract class TransitionableState<T extends IState> extends AState
{
	private final Consumer<T> nextStateConsumer;
	private final List<Transition> transitions = new ArrayList<>();

	@Setter
	private boolean deactivateTransitions = false;


	public final void addTransition(BooleanSupplier evaluation, T nextState)
	{
		transitions.add(new Transition(evaluation, nextState));
	}


	public final void addTransition(BooleanSupplier evaluation1, BooleanSupplier evaluation2, T nextState)
	{
		transitions.add(new Transition(() -> evaluation1.getAsBoolean() && evaluation2.getAsBoolean(), nextState));
	}


	protected void onInit()
	{
		// can be overwritten
	}


	protected void onExit()
	{
		// can be overwritten
	}


	protected void beforeUpdate()
	{
		// can be overwritten
	}


	protected void onUpdate()
	{
		// can be overwritten
	}


	@Override
	public void doEntryActions()
	{
		onInit();
	}


	@Override
	public void doExitActions()
	{
		onExit();
	}


	@Override
	public final void doUpdate()
	{
		beforeUpdate();
		if (!deactivateTransitions)
		{
			for (var transition : transitions)
			{
				if (transition.evaluation.getAsBoolean())
				{
					nextStateConsumer.accept(transition.nextState);
					return;
				}
			}
		}
		onUpdate();
	}


	@RequiredArgsConstructor
	private class Transition
	{
		final BooleanSupplier evaluation;
		final T nextState;
	}
}
