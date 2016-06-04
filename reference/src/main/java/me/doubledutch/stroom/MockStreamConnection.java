package me.doubledutch.stroom;

import java.io.*;
import java.net.*;
import org.json.*;
import java.util.*;

public class MockStreamConnection implements StreamConnection{
	private List<String> list=new ArrayList<String>();

	public MockStreamConnection(){

	}
	
	public List<String> get(long index,long endIndex) throws IOException{
		return list.subList((int)index,(int)endIndex);
	}

	public String get(long index) throws IOException{
		if(list.size()>index){
			return list.get((int)index);
		}
		return null;
	}
	public long getCount() throws IOException{
		return list.size();
	}

	public String getLast() throws IOException{
		if(list.size()>0){
			return list.get(list.size()-1);
		}
		return null;
	}

	public long append(JSONObject data,int hint) throws IOException,JSONException{
		return append(data);
	}
	public long append(String data,int hint) throws IOException,JSONException{
		return append(data);
	}
	public List<Long> append(List<String> data,int hint) throws IOException,JSONException{
		return append(data);
	}

	public synchronized long append(JSONObject data) throws IOException,JSONException{
		return append(data.toString());
	}

	public List<Long> append(List<String> data) throws IOException,JSONException{
		List<Long> output=new ArrayList<Long>();
		for(String str:data){
			output.add(append(str));
		}
		return output;
	}

	public synchronized long append(String data) throws IOException,JSONException{
		list.add(data);
		return list.size()-1;
	}

	public synchronized void truncate(long index) throws IOException{
		if(index==0){
			list=new ArrayList<String>();
		}
		while(list.size()>index){
			list.remove(list.size()-1);
		}
	}
}