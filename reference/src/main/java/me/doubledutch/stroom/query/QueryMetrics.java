package me.doubledutch.stroom.query;

import org.json.*;
import java.util.*;

public class QueryMetrics{
	public String operation;
	public Map<String,Long> timers=new HashMap<String,Long>();
	public Map<String,Long> valueSet=new HashMap<String,Long>();
	public List<QueryMetrics> children=new ArrayList<QueryMetrics>();

	public QueryMetrics(String operation){
		this.operation=operation;
	}

	public void set(String id,long value){
		valueSet.put(id,value);
	}

	public long get(String id){
		if(valueSet.containsKey(id)){
			return valueSet.get(id);
		}
		return 0;
	}

	public void inc(String id,long value){
		if(valueSet.containsKey(id)){
			valueSet.put(id,value+valueSet.get(id));
		}else{
			valueSet.put(id,value);
		}
	}

	public void startTimer(String key){
		timers.put(key,System.nanoTime());
	}

	public void stopTimer(String key){
		long time=System.nanoTime()-timers.get(key);
		inc(key,time);
	}

	public JSONObject toJSON() throws JSONException{
		JSONObject obj=new JSONObject();
		obj.put("id",operation);
		JSONObject counters=new JSONObject();
		for(String key:valueSet.keySet()){
			counters.put(key,valueSet.get(key));
		}
		obj.put("counters",counters);
		JSONArray data=new JSONArray();
		for(QueryMetrics metric:children){
			data.put(metric.toJSON());
		}
		obj.put("children",data);
		return obj;
	}
}