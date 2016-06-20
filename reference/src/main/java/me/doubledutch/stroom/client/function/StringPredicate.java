package me.doubledutch.stroom.client.function;

import org.json.*;

@FunctionalInterface
public interface StringPredicate{
	public boolean test(String obj) throws JSONException;
}