package me.doubledutch.stroom.aggregates;

import org.apache.log4j.Logger;

import me.doubledutch.stroom.streams.*;
import me.doubledutch.stroom.perf.*;
import me.doubledutch.stroom.*;
import me.doubledutch.stroom.client.StreamConnection;
import java.util.*;
import org.json.*;
import java.net.*;
import javax.script.*;

public class AggregateService extends Service{
	private final Logger log = Logger.getLogger("Aggregate");

	private long outputIndex=-1;
	private String aggregate=null;
	private BatchMetric metric=null;

	public AggregateService(StreamHandler handler,JSONObject obj) throws Exception{
		super(handler,obj);		
	}

	public String getAggregate() throws Exception{
		if(type==JAVASCRIPT){
			metric.startTimer("javascript.serialize");
			jsEngine.eval("var result=null");
			jsEngine.eval("if(aggregate!=null)result=JSON.stringify(aggregate);");
			// Object obj=jsEngine.eval("result");
			Object obj=jsEngine.get("result");
			metric.stopTimer("javascript.serialize");
			if(obj!=null){
				aggregate=(String)obj;
			}
		}
		return aggregate;
	}

	private void loadState() throws Exception{
		JSONObject obj=new JSONObject(getStream("state").getLast());
		index=obj.getLong("i");
		outputIndex=obj.getLong("o");
		aggregate=getStream("output").get(outputIndex);
		if(type==JAVASCRIPT){
			jsEngine.eval("var aggregate="+aggregate+";");
		}
	}

	private void saveState() throws Exception{
		JSONObject obj=new JSONObject();
		obj.put("i",index);
		obj.put("o",outputIndex);
		getStream("state").append(obj,StreamConnection.FLUSH);
	}

	public void reset() throws Exception{
		getStream("state").truncate(0);
		getStream("output").truncate(0);
		index=-1;
		outputIndex=-1;
		aggregate=null;
	}

	private void processDocument(String str) throws Exception{
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
			Map<String,String> headers=new HashMap<String,String>();
			headers.put("X-Stroom-Service",getId());
			headers.put("X-Stroom-Index",""+getIndex());
			out=Utility.postURL(url,outputObj.toString(),headers);		
			metric.stopTimer("http.post");
		}else if(type==JAVASCRIPT){
			metric.startTimer("javascript.deserialize");
			jsEngine.put("raw",str);
			jsEngine.eval("var obj=JSON.parse(raw);");
			// jsEngine.eval("var obj=JSON.parse('"+str+"');");
			metric.stopTimer("javascript.deserialize");
			// jsEngine.eval("var aggregate="+aggregate+";");
			// jsEngine.eval("var result=reduce(aggregate,obj);");
			metric.startTimer("javascript.run");
			jsEngine.eval("aggregate=reduce(aggregate,obj);");
			metric.stopTimer("javascript.run");

			/*
			metric.startTimer("javascript.serialize");
			jsEngine.eval("var result=null");
			jsEngine.eval("if(aggregate!=null)result=JSON.stringify(aggregate);");
			Object obj=jsEngine.eval("result");
			metric.stopTimer("javascript.serialize");
			if(obj!=null){
				out=(String)obj;
			}
			*/
			// TODO: handle javascript errors
		}else if(type==QUERY){

		}
		if(out==null){
			// Assume error
		}else{
			// Send output along
			aggregate=out;

			// output.append(out);
		}
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
					for(String str:batch){
						// TODO: add selective error handling here!
						processDocument(str);
						index++;
					}
					// TODO: add selective state saving point
					metric.startTimer("output.append");
					getAggregate();
					outputIndex=getStream("output").append(aggregate,StreamConnection.FLUSH);
					metric.stopTimer("output.append");
					metric.startTimer("state.append");
					saveState();
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