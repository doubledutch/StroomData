package me.doubledutch.stroom.client.function;

import org.json.*;

@FunctionalInterface
public interface JSONObjectBiFunction{
	public JSONObject apply(JSONObject obj1,JSONObject obj2) throws JSONException;
}