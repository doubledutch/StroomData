package me.doubledutch.stroom.client.function;

import org.json.*;

@FunctionalInterface
public interface JSONObjectArrayFunction{
	public JSONArray apply(JSONObject obj) throws JSONException;
}