/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.snapshot;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;
import edu.tigers.sumatra.math.vector.IVector3;
import lombok.Value;


/**
 * A snapshot object.
 */
@Value
public class SnapObject
{
	/**
	 * [mm,mm,rad]
	 */
	IVector3 pos;
	/**
	 * [m/s,m/s,rad/s]
	 */
	IVector3 vel;


	/**
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public JsonObject toJSON()
	{
		JsonObject obj = new JsonObject();
		obj.put("pos", JsonConverter.encode(pos));
		obj.put("vel", JsonConverter.encode(vel));
		return obj;
	}


	/**
	 * @param obj to read from
	 * @return snapObject from json
	 */
	public static SnapObject fromJSON(final JsonObject obj)
	{
		if (obj == null)
		{
			return null;
		}
		return new SnapObject(JsonConverter.decodeVector3((JsonArray) obj.get("pos")),
				JsonConverter.decodeVector3((JsonArray) obj.get("vel")));
	}
}
