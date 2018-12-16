/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine.events;

/**
 * AutoRef game events and mapping to human readable texts and referee protocol enum
 */
public enum EGameEvent
{
	// Match Proceeding
	PREPARED("Prepared"),
	NO_PROGRESS_IN_GAME("No progress in game"),
	PLACEMENT_FAILED("Ball placement failed"),
	PLACEMENT_SUCCEEDED("Ball placement successful"),
	BOT_SUBSTITUTION("Bot substitution"),
	TOO_MANY_ROBOTS("Too many robots"),
	
	
	// Ball out of field
	BALL_LEFT_FIELD_GOAL_LINE("Ball left the field (goal line)"),
	BALL_LEFT_FIELD_TOUCH_LINE("Ball left the field (touch line)"),
	GOAL("Goal"),
	POSSIBLE_GOAL("Possible goal"),
	INDIRECT_GOAL("Indirect goal"),
	CHIP_ON_GOAL("Ball was chipped on goal"),
	
	
	// Minor offense
	AIMLESS_KICK("Aimless kick (icing)"),
	KICK_TIMEOUT("Kick timeout"),
	KEEPER_HELD_BALL("Keeper held ball"),
	ATTACKER_DOUBLE_TOUCHED_BALL("Attacker double touched ball"),
	ATTACKER_IN_DEFENSE_AREA("Attacker in defense area"),
	ATTACKER_TOUCH_KEEPER("Attacker touched keeper"),
	BOT_DRIBBLED_BALL_TOO_FAR("Ball was dribbled to far"),
	BOT_KICKED_BALL_TOO_FAST("Ball was kicked to fast"),
	
	
	// Fouls
	ATTACKER_TOO_CLOSE_TO_DEFENSE_AREA("Attacker too close to defense area"),
	BOT_INTERFERED_PLACEMENT("Bot interfered placement"),
	BOT_CRASH_DRAWN("Bot crash (drawn)"),
	BOT_CRASH_UNIQUE("Bot crash (unique)"),
	BOT_CRASH_UNIQUE_SKIPPED("Bot crash (unique, skipped)"),
	BOT_PUSHED_BOT("Bot pushed bot"),
	BOT_PUSHED_BOT_SKIPPED("Bot pushed bot (skipped)"),
	BOT_HELD_BALL_DELIBERATELY("Ball was hold deliberately"),
	BOT_TIPPED_OVER("Bot tipped over"),
	BOT_TOO_FAST_IN_STOP("Robot stop speed"),
	DEFENDER_TOO_CLOSE_TO_KICK_POINT("Defender to close to kick point"),
	DEFENDER_IN_DEFENSE_AREA_PARTIALLY("Defender is in defense-area (partially)"),
	DEFENDER_IN_DEFENSE_AREA("Defender is in defense-area (full)"),
	
	
	// Repeated
	MULTIPLE_CARDS("Team got multiple cards"),
	MULTIPLE_PLACEMENT_FAILURES("Team failed multiple ball placements"),
	MULTIPLE_FOULS("Team made multiple fouls"),
	
	// Unsporting behaviors
	UNSPORTING_BEHAVIOR_MINOR("Minor unsporting behavior"),
	UNSPORTING_BEHAVIOR_MAJOR("Major unsporting behavior")
	
	
	;
	
	
	private final String eventText;
	
	
	EGameEvent(final String eventText)
	{
		this.eventText = eventText;
	}
	
	
	public String getEventText()
	{
		return eventText;
	}
}
