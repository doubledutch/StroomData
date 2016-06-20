package me.doubledutch.stroom.aggregates;

import org.apache.log4j.Logger;

import me.doubledutch.stroom.streams.*;
import me.doubledutch.stroom.perf.*;
import me.doubledutch.stroom.*;
import java.util.*;
import me.doubledutch.stroom.client.StreamConnection;
import org.json.*;
import java.net.*;
import javax.script.*;

public class PartitionedAggregateService extends Service{
	private final Logger log = Logger.getLogger("Aggregate");

	private long outputIndex=-1;
	private BatchMetric metric=null;
	private String partition=null;

	private Map<String,Long> aggregateMap=new HashMap<String,Long>();
	// JSONObject newState=new JSONObject();
	private Map<String,String> aggregateCache=new HashMap<String,String>();
	private Set<String> partitionTouchSet=new HashSet<String>();


	public PartitionedAggregateService(StreamHandler handler,JSONObject obj) throws Exception{
		super(handler,obj);		
		partition=obj.getString("partition_key");
	}

	public String getAggregate(String key) throws Exception{
		//
		if(aggregateCache.containsKey(key)){
			return aggregateCache.get(key);
		}
		if(!aggregateMap.containsKey(key)){
			return null;
		}
		

		long loc=aggregateMap.get(key);
		String doc=getStream("output").get(loc);
		aggregateCache.put(key,doc);
		return doc;
	}

	private void loadState() throws Exception{
		long loc=0;
		List<String> batch=getStream("state").get(loc,loc+500);
		while(batch.size()>0){
			for(String str:batch){
				JSONObject obj=new JSONObject(str);
				index=obj.getLong("i");
				JSONObject objSt=obj.getJSONObject("o");
				Iterator<String> keyIt=objSt.keys();

				while(keyIt.hasNext()){
					String key=keyIt.next();
					aggregateMap.put(key,objSt.getLong(key));
				}
			}

			loc+=batch.size();
			batch=getStream("state").get(loc,loc+500);
		}
	}

	private void saveState() throws Exception{
		JSONObject obj=new JSONObject();
		obj.put("i",index);

		// Save snapshot and build state
		List<String> batch=new ArrayList<String>();
		List<String> pKeyList=new ArrayList<String>();
		for(String key:partitionTouchSet){
			pKeyList.add(key);
			String data=aggregateCache.get(key);
			batch.add(data);
		}
		
		metric.startTimer("output.append");
		List<Long> results=null;
		if(batch.size()>0){
			results=getStream("output").append(batch,StreamConnection.FLUSH);
		}
		metric.stopTimer("output.append");
		JSONObject newState=new JSONObject();
		if(results!=null){
			for(int i=0;i<pKeyList.size();i++){
				String key=pKeyList.get(i);
				long loc=results.get(i);
				newState.put(key,loc);
			}
		}
		obj.put("o",newState);
		getStream("state").append(obj,StreamConnection.FLUSH);
		partitionTouchSet.clear();
	}
	

	public void reset() throws Exception{
		getStream("state").truncate(0);
		getStream("output").truncate(0);
		index=-1;
		outputIndex=-1;
	}

	public String getPartitionKey(){
		return partition;
	}

	public int getPartitionCount(){
		return aggregateMap.size();
	}

	private String getPartitionKey(String event) throws JSONException,ScriptException{
		Object value=null;
		if(partition==null||partition.trim().length()==0){
			// Assume javascript based function
			metric.startTimer("javascript.deserialize");
			jsEngine.put("raw",event);
			jsEngine.eval("var obj=JSON.parse(raw);");
			metric.stopTimer("javascript.deserialize");
			metric.startTimer("javascript.run");
			jsEngine.eval("var key=getPartitionKey(obj);");
			metric.stopTimer("javascript.run");
			metric.stopTimer("javascript.serialize");
			value=jsEngine.get("key");
			metric.stopTimer("javascript.serialize");
			
		
		}else if(partition.startsWith("http://")||partition.startsWith("https://")){
			// TODO: implement http based partition key
		}else{
			JSONObject obj=new JSONObject(event);
			value=Utility.pickValue(obj,partition);	
		}
		if(value!=null){
			if(value instanceof String){
				return (String)value;
			}
			// System.out.println(value.toString());
			return value.toString();
		}
		return null;
	}

	private String processDocument(String str,String aggregate) throws Exception{
		// System.out.println("Process document");
		// TODO: add ability to batch output
		String out=null;
		boolean error=false;
		if(type==HTTP){
			JSONObject outputObj=new JSONObject();
			outputObj.put("event",new JSONObject(str));
			if(aggregate!=null){
				outputObj.put("aggregate",new JSONObject(aggregate));
			}else{
				outputObj.put("aggregate",JSONObject.NULL);
			}
			metric.startTimer("http.post");
			out=Utility.postURL(url,outputObj.toString());		
			metric.stopTimer("http.post");
		}else if(type==JAVASCRIPT){
			metric.startTimer("javascript.deserialize");
			if(!(partition==null||partition.trim().length()==0)){
				// Only do this if we haven't already done it for the partition key
				jsEngine.put("raw",str);
				jsEngine.eval("var obj=JSON.parse(raw);");
			}
			// TODO: only when it's actually a new aggregate should it be put back in!
			jsEngine.put("raw",aggregate);
			jsEngine.eval("var aggregate=JSON.parse(raw);");
			// jsEngine.eval("var obj=JSON.parse('"+str+"');");
			metric.stopTimer("javascript.deserialize");
			// jsEngine.eval("var aggregate="+aggregate+";");
			// jsEngine.eval("var result=reduce(aggregate,obj);");
			metric.startTimer("javascript.run");
			jsEngine.eval("aggregate=reduce(aggregate,obj);");
			metric.stopTimer("javascript.run");

			
			metric.startTimer("javascript.serialize");
			jsEngine.eval("var result=null");
			jsEngine.eval("if(aggregate!=null)result=JSON.stringify(aggregate);");
			// Object obj=jsEngine.eval("result");
			Object obj=jsEngine.get("result");
			metric.stopTimer("javascript.serialize");
			if(obj!=null){
				out=(String)obj;
			}
			
			// TODO: handle javascript errors
		}else if(type==QUERY){

		}
		if(out==null){
			// Assume error
		}else{
			// Send output along
			// aggregate=out;

			// output.append(out);
		}
		return out;
	}

	public void run(){
		try{
			if(type==JAVASCRIPT){
				jsEngine.eval("var aggregate=null");
			}
			if(getStream("state").getCount()>0){
				loadState();
			}
			log.info(getId()+" restarting at "+(index+1));
			isRunning(true);
			while(shouldBeRunning()){
				metric=new BatchMetric();
				metric.startTimer("batch.time");
				// Load
				metric.startTimer("input.get");
				List<String> batch=getStream("input").get(index+1,index+getBatchSize()+1);
				metric.stopTimer("input.get");
				metric.setSamples(batch.size());

				// Process
				if(batch.size()==0){
					metric.stopTimer("batch.time");
					// No new data, wait before pulling again
					try{
						Thread.sleep(getWaitTime());
					}catch(Exception se){}
				}else{
					// TODO: huge performance improvement... sort events in the batch by partition key
					//       this lets us keep the aggregate in the javascript engine for each partition key
					//       in the batch
					// newState=new JSONObject();
					for(String str:batch){
						// TODO: add selective error handling here!
						String key=getPartitionKey(str);
						String newAggregate=processDocument(str,getAggregate(key));
						// metric.startTimer("output.append");
						// long loc=getStream("output").append(newAggregate);
						// long loc=0;
						// metric.stopTimer("output.append");
						// aggregateMap.put(key,loc);
						// newState.put(key,loc);
						// processDocument(str);
						aggregateCache.put(key,newAggregate);
						partitionTouchSet.add(key);
						index++;
					}
					// TODO: add selective state saving point
					
					// getAggregate();
					// outputIndex=getStream("output").append(aggregate);
					
					metric.startTimer("state.append");
					saveState();
					metric.stopTimer("state.append");
					metric.stopTimer("batch.time");
				}
				addBatchMetric(metric);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		try{
			saveState();
		}catch(Exception e){
			e.printStackTrace();
		}
		isRunning(false);
	}

	public JSONObject toJSON() throws JSONException{
		JSONObject obj=super.toJSON();
		obj.put("partitions",aggregateMap.size());
		return obj;
	}
}

