package me.doubledutch.stroom;

import java.io.*;
import java.net.*;

import me.doubledutch.stroom.streams.*;
import me.doubledutch.stroom.client.StreamConnection;

public class ScriptAPI{
	private StreamHandler streamHandler;

	public ScriptAPI(StreamHandler handler){
		this.streamHandler=handler;
	}

	private String getStreamName(URI stream){
		String path=stream.getPath();
		if(!path.startsWith("/stream/"))return null;
		return path.substring(path.lastIndexOf("/")+1); // TODO: possibly make smarter and less breakable
	}

	public StreamConnection openStream(String name) throws Exception{
		if(name.indexOf("://")==-1){
			// TODO: make this way less hacky
			name="local://direct/stream/"+name;
		}
		URI stream=new URI(name);
		String scheme=stream.getScheme();
		String streamName=getStreamName(stream);
		if(scheme.equals("local")){
			String host=stream.getHost();
			if(host.equals("direct")){
				return new LocalStreamConnection(streamHandler.getOrCreateStream(streamName));
			}
		}
		return null;
	}
}