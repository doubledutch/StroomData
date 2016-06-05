package me.doubledutch.stroom.filters;

import org.apache.log4j.Logger;

import me.doubledutch.stroom.perf.*;
import me.doubledutch.stroom.streams.*;
import me.doubledutch.stroom.*;
import java.util.*;
import org.json.*;
import java.net.*;
import javax.script.*;

public class FilterService extends Service{
	private final Logger log = Logger.getLogger("Filter");

	private long outputIndex=-1;
	private double sampleRate=1.0;
	private BatchMetric metric=null;

	public FilterService(StreamHandler handler,JSONObject obj) throws Exception{
		super(handler,obj);
		
		String strType=obj.getString("type");
		if(strType.equals("sample")){
			type=SAMPLE;
			sampleRate=obj.getDouble("sample_rate");
		}
	}

	private void loadState() throws Exception{
		JSONObject obj=new JSONObject(getStream("state").getLast());
		index=obj.getLong("i");
		outputIndex=obj.getLong("o");
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
	}

	private String processDocument(String str) throws Exception{
		// System.out.println("Process document");
		// TODO: add ability to batch output
		String out=null;
		if(type==HTTP){
			out=Utility.postURL(url,str);		
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

		}else if(type==SAMPLE){
			if(Math.random()<sampleRate){
				out=str;
			}else{
				out="";
			}
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
							output.add(out);
						}
						index++;
					}
					metric.startTimer("output.append");
					if(output.size()>0){
						List<Long> result=getStream("output").append(output);
						outputIndex=result.get(result.size()-1);
					}
					metric.stopTimer("output.append");
					// TODO: add selective state saving point
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