/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.snapshot;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


class SnapshotTest
{
	@Test
	void testToJSONBall()
	{
		Snapshot snapshot = Snapshot.builder().bots(new HashMap<>()).ball(new SnapObject(Vector3.zero(), Vector3.zero()))
				.build();
		assertThat(snapshot.toJSON().toJSONString())
				.isEqualTo("{\"ball\":{\"pos\":[0.0,0.0,0.0],\"vel\":[0.0,0.0,0.0]},\"bots\":[]}");
	}


	@Test
	void testToJSONBotsBall()
	{
		Map<BotID, SnapObject> bots = new HashMap<>();
		bots.put(BotID.createBotId(0, ETeamColor.BLUE), new SnapObject(Vector3.zero(), Vector3.zero()));
		Snapshot snapshot = Snapshot.builder().bots(bots).ball(new SnapObject(Vector3.zero(), Vector3.zero())).build();
		assertThat(snapshot.toJSON().toJSONString()).isEqualTo(
				"{\"ball\":{\"pos\":[0.0,0.0,0.0],\"vel\":[0.0,0.0,0.0]},\"bots\":[{\"obj\":{\"pos\":[0.0,0.0,0.0],\"vel\":[0.0,0.0,0.0]},\"id\":{\"number\":0,\"color\":\"BLUE\"}}]}");
	}


	@Test
	void testToJSONAll() throws ParseException
	{
		Snapshot snapshot = Snapshot.builder()
				.bot(BotID.createBotId(0, ETeamColor.BLUE), new SnapObject(Vector3.zero(), Vector3.zero()))
				.ball(new SnapObject(Vector3.zero(), Vector3.zero()))
				.command(SslGcRefereeMessage.Referee.Command.FORCE_START)
				.stage(SslGcRefereeMessage.Referee.Stage.NORMAL_FIRST_HALF)
				.placementPos(Vector2.fromXY(1, 2))
				.build();

		String json = snapshot.toJSON().toJSONString();
		Snapshot parsedSnapshot = Snapshot.fromJSONString(json);

		assertThat(parsedSnapshot).isEqualTo(snapshot);
	}


	@Test
	void testFromJSONBall() throws ParseException
	{
		JSONParser parser = new JSONParser();
		Snapshot snapshot = Snapshot.builder().bots(new HashMap<>()).ball(new SnapObject(Vector3.zero(), Vector3.zero()))
				.build();
		assertEquals(snapshot,
				Snapshot.fromJSON(
						(JSONObject) parser.parse("{\"ball\":{\"pos\":[0.0,0.0,0.0],\"vel\":[0.0,0.0,0.0]},\"bots\":[]}")));
	}


	@Test
	void testFromJSONBotBall() throws ParseException
	{
		JSONParser parser = new JSONParser();
		Map<BotID, SnapObject> bots = new HashMap<>();
		bots.put(BotID.createBotId(0, ETeamColor.BLUE), new SnapObject(Vector3.zero(), Vector3.zero()));
		Snapshot snapshot = Snapshot.builder().bots(bots).ball(new SnapObject(Vector3.zero(), Vector3.zero())).build();
		assertEquals(snapshot,
				Snapshot.fromJSON(
						(JSONObject) parser.parse(
								"{\"ball\":{\"pos\":[0.0,0.0,0.0],\"vel\":[0.0,0.0,0.0]},\"bots\":[{\"obj\":{\"pos\":[0.0,0.0,0.0],\"vel\":[0.0,0.0,0.0]},\"id\":{\"number\":0,\"color\":\"BLUE\"}}]}")));
	}
}
