package me.doubledutch.stroom;

import org.json.*;
import java.io.*;
import java.net.*;
import java.util.*;
import me.doubledutch.stroom.streams.*;

public class Service{
	private Map<String,MockStreamConnection> mockMap=new HashMap<String,MockStreamConnection>();
	private StreamHandler streamHandler=null;

	public Service(StreamHandler streamHandler){
		this.streamHandler=streamHandler;
	}

	private String getStreamName(URI stream){
		String path=stream.getPath();
		if(!path.startsWith("/streams/"))return null;
		return path.substring(9); // TODO: // possibly make smarter and less breakable
	}

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