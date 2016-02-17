/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 7, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee.engine.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.RefCommand;
import edu.tigers.autoreferee.engine.RuleViolation;
import edu.tigers.autoreferee.engine.rules.impl.GoalRule;
import edu.tigers.autoreferee.engine.rules.impl.PlaceBallStateRule;
import edu.tigers.autoreferee.engine.rules.impl.PrepareKickoffStateRule;
import edu.tigers.autoreferee.engine.rules.impl.StopStateRule;
import edu.tigers.autoreferee.engine.rules.impl.violations.BallLeftFieldRule;
import edu.tigers.autoreferee.engine.rules.impl.violations.BallSpeedingRule;
import edu.tigers.autoreferee.engine.rules.impl.violations.BotNumberRule;
import edu.tigers.autoreferee.engine.rules.impl.violations.BotStopSpeedRule;
import edu.tigers.autoreferee.remote.IRefboxRemote;
import edu.tigers.sumatra.wp.data.EGameStateNeutral;


/**
 * This class processes all rules and sends out referee commands
 * 
 * @author "Lukas Magel"
 */
public class AutoRefEngine
{
	private static final Logger	log								= Logger.getLogger(AutoRefEngine.class);
	
	/** in ms */
	private static long				DUPLICATE_RESEND_WAIT_TIME	= 500;
	
	private List<IGameRule>			rules;
	private IRefboxRemote			remote;
	
	private FollowUpAction			lastFollowUp;
	private FollowUpAction			followUp;
	
	private RefCommand				lastRefCommand;
	private long						lastCommandTimestamp;
	
	
	/**
	 * @param remote
	 */
	public AutoRefEngine(final IRefboxRemote remote)
	{
		this.remote = remote;
		rules = new ArrayList<>();
		rules.add(new GoalRule());
		rules.add(new BallLeftFieldRule());
		rules.add(new BallSpeedingRule());
		rules.add(new PrepareKickoffStateRule());
		rules.add(new StopStateRule());
		rules.add(new PlaceBallStateRule());
		rules.add(new BotNumberRule());
		rules.add(new BotStopSpeedRule());
	}
	
	
	/**
	 * @param frame
	 */
	public void update(final IAutoRefFrame frame)
	{
		REFrameAdapter frameAdapter = new REFrameAdapter(frame, followUp, lastFollowUp);
		lastFollowUp = followUp;
		checkFollowUpLifetime(frame);
		
		EGameStateNeutral curState = frame.getGameState();
		EGameStateNeutral lastState = frame.getPreviousFrame().getGameState();
		
		// Retrieve the rules which are active in the current gamestate
		List<IGameRule> activeRules = rules.stream().filter(rule -> rule.isActiveIn(curState))
				.sorted(IGameRule.GameRuleComparator.INSTANCE).collect(Collectors.toList());
		
		// Reset all newly active rules
		activeRules.stream().filter(rule -> !rule.isActiveIn(lastState))
				.forEach(rule -> rule.reset());
		
		/*
		 * Run the update function on all rules.
		 * The stream is collected into a list first to force the stream to perform the update function on all elements.
		 */
		List<Optional<RuleResult>> optResults = activeRules.stream().map(rule -> rule.update(frameAdapter))
				.collect(Collectors.toList());
		
		/*
		 * Grab the result of the rule with the highest priority. The list is already in sorted order.
		 */
		List<RuleResult> results = optResults.stream().filter(optRslt -> optRslt.isPresent())
				.map(optRslt -> optRslt.get()).collect(Collectors.toList());
		Optional<RuleResult> optFirstResult = results.stream().findFirst();
		
		if (optFirstResult.isPresent())
		{
			RuleResult firstResult = optFirstResult.get();
			firstResult.getFollowUp().ifPresent(followUp -> this.followUp = followUp);
			firstResult.getCommands().forEach(cmd -> sendCommand(cmd, frame.getTimestamp()));
		}
		
		/*
		 * Log all rule violations
		 */
		results.stream().filter(result -> result.getViolation().isPresent()).map(result -> result.getViolation().get())
				.forEach(violation -> logViolation(violation));
		
	}
	
	
	private void sendCommand(final RefCommand cmd, final long ts)
	{
		if (cmd.equals(lastRefCommand)
				&& (((ts - lastCommandTimestamp) * 1_000_000) < DUPLICATE_RESEND_WAIT_TIME))
		{
			log.debug("Dropping duplicate ref message: " + cmd.getCommand());
			return;
		}
		lastRefCommand = cmd;
		lastCommandTimestamp = ts;
		doSendCommand(cmd);
	}
	
	
	private void doSendCommand(final RefCommand cmd)
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Sending new command: ");
		builder.append(cmd.getCommand());
		cmd.getKickPos().ifPresent(pos -> {
			builder.append(" @Position: ");
			builder.append(pos.x());
			builder.append(" | ");
			builder.append(pos.y());
		});
		log.info(builder.toString());
		remote.sendCommand(cmd);
	}
	
	
	private void logViolation(final RuleViolation violation)
	{
		log.warn("Rule violation: " + violation.getViolationType() + " Committed by team: " + violation.getTeamAtFault());
	}
	
	
	private void checkFollowUpLifetime(final IAutoRefFrame frame)
	{
		if ((frame.getGameState() == EGameStateNeutral.RUNNING)
				&& (frame.getPreviousFrame().getGameState() != EGameStateNeutral.RUNNING))
		{
			followUp = null;
		}
	}
	
	
	/**
	 * Reset all saved state
	 */
	public void reset()
	{
		followUp = null;
		rules.stream().forEach(rule -> rule.reset());
	}
}
