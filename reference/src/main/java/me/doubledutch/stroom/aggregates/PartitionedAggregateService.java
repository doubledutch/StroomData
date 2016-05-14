package me.doubledutch.stroom.aggregates;

import org.apache.log4j.Logger;

import me.doubledutch.stroom.streams.*;
import me.doubledutch.stroom.*;
import java.util.*;
import org.json.*;
import java.io.IOException;
import java.net.*;
import javax.script.*;

public class PartitionedAggregateService extends Service{
	private final Logger log = Logger.getLogger("PartitionedAggregate");

	private int WAIT_TIME=1000;
	private int BATCH_SIZE=100;
	// private int SNAPSHOT_RATE=100;

	public static int HTTP=0;
	public static int QUERY=1;
	public static int JAVASCRIPT=2;

	private int type=HTTP;

	private String url=null;
	private double sampleRate=1.0;

	private StreamConnection input=null;
	private StreamConnection output=null;
	private StreamConnection state=null;

	private long index=-1;
	private long outputIndex=-1;
	private Map<String,Long> aggregateMap=new HashMap<String,Long>();
	JSONObject newState=new JSONObject();

	private String id;
	private String partition;
	private String script=null;

	private ScriptEngine jsEngine;
	private Invocable jsInvocable;

	public PartitionedAggregateService(StreamHandler handler,JSONObject obj) throws Exception{
		super(handler,obj);
		
		id=obj.getString("id");
		partition=obj.getString("partition");
		if(obj.has("batch_size")){
			setBatchSize(obj.getInt("batch_size"));
		}
		
		input=openStream(new URI(obj.getString("input_stream")));
		output=openStream(new URI(obj.getString("output_stream")));
		if(obj.has("state_stream")){
			state=openStream(new URI(obj.getString("state_stream")));
		}else{
			state=openStream(new URI(obj.getString("output_stream")+".state"));
		}
		String strType=obj.getString("type");
		if(strType.equals("http")){
			type=HTTP;
			url=obj.getString("url");
		}else if(strType.equals("javascript")){
			type=JAVASCRIPT;
			script=obj.getString("script");
			String scriptData=Utility.readFile(script);
			ScriptEngineManager mgr = new ScriptEngineManager();
	        jsEngine = mgr.getEngineByName("JavaScript");
	        // jsEngine = mgr.getEngineByName("nashorn");
	        jsInvocable = (Invocable) jsEngine;
			jsEngine.eval(scriptData);
		}
	}

	private void setBatchSize(int size){
		BATCH_SIZE=size;
	}

	private void loadState() throws Exception{
		long loc=0;
		List<String> batch=state.get(loc,loc+BATCH_SIZE);
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
			batch=state.get(loc,loc+BATCH_SIZE);
		}

	}

	private void saveState() throws Exception{
		JSONObject obj=new JSONObject();
		obj.put("i",index);
		obj.put("o",newState);
		state.append(obj);
	}

	private String getLastAggregate(String partitionKey) throws IOException{
		long loc=aggregateMap.get(partitionKey);
		String doc=output.get(loc);
		return doc;
	}

	private String getPartitionKey(String event) throws JSONException{
		JSONObject obj=new JSONObject(event);
		Object value=Utility.pickValue(obj,partition);
		if(value instanceof String){
			return (String)value;
		}
		return value.toString();
	}

	private String processDocument(String str,String aggregate) throws Exception{
		// System.out.println("Process document");
		// TODO: add ability to batch output
		String out=null;
		if(type==HTTP){
			JSONObject outputObj=new JSONObject();
			outputObj.put("event",new JSONObject(str));
			if(aggregate!=null){
				outputObj.put("aggregate",new JSONObject(aggregate));
			}else{
				outputObj.put("aggregate",JSONObject.NULL);
			}
			out=Utility.postURL(url,outputObj.toString());		
		}else if(type==JAVASCRIPT){
			jsEngine.eval("var obj="+str+";");
			jsEngine.eval("var aggregate="+aggregate+";");
			jsEngine.eval("var result=reduce(aggregate,obj);");
			jsEngine.eval("if(result!=null)result=JSON.stringify(result);");
			Object obj=jsEngine.eval("result");
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
			return out;

			// output.append(out);
		}
		return null;
	}

	public void run(){
		try{
			if(state.getCount()>0){
				loadState();
			}
			log.info(id+" restarting at "+(index+1));
			isRunning(true);
			while(shouldBeRunning()){
				// Load
				List<String> batch=input.get(index+1,index+BATCH_SIZE+1);
				// Process
				if(batch.size()==0){
					// No new data, wait before pulling again
					try{
						Thread.sleep(WAIT_TIME);
					}catch(Exception se){}
				}else{
					newState=new JSONObject();
					for(String str:batch){
						// TODO: add selective error handling here!
						String key=getPartitionKey(str);
						String newAggregate=processDocument(str,getLastAggregate(key));
						long loc=output.append(newAggregate);
						aggregateMap.put(key,loc);
						newState.put(key,loc);
						index++;
					}
					saveState();
				}
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
		JSONObject obj=new JSONObject();
		return obj;
	}
}