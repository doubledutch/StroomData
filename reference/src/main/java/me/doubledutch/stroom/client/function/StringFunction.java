package me.doubledutch.stroom.client.function;

import java.util.*;
import org.json.*;

@FunctionalInterface
public interface StringFunction{
	public String apply(String obj) throws JSONException;
}