package me.doubledutch.stroom;

import java.util.*;
import java.util.concurrent.*;
import org.json.*;
import me.doubledutch.stroom.filters.*;

public class KeyValueManager{
	private static KeyValueManager app=null;

	private Map<String,KeyValueService> kvstores=new ConcurrentHashMap<String,KeyValueService>();
	// private Map<String,PartitionedAggregateService> partitionedAggregates=new ConcurrentHashMap<String,PartitionedAggregateService>();

	public KeyValueManager(){
		app=this;
	}

	public static KeyValueManager get(){
		return app;
	}

	public void addKeyValueService(KeyValueService service){
		kvstores.put(service.getId(),service);
	}

	public JSONArray list() throws JSONException{
		JSONArray result=new JSONArray();
		for(KeyValueService service:kvstores.values()){
			JSONObject obj=new JSONObject();
			obj.put("id",service.getId());
			obj.put("keys",service.getKeyCount());
			result.put(obj);
		}
		
		return result;
	}

	public KeyValueService get(String name){
		return kvstores.get(name);
	}

	public String getValue(String id,String key) throws Exception{
		if(kvstores.containsKey(id)){
			return kvstores.get(id).getValue(key);
		}
		return null;
	}
}