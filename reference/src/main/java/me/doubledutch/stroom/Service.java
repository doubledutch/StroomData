package me.doubledutch.stroom;

import org.json.*;
import java.io.*;
import java.net.*;
import java.util.*;
import me.doubledutch.stroom.streams.*;

public abstract class Service implements Runnable{
	private Map<String,MockStreamConnection> mockMap=new HashMap<String,MockStreamConnection>();
	private StreamHandler streamHandler=null;

	private boolean isRunning=false;
	private boolean shouldBeRunning=false;
	private Thread thread=null;

	public Service(StreamHandler streamHandler){
		this.streamHandler=streamHandler;
	}

	private String getStreamName(URI stream){
		String path=stream.getPath();
		if(!path.startsWith("/stream/"))return null;
		return path.substring(path.lastIndexOf("/")+1); // TODO: possibly make smarter and less breakable
	}

	
	public boolean isRunning(){
		return isRunning;
	}

	public void isRunning(boolean value){
		isRunning=value;
	}	

	public boolean shouldBeRunning(){
		return shouldBeRunning;
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

	public abstract void run();

	public abstract JSONObject toJSON() throws JSONException;

	public StreamConnection openStream(URI stream) throws IOException{
		String scheme=stream.getScheme();
		String streamName=getStreamName(stream);
		if(scheme.equals("local")){
			String host=stream.getHost();
			if(host.equals("mock")){
				if(!mockMap.containsKey(streamName)){
					mockMap.put(streamName,new MockStreamConnection());
				}
				return mockMap.get(streamName);
			}else if(host.equals("direct")){
				return new LocalStreamConnection(streamHandler.getOrCreateStream(streamName));
			}
		}
		return null;
	}
}