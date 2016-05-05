package me.doubledutch.stroom;

import java.io.*;
import java.net.*;
import org.json.*;
import java.util.*;
import me.doubledutch.stroom.streams.*;

public class LocalStreamConnection implements StreamConnection{
	private Stream stream=null;
	private String topic=null;

	public LocalStreamConnection(Stream stream){
		this.stream=stream;
		this.topic=stream.getTopic();
	}

	public List<String> get(long index,long endIndex) throws IOException{
		List<Document> data=stream.getDocuments(index,endIndex);
		List<String> result=new ArrayList<String>(data.size());
		for(Document doc:data){
			result.add(doc.getStringData());
		}
		return result;
	}

	public long getCount() throws IOException{
		return stream.getCount();
	}

	public String get(long index) throws IOException{
		Document doc=stream.getDocument(index);
		if(doc==null)return null;
		return doc.getStringData();
	}

	public String getLast() throws IOException{
		return get(stream.getCount()-1);
	}

	public long append(JSONObject data) throws IOException,JSONException{
		return append(data.toString());
	}

	public long append(String data) throws IOException,JSONException{
		JSONObject obj=new JSONObject(data);
		Document doc=new Document(topic,obj.toString());
		stream.addDocument(doc);
		return doc.getLocation();
	}

	public void truncate(long index) throws IOException{
		stream.truncate(index);
	}
}