/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.events;


import edu.tigers.sumatra.gamecontroller.SslGameControllerCommon;
import edu.tigers.sumatra.gamecontroller.SslGameControllerCommon.Team;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;


public abstract class AGameEvent implements IGameEvent
{
	protected Team getTeam(ETeamColor color)
	{
		if (color == ETeamColor.YELLOW)
		{
			return Team.YELLOW;
		} else if (color == ETeamColor.BLUE)
		{
			return Team.BLUE;
		} else
		{
			throw new IllegalArgumentException("Team should be either yellow or blue: " + color);
		}
	}
	
	
	protected Team getTeam(BotID id)
	{
		return getTeam(id.getTeamColor());
	}
	
	
	protected SslGameControllerCommon.Location getLocationFromVector(IVector2 location)
	{
		return SslGameControllerCommon.Location.newBuilder().setX((float) (location.x() / 1000.f))
				.setY((float) location.y() / 1000.f)
				.build();
	}
	
	
	protected String formatVector(IVector2 vec)
	{
		return String.format("(%.3f | %.3f)", vec.x(), vec.y());
	}
	
	
	@Override
	public String toString()
	{
		return getType().getEventText();
	}
}
