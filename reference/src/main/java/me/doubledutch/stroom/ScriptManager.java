package me.doubledutch.stroom;

import org.apache.log4j.Logger;

import java.util.*;
import org.json.*;
import me.doubledutch.stroom.streams.*;
import me.doubledutch.stroom.filters.*;
import me.doubledutch.stroom.aggregates.*;
import me.doubledutch.stroom.client.StreamConnection;

public class ScriptManager implements Runnable{
	private final Logger log = Logger.getLogger("ScriptManager");
	private static ScriptManager app;
	private StreamHandler streamHandler;
	private Map<String,String> agg=new HashMap<String,String>();

	private StreamConnection scriptStream;

	public ScriptManager(StreamHandler handler) throws Exception{
		app=this;
		streamHandler=handler;
		// TODO: creating a stream connection like this feels slightly dirty,
		//       possibly refactor to make the service code generically usable
		scriptStream=new LocalStreamConnection(streamHandler.getOrCreateStream("_stroom_scripts"));
		loadState();
	}

	public static ScriptManager get(){
		return app;
	}

	public void run(){

	}

	public void deleteScript(String name) throws Exception{
		JSONObject obj=new JSONObject();
		obj.put("name",name);
		obj.put("disabled",true);
		scriptStream.append(obj);
		agg.remove(name);
	}

	public void setScript(String name,String script) throws Exception{
		JSONObject obj=new JSONObject();
		obj.put("name",name);
		obj.put("disabled",false);
		obj.put("script",script);
		scriptStream.append(obj);

		agg.put(name,script);
	}

	public String getScript(String name){
		return agg.get(name);
	}

	private void processState(String document) throws JSONException{
		JSONObject obj=new JSONObject(document);
		if(!obj.has("name")){
			log.error("script stream object missing name!");
			log.error(obj.toString());
		}else{
			String id=obj.getString("name");
			if(obj.has("disabled")){
				if(obj.getBoolean("disabled")){
					agg.remove(obj.getString("name"));
					return;
				}
			}
			agg.put(id,obj.getString("script"));
		}
	}

	private void loadState() throws Exception{
		long index=0;
		List<String> batch=scriptStream.get(index,index+100);
		while(batch.size()>0){
			index+=batch.size();
			for(String doc:batch){
				processState(doc);
			}
			batch=scriptStream.get(index,index+100);
		}
	}

	public JSONObject toJSON(String id) throws JSONException{
		JSONObject result=new JSONObject();
		result.put("name",id);
		String script=getScript(id);
		result.put("size",script.length());
		// TODO: add dependency tracking here!
		return result;
	}

	public JSONArray toJSON() throws JSONException{
		JSONArray result=new JSONArray();
		for(String key:agg.keySet()){
			result.put(toJSON(key));
		}
		return result;
	}
}