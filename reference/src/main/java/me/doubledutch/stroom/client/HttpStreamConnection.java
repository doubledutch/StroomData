package me.doubledutch.stroom.client;

import java.util.*;
import org.json.*;
import java.io.*;

public class HttpStreamConnection implements StreamConnection{
	private Stroom con;
	private String baseURL;

	protected HttpStreamConnection(Stroom con,String baseURL){
		this.con=con;
		this.baseURL=baseURL;
	}

	public List<String> get(long index,long endIndex) throws IOException{
		try{
			String output=con.getURL(baseURL+index+"-"+endIndex);
			if(output!=null){
				JSONArray data=new JSONArray(output);
				ArrayList<String> result=new ArrayList<String>(data.length());
				for(int i=0;i<data.length();i++){
					String doc=data.getJSONObject(i).toString();
					result.add(doc);
				}
				return result;
			}
		}catch(Exception e){

		}
		return null;
	}

	public String get(long index) throws IOException{
		try{
			String output=con.getURL(baseURL+index);
			if(output!=null && output.trim().length()>0){
				return output;
			}
		}catch(Exception e){

		}
		return null;
	}

	public String getLast() throws IOException{
		try{
			String output=con.getURL(baseURL+"_");
			if(output!=null && output.trim().length()>0){
				return output;
			}
		}catch(Exception e){

		}
		return null;
	}

	public long getCount() throws IOException{
		try{
			String output=con.getURL(baseURL);
			if(output!=null && output.trim().length()>0){
				JSONObject obj=new JSONObject(output);
				return obj.getLong("count");
			}
		}catch(Exception e){

		}
		// TODO: should this be returning a Long and thus null instead?
		return -1;
	}
	
	public long append(JSONObject data) throws IOException,JSONException{
		return append(data.toString());
	}

	public long append(String data) throws IOException,JSONException{
		try{
			String output=con.postURL(baseURL,data);
			if(output!=null && output.trim().length()>0){
				JSONObject obj=new JSONObject(output);
				return obj.getLong("location");
			}
		}catch(Exception e){

		}
		// TODO: should this be returning a Long and thus null instead?
		return -1;
	}

	public List<Long> append(List<String> data) throws IOException,JSONException{
		try{
			JSONArray postData=new JSONArray();
			for(String str:data){
				postData.put(new JSONObject(str));
			}
			String output=con.postURL(baseURL,postData.toString());
			if(output!=null && output.trim().length()>0){
				JSONObject obj=new JSONObject(output);
				JSONArray locationList=obj.getJSONArray("location_list");
				ArrayList<Long> result=new ArrayList<Long>(locationList.length());
				for(int i=0;i<locationList.length();i++){
					result.add(locationList.getLong(i));
				}
				return result;
			}
		}catch(Exception e){

		}
		return null;
	}
	
	public long append(JSONObject data,int hint) throws IOException,JSONException{
		return append(data.toString(),hint);
	}

	public long append(String data,int hint) throws IOException,JSONException{
		try{
			Map<String,String> map=new HashMap<String,String>();
			map.put("X-stroom-hint",getHintName(hint));
			String output=con.postURL(baseURL,data,map);
			if(output!=null && output.trim().length()>0){
				JSONObject obj=new JSONObject(output);
				return obj.getLong("location");
			}
		}catch(Exception e){

		}
		// TODO: should this be returning a Long and thus null instead?
		return -1;
	}

	public List<Long> append(List<String> data,int hint) throws IOException,JSONException{
		try{
			JSONArray postData=new JSONArray();
			for(String str:data){
				postData.put(str);
			}
			Map<String,String> map=new HashMap<String,String>();
			map.put("X-stroom-hint",getHintName(hint));
			String output=con.postURL(baseURL,postData.toString(),map);
			if(output!=null && output.trim().length()>0){
				JSONObject obj=new JSONObject(output);
				JSONArray locationList=obj.getJSONArray("location_list");
				ArrayList<Long> result=new ArrayList<Long>(locationList.length());
				for(int i=0;i<locationList.length();i++){
					result.add(locationList.getLong(i));
				}
				return result;
			}
		}catch(Exception e){

		}
		return null;
	}

	public void truncate(long index) throws IOException{
		con.deleteURL(baseURL+index);
	}

	private String getHintName(int hint){
		switch(hint){
			case 0:return "LINEAR";
			case 1:return "SYNC";
			case 2:return "FLUSH";
			case 3:return "NONE";
		}
		return "";
	}
}