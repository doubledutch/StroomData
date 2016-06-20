package me.doubledutch.stroom.client.function;

import org.json.*;

@FunctionalInterface
public interface JSONObjectFunction{
	public JSONObject apply(JSONObject obj) throws JSONException;
}