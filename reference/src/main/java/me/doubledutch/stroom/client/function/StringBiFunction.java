package me.doubledutch.stroom.client.function;

import java.util.*;
import org.json.*;

@FunctionalInterface
public interface StringBiFunction{
	public String apply(String obj1,String obj2) throws JSONException;
}