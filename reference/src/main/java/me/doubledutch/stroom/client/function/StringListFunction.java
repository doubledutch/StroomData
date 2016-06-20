package me.doubledutch.stroom.client.function;

import java.util.*;
import org.json.*;

@FunctionalInterface
public interface StringListFunction{
	public List<String> apply(String obj) throws JSONException;
}