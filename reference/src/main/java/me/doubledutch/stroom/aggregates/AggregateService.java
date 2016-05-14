package me.doubledutch.stroom.aggregates;

import org.apache.log4j.Logger;

import me.doubledutch.stroom.streams.*;
import me.doubledutch.stroom.*;
import java.util.*;
import org.json.*;
import java.net.*;
import javax.script.*;

public class AggregateService extends Service{
	private final Logger log = Logger.getLogger("Aggregate");

	private int WAIT_TIME=1000;
	
	private long index=-1;
	private long outputIndex=-1;
	private String aggregate=null;

	public AggregateService(StreamHandler handler,JSONObject obj) throws Exception{
		super(handler,obj);		
	}

	private void loadState() throws Exception{
		JSONObject obj=new JSONObject(getStream("state").getLast());
		index=obj.getInt("i");
		outputIndex=obj.getInt("o");
	}

	private void saveState() throws Exception{
		JSONObject obj=new JSONObject();
		obj.put("i",index);
		obj.put("o",outputIndex);
		getStream("state").append(obj);
	}

	private void processDocument(String str) throws Exception{
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
			aggregate=out;

			// output.append(out);
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
					outputIndex=getStream("output").append(aggregate);
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