package edu.tigers.autoreferee.engine.events;

import edu.tigers.sumatra.gamecontroller.SslGameEvent2019;
import edu.tigers.sumatra.ids.ETeamColor;


public class PlacementFailedEvent extends AGameEvent
{
	private final ETeamColor team;
	private final double remainingDistance;
	
	
	public PlacementFailedEvent(final ETeamColor team, final double remainingDistance)
	{
		this.team = team;
		this.remainingDistance = remainingDistance;
	}
	
	
	@Override
	public EGameEvent getType()
	{
		return EGameEvent.PLACEMENT_FAILED;
	}
	
	
	@Override
	public SslGameEvent2019.GameEvent toProtobuf()
	{
		SslGameEvent2019.GameEvent.Builder builder = SslGameEvent2019.GameEvent.newBuilder();
		builder.setType(SslGameEvent2019.GameEventType.PLACEMENT_FAILED);
		builder.getPlacementFailedBuilder()
				.setByTeam(getTeam(team))
				.setRemainingDistance((float) remainingDistance / 1000f)
				.build();
		
		return builder.build();
	}
	
	
	@Override
	public String toString()
	{
		return String.format("Placement failed by %s: %.2f mm remaining", team, remainingDistance);
	}
}
