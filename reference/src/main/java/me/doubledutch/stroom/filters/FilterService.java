package me.doubledutch.stroom.filters;

import org.apache.log4j.Logger;

import me.doubledutch.stroom.streams.*;
import me.doubledutch.stroom.*;
import java.util.*;
import org.json.*;
import java.net.*;
import javax.script.*;
/*
	One at a time, at least once, at most once
	multi threaded, low guarantee
	stochastic sampling

	external url
	internal javascript script
	internal sql query

	load data
	execute filter
	write response
	write state

	how do we handle errors?
*/

public class FilterService extends Service{
	private final Logger log = Logger.getLogger("Filter");

	private int WAIT_TIME=1000;

	private double sampleRate=1.0;

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
		index=obj.getInt("i");
	}

	private void saveState() throws Exception{
		JSONObject obj=new JSONObject();
		obj.put("i",index);
		getStream("state").append(obj);
	}

	public void reset() throws Exception{
		getStream("state").truncate(0);
		getStream("output").truncate(0);
		index=-1;
	}

	private void processDocument(String str) throws Exception{
		// System.out.println("Process document");
		// TODO: add ability to batch output
		String out=null;
		if(type==HTTP){
			out=Utility.postURL(url,str);		
		}else if(type==JAVASCRIPT){
			jsEngine.eval("var obj="+str+";");
			jsEngine.eval("var result=map(obj);");
			jsEngine.eval("if(result!=null)result=JSON.stringify(result);");
			Object obj=jsEngine.eval("result");
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
			getStream("output").append(out);
		}
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
				List<String> batch=getStream("input").get(index+1,index+getBatchSize()+1);
				// Process
				if(batch.size()==0){
					// No new data, wait before pulling again
					try{
						Thread.sleep(WAIT_TIME);
					}catch(Exception se){}
				}else{
					for(String str:batch){
						// TODO: add selective error handling here!
						processDocument(str);
						index++;
					}
					// TODO: add selective state saving point
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
		JSONObject obj=super.toJSON();
		return obj;
	}
}