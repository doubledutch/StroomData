package me.doubledutch.stroom.streams;

import java.util.concurrent.*;
import java.util.*;
import java.io.*;
import org.json.*;

public class StreamHandler{
	private String parentFolder;
	private ConcurrentHashMap<String,Stream> streamMap;
	private int MAX_COMMIT_BATCH;
	private int MAX_COMMIT_LAG;
	private long MAX_BLOCK_SIZE=32*1024*1024*1024l; // 32 gb block files
	private long MAX_INDEX_SIZE=50*1000*1000l; // close to 1gb index blocks

	public StreamHandler(JSONObject config) throws JSONException{
		MAX_COMMIT_BATCH=config.getInt("commit_batch_size");
		MAX_COMMIT_LAG=config.getInt("commit_batch_timeout");
		if(config.has("block_size")){
			MAX_BLOCK_SIZE=config.getLong("block_size");
		}
		if(config.has("index_size")){
			MAX_INDEX_SIZE=config.getLong("index_size");
		}
		parentFolder=config.getString("path");
		streamMap=new ConcurrentHashMap<String,Stream>();
		try{
			if(!parentFolder.endsWith(File.separator)){
				parentFolder+=File.separator;
			}
			File ftest=new File(parentFolder);
			if(!ftest.exists()){
				ftest.mkdir();
			}
			for(String subFolder:ftest.list()){
				ftest=new File(parentFolder+subFolder);
				if(ftest.exists()){
					getOrCreateStream(subFolder);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}

	public void stop(){
		synchronized(this){
			for(Stream stream:streamMap.values()){
				try{
					stream.stop();
				}catch(Exception e){}
			}
		}
	}

	public Stream getOrCreateStream(String topic) throws IOException{
		if(streamMap.containsKey(topic)){
			return  streamMap.get(topic);
		}
		synchronized(this){
			if(!streamMap.containsKey(topic)){
				streamMap.put(topic,new Stream(parentFolder,topic,MAX_COMMIT_BATCH,MAX_COMMIT_LAG,MAX_BLOCK_SIZE,MAX_INDEX_SIZE));
			}
		}
		return streamMap.get(topic);
	}

	public Stream[] getStreams() throws IOException{
		return streamMap.values().toArray(new Stream[0]);
	}

	public Document getDocument(String topic,long location) throws IOException{
		Stream stream=getOrCreateStream(topic);
		return stream.getDocument(location);
	}

	public void addDocument(Document doc) throws IOException{
		Stream stream=getOrCreateStream(doc.getTopic());
		stream.addDocument(doc);
	}

	public void addDocument(Document doc,int writeMode) throws IOException{
		Stream stream=getOrCreateStream(doc.getTopic());
		stream.addDocument(doc,writeMode);
	}

	public void addDocuments(List<Document> batch) throws IOException{
		Stream stream=getOrCreateStream(batch.get(0).getTopic());
		stream.addDocuments(batch);
	}

	public void addDocuments(List<Document> batch,int writeMode) throws IOException{
		Stream stream=getOrCreateStream(batch.get(0).getTopic());
		stream.addDocuments(batch,writeMode);
	}

	public List<Document> getDocuments(String topic,long startIndex,long endIndex) throws IOException{
		Stream stream=getOrCreateStream(topic);
		return stream.getDocuments(startIndex,endIndex);
	}

	public void truncateStream(String topic,long index) throws IOException{
		if(index==0){
			deleteStream(topic);
			return;
		}
		Stream stream=getOrCreateStream(topic);
		stream.truncate(index);
	}

	public void deleteStream(String topic) throws IOException{
		Stream stream=getOrCreateStream(topic);
		stream.truncate(0);
		streamMap.remove(topic);
		File ftest=new File(parentFolder+topic);
		ftest.delete();
	}
}