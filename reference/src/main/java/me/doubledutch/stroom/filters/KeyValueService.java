package me.doubledutch.stroom.filters;

import org.apache.log4j.Logger;

import me.doubledutch.stroom.perf.*;
import me.doubledutch.stroom.streams.*;
import me.doubledutch.stroom.*;
import me.doubledutch.stroom.client.StreamConnection;
import me.doubledutch.stroom.client.KVStoreConnection;
import java.util.*;
import java.util.concurrent.*;
import org.json.*;
import java.net.*;
import javax.script.*;
import java.io.*;

public class KeyValueService extends Service implements KVStoreConnection{
	private final Logger log = Logger.getLogger("Filter");

	private long outputIndex=-1;
	private double sampleRate=1.0;
	private BatchMetric metric=null;
	private JSONObject stateMap=null;

	// TODO: is this truly a good fit? isn't concurrenthashmap copy on write
	private Map<String,Long> keyMap=new ConcurrentHashMap<String,Long>();


	public String get(String key) throws IOException{
		return getStream("output").get(keyMap.get(key));
	}
	public List<String> list() throws IOException{
		List<String> result=new ArrayList<String>();
		for(String key:keyMap.keySet()){
			result.add(key);
		}
		return result;
	}

	private boolean keyMatches(String key,String pattern){
		// TODO: Crude for now implement good pattern support soon
		if(pattern.endsWith("*")){
			if(key.startsWith(pattern.substring(0,pattern.length()-1))){
				return true;
			}
		}else if(pattern.startsWith("*")){
			if(key.endsWith(pattern.substring(1))){
				return true;
			}
		}
		return false;
	}

	public List<String> search(String pattern) throws IOException{
		List<String> result=new ArrayList<String>();
		for(String key:keyMap.keySet()){
			if(keyMatches(key,pattern)){
				result.add(key);
			}
		}
		return result;
	}


	public KeyValueService(StreamHandler handler,JSONObject obj) throws Exception{
		super(handler,obj);
		String strType=obj.getString("type");
	}

	public int getKeyCount(){
		return keyMap.size();
	}

	public String getValue(String key) throws IOException{
		return getStream("output").get(keyMap.get(key));
	}

	private void loadState() throws Exception{
		long loc=0;
		List<String> batch=getStream("state").get(loc,loc+500);
		while(batch.size()>0){
			for(String str:batch){
				JSONObject obj=new JSONObject(str);
				index=obj.getLong("i");
				if(obj.has("o")){
					JSONObject objSt=obj.getJSONObject("o");
					Iterator<String> keyIt=objSt.keys();

					while(keyIt.hasNext()){
						String key=keyIt.next();
						if(objSt.isNull(key)){
							keyMap.remove(key);
						}else{
							keyMap.put(key,objSt.getLong(key));
						}
					}
				}
			}

			loc+=batch.size();
			batch=getStream("state").get(loc,loc+500);
		}

	}

	private void saveState() throws Exception{
		JSONObject obj=new JSONObject();
		obj.put("i",index);
		if(stateMap!=null){
			// obj.put("o",outputIndex);
			obj.put("o",stateMap);
		}
		getStream("state").append(obj,StreamConnection.FLUSH);
	}

	public void reset() throws Exception{
		getStream("state").truncate(0);
		getStream("output").truncate(0);
		index=-1;
		outputIndex=-1;
	}

	private String processDocument(String str) throws Exception{
		String out=null;
		if(type==HTTP){
			Map<String,String> headers=new HashMap<String,String>();
			headers.put("X-Stroom-Service",getId());
			headers.put("X-Stroom-Index",""+getIndex());
			out=Utility.postURL(url,str,headers);		
		}else if(type==JAVASCRIPT){
			metric.startTimer("javascript.derialize");
			jsEngine.put("raw",str);
			jsEngine.eval("var obj=JSON.parse(raw);");
			metric.stopTimer("javascript.derialize");

			// jsEngine.eval("var obj="+str+";");
			metric.startTimer("javascript.run");
			jsEngine.eval("var result=map(obj);");
			metric.stopTimer("javascript.run");
			metric.startTimer("javascript.serialize");
			jsEngine.eval("if(result!=null)result=JSON.stringify(result);");
			Object obj=jsEngine.eval("result");
			metric.stopTimer("javascript.serialize");

			if(obj!=null){
				out=(String)obj;
			}else{
				out="";
			}
			// TODO: handle javascript errors
		}else if(type==QUERY){

		}
		if(out==null){
			// Assume error
		}else if(out.trim().length()==0){
			// Assume output not intended
		}else{
			// Send output along
			// getStream("output").append(out);
		}
		return out;
	}

	public void run(){
		try{
			if(getStream("state").getCount()>0){
				loadState();
			}
			log.info(getId()+" restarting at "+(index+1));
			isRunning(true);
			while(shouldBeRunning()){
				// Load
				metric=new BatchMetric();
				metric.startTimer("batch.time");
				metric.startTimer("input.get");
				List<String> batch=getStream("input").get(index+1,index+getBatchSize()+1);
				metric.stopTimer("input.get");
				metric.setSamples(batch.size());
				// Process
				if(batch.size()==0){
					// No new data, wait before pulling again
					try{
						Thread.sleep(getWaitTime());
					}catch(Exception se){}
				}else{
					List<String> output=new ArrayList<String>();
					for(String str:batch){
						// TODO: add selective error handling here!
						String out=processDocument(str);
						if(out!=null && out.length()>0){
							if(out.startsWith("[")){
								JSONArray arr=new JSONArray(out);
								for(int i=0;i<arr.length();i++){
									JSONObject obj=arr.getJSONObject(i);
									output.add(obj.toString());
								}
							}else{
								output.add(out);
							}
						}
						index++;
					}
					metric.startTimer("output.append");
					if(output.size()>0){
						// Separate keys from values
						List<String> removeList=new ArrayList<String>();
						List<String> keyList=new ArrayList<String>(output.size());
						List<String> valueList=new ArrayList<String>(output.size());
						for(String str:output){
							JSONObject obj=new JSONObject(str);
							if(obj.has("key") && obj.has("value")){
								Object keyObj=obj.get("key");
								String key=null;
								if(keyObj instanceof String){
									key=(String)keyObj;
								}else{
									key=keyObj.toString();
								}
								if(obj.isNull("value")){
									// Remove key
									removeList.add(key);
								}else{
									keyList.add(key);
									JSONObject value=obj.getJSONObject("value");
									valueList.add(value.toString());
								}
							}
						}
						// store values
						JSONObject map=new JSONObject();
						if(valueList.size()>0){
							List<Long> result=getStream("output").append(valueList);

							// match keys with locations and store in state
							
							for(int i=0;i<result.size();i++){
								String key=keyList.get(i);
								long loc=result.get(i);
								map.put(key,loc);
								keyMap.put(key,loc);
							}
							outputIndex=result.get(result.size()-1);
						}
						for(String key:removeList){
							map.put(key,JSONObject.NULL);
							keyMap.remove(key);
						}
						stateMap=map;
						
					}
					metric.stopTimer("output.append");
					// TODO: add selective state saving point
					metric.startTimer("state.append");
					saveState();
					stateMap=null;
					metric.stopTimer("state.append");
				}
				metric.stopTimer("batch.time");
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
		return obj;
	}
}