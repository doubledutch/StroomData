package me.doubledutch.stroom.client.function;

import org.json.*;

@FunctionalInterface
public interface JSONObjectPredicate{
	public boolean test(JSONObject obj) throws JSONException;
}