package me.doubledutch.stroom;

import java.util.*;
import java.util.concurrent.*;
import org.json.*;
import me.doubledutch.stroom.aggregates.*;

public class AggregateManager{
	private static AggregateManager app=null;

	private Map<String,AggregateService> aggregates=new ConcurrentHashMap<String,AggregateService>();
	private Map<String,PartitionedAggregateService> partitionedAggregates=new ConcurrentHashMap<String,PartitionedAggregateService>();

	public AggregateManager(){
		app=this;
	}

	public static AggregateManager get(){
		return app;
	}

	public void addAggregate(AggregateService service){
		aggregates.put(service.getId(),service);
	}

	public void addPartitionedAggregate(PartitionedAggregateService service){
		partitionedAggregates.put(service.getId(),service);
	}

	public JSONArray list() throws JSONException{
		JSONArray result=new JSONArray();
		for(AggregateService service:aggregates.values()){
			JSONObject obj=new JSONObject();
			obj.put("id",service.getId());
			obj.put("partition_key",JSONObject.NULL);
			obj.put("partitions",1);
			result.put(obj);
		}
		for(PartitionedAggregateService service:partitionedAggregates.values()){
			JSONObject obj=new JSONObject();
			obj.put("id",service.getId());
			obj.put("partition_key",service.getPartitionKey());
			obj.put("partitions",service.getPartitionCount());
			result.put(obj);
		}
		return result;
	}

	public String getAggregate(String id) throws Exception{
		if(aggregates.containsKey(id)){
			return aggregates.get(id).getAggregate();
		}
		return null;
	}

	public String getAggregate(String id,String partition) throws Exception{
		if(partitionedAggregates.containsKey(id)){
			return partitionedAggregates.get(id).getAggregate(partition);
		}
		return null;
	}
}