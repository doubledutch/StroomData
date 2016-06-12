package me.doubledutch.stroom.query;

import me.doubledutch.stroom.streams.*;
import me.doubledutch.stroom.query.sql.*;
import me.doubledutch.stroom.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class QueryRunner implements Runnable{
	ExecutorService threadPool=null;
	ExecutorService dataThreadPool=null;
	StreamHandler streams=null;

	public QueryRunner(StreamHandler streams){
		this.streams=streams;
		threadPool=Executors.newCachedThreadPool();
		dataThreadPool=Executors.newCachedThreadPool();
	}

	public void run(){

	}

	public void runTask(Runnable task) throws Exception{
		// TODO: bad naming, fix
		dataThreadPool.execute(task);
	}

	public void runQuery(Query q) throws Exception{
		if(q.getType()==Query.SQL){
			SQLRunner task=new SQLRunner(q,this);
			threadPool.execute(task);
		}else{
			// ?
		}
	}

	private String getStreamName(URI stream){
		String path=stream.getPath();
		if(!path.startsWith("/stream/"))return null;
		return path.substring(path.lastIndexOf("/")+1); // TODO: possibly make smarter and less breakable
	}


	public StreamConnection openStream(URI stream) throws IOException{
		String scheme=stream.getScheme();
		String streamName=getStreamName(stream);
		if(scheme.equals("local")){
			String host=stream.getHost();
			if(host.equals("direct")){
				return new LocalStreamConnection(streams.getOrCreateStream(streamName));
			}
		}
		return null;
	}
}