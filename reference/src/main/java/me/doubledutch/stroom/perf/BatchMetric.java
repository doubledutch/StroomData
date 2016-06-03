package me.doubledutch.stroom.perf;

import java.util.*;
import org.json.*;

public class BatchMetric{
	private Map<String,Long> values=new HashMap<String,Long>();
	private Map<String,Long> timers=new HashMap<String,Long>();
	private int samples=0;

	public void set(String key,long value){
		values.put(key,value);
	}

	public void inc(String key){
		if(!values.containsKey(key)){
			values.put(key,0l);
		}
		values.put(key,values.get(key)+1);
	}

	public void inc(String key, long value){
		if(!values.containsKey(key)){
			values.put(key,0l);
		}
		values.put(key,values.get(key)+value);
	}

	public void startTimer(String key){
		timers.put(key,System.nanoTime());
	}

	public void stopTimer(String key){
		long time=System.nanoTime()-timers.get(key);
		inc(key,time);
	}

	public void setSamples(int samples){
		this.samples=samples;
	}

	public double getRate(){
		return samples/((double)values.get("batch.time")/1000000000l);
	}

	public JSONObject toJSON() throws JSONException{
		JSONObject obj=new JSONObject();
		for(String key:values.keySet()){
			long value=values.get(key);
			JSONObject result=new JSONObject();
			result.put("sum",value);
			if(samples>0){
				result.put("avg",((double)value)/samples);
			}
			obj.put(key,result);
		}
		return obj;
	}
}