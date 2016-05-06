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

public class FilterService extends Service implements Runnable{
	private final Logger log = Logger.getLogger("Filter");

	private int WAIT_TIME=1000;
	private int BATCH_SIZE=100;

	public static int HTTP=0;
	public static int QUERY=1;
	public static int JAVASCRIPT=2;
	public static int SAMPLE=3;

	private int type=HTTP;

	private String url=null;
	private double sampleRate=1.0;

	private StreamConnection input=null;
	private StreamConnection output=null;
	private StreamConnection state=null;

	private boolean isRunning=false;
	private boolean shouldBeRunning=false;
	private Thread thread=null;

	private long index=-1;

	private String id;
	private String script=null;

	private ScriptEngine jsEngine;
	private Invocable jsInvocable;

	public FilterService(StreamHandler handler,JSONObject obj) throws Exception{
		super(handler);
		
		id=obj.getString("id");

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
		}else if(strType.equals("sample")){
			type=SAMPLE;
			sampleRate=obj.getDouble("sample_rate");
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

		// JSONObject filter=obj.getJSONObject("filter");
	}

	private void setBatchSize(int size){
		BATCH_SIZE=size;
	}

	public void start(){
		shouldBeRunning=true;
		thread=new Thread(this);
		thread.start();
	}

	public void stop(){
		shouldBeRunning=false;
		while(isRunning){
			try{
				Thread.sleep(50);
			}catch(Exception e){}
		}
	}

	private void loadState() throws Exception{
		JSONObject obj=new JSONObject(state.getLast());
		index=obj.getInt("i");
	}

	private void saveState() throws Exception{
		JSONObject obj=new JSONObject();
		obj.put("i",index);
		state.append(obj);
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
			output.append(out);
		}
	}

	public void run(){
		try{
			if(state.getCount()>0){
				loadState();
			}
			log.info(id+" restarting at "+(index+1));
			isRunning=true;
			while(shouldBeRunning){
				// Load
				List<String> batch=input.get(index+1,index+BATCH_SIZE+1);
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
		isRunning=false;
	}

	public JSONObject toJSON() throws JSONException{
		JSONObject obj=new JSONObject();
		return obj;
	}
}