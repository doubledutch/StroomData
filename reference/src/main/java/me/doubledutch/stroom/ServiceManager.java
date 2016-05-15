package me.doubledutch.stroom;

import org.apache.log4j.Logger;

import java.util.*;
import org.json.*;
import me.doubledutch.stroom.streams.*;
import me.doubledutch.stroom.filters.*;
import me.doubledutch.stroom.aggregates.*;

public class ServiceManager implements Runnable{
	private final Logger log = Logger.getLogger("ServiceManager");
	private static ServiceManager app;
	private StreamHandler streamHandler;
	private Map<String,Service> serviceMap=new HashMap<String,Service>();
	private Map<String,JSONObject> agg=new HashMap<String,JSONObject>();

	private StreamConnection serviceStream;

	public ServiceManager(StreamHandler handler) throws Exception{
		app=this;
		streamHandler=handler;
		// TODO: creating a stream connection like this feels slightly dirty,
		//       possibly refactor to make the service code generically usable
		serviceStream=new LocalStreamConnection(streamHandler.getOrCreateStream("_stroom_service"));
		loadState();
		createServices();
	}

	public static ServiceManager get(){
		return app;
	}

	public void start(){
		for(Service service:serviceMap.values()){
			try{
				service.start();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	public void stop(){
		for(Service service:serviceMap.values()){
			service.stop();
		}
	}

	public void run(){

	}

	public void addService(JSONObject obj) throws Exception{
		Service service=createService(obj);
		serviceStream.append(obj);
		serviceMap.put(obj.getString("id"),service);
		service.start();
	}

	public void updateService(String id,JSONObject obj) throws Exception{
		Service service=getService(id);
		service.stop();
		service=createService(obj);
		serviceStream.append(obj);
		serviceMap.put(id,service);
		service.start();

		// TODO: look into dependency tracking for a more complete reset
	}

	public void updateAndResetService(String id,JSONObject obj) throws Exception{
		Service service=getService(id);
		service.stop();
		service.reset();
		service=createService(obj);
		serviceStream.append(obj);
		serviceMap.put(id,service);
		service.start();

		// TODO: look into dependency tracking for a more complete reset
	}

	public void disableService(String id) throws Exception{
		Service service=getService(id);
		service.stop();
		service.setDisabled(true);
		JSONObject config=service.getConfiguration();
		serviceStream.append(config);
	}

	public void enableService(String id) throws Exception{
		Service service=getService(id);
		service.setDisabled(false);
		JSONObject config=service.getConfiguration();
		serviceStream.append(config);
		service.start();
	}

	public void restartService(String id) throws Exception{
		Service service=getService(id);
		service.stop();
		service.start();
	}

	public void stopService(String id) throws Exception{
		Service service=getService(id);
		service.stop();
	}

	public void startService(String id) throws Exception{
		Service service=getService(id);
		if(!service.isRunning()){
			service.start();
		}
	}

	public void resetService(String id) throws Exception{
		Service service=getService(id);
		service.stop();
		service.reset();
		service.start();
	}

	private Service getService(String id) throws Exception{
		return serviceMap.get(id);
	}

	private Service createService(JSONObject obj) throws Exception{
		String service=obj.getString("service").trim().toLowerCase();
		if(service.equals("filter")){
			return new FilterService(streamHandler,obj);
		}else if(service.equals("aggregate")){
			return new AggregateService(streamHandler,obj);
		}else{
			log.error("Unknown service type "+service);
		}
		return null;
	}

	private void createServices(){
		for(String id:agg.keySet()){
			try{
				Service service=createService(agg.get(id));
				serviceMap.put(id,service);
			}catch(Exception e){
				log.error("failed to create service for "+id);
				e.printStackTrace();
			}
		}
		agg=null;
	}

	private void processState(String document) throws JSONException{
		JSONObject obj=new JSONObject(document);
		if(!obj.has("id")){
			log.error("service stream object missing id!");
			log.error(obj.toString());
		}else{
			String id=obj.getString("id");
			agg.put(id,obj);
		}
	}

	private void loadState() throws Exception{
		long index=0;
		List<String> batch=serviceStream.get(index,index+100);
		while(batch.size()>0){
			index+=batch.size();
			for(String doc:batch){
				processState(doc);
			}
			batch=serviceStream.get(index,index+100);
		}
	}

	public JSONObject toJSON(String id) throws Exception{
		Service service=getService(id);
		return service.toJSON();
	}

	public JSONArray toJSON() throws JSONException{
		JSONArray result=new JSONArray();
		for(Service service:serviceMap.values()){
			result.put(service.toJSON());
		}
		return result;
	}
}