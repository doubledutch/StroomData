package me.doubledutch.stroom;


import me.doubledutch.lazyjson.*;
import java.io.*;
import java.net.*;
import org.json.*;
import java.util.*;

public class Utility{
	/**
	 * Read the full contents of a text file
	 */
	public static String readFile(String filename) throws IOException{
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		StringBuilder buf=new StringBuilder();
		char[] data=new char[8192];
		int num=reader.read(data);
		while(num>-1){
			buf.append(data,0,num);
			num=reader.read(data);
		}
		return buf.toString();
	}

	public static Object pickValue(LazyObject obj,String key) throws LazyException{
		if(key.indexOf(".")>-1){
			String child=key.substring(key.indexOf(".")+1);
			key=key.substring(0,key.indexOf("."));
			return pickValue(obj.getJSONObject(key),child);
		}
		if(!obj.has(key)){
			return null;
		}
		LazyType t=obj.getType(key);
		switch(t){
			case ARRAY:return obj.getJSONArray(key);
			case OBJECT:return obj.getJSONObject(key);
			case FLOAT:return obj.getDouble(key);
			case INTEGER:return obj.getLong(key);
			case BOOLEAN:return obj.getBoolean(key);
			case STRING:return obj.getString(key);
		}
		return null;
	}

	public static Object pickValue(JSONObject obj,String key) throws JSONException{
		if(key.indexOf(".")>-1){
			String child=key.substring(key.indexOf(".")+1);
			key=key.substring(0,key.indexOf("."));
			return pickValue(obj.getJSONObject(key),child);
		}
		if(!obj.has(key)){
			return null;
		}
		return obj.get(key);
	}

	public static String postURL(String strurl,String body){
		try{
			URL url=new URL(strurl);
			HttpURLConnection con = (HttpURLConnection)url.openConnection();
			con.setRequestMethod("POST");
			con.addRequestProperty("Content-Type", "application/json");
			byte[] outdata=body.getBytes("UTF-8");
			con.setRequestProperty("Content-Length",""+outdata.length);
			con.setDoOutput(true);
			con.getOutputStream().write(outdata);

			try(Reader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"))){
				StringBuilder buf = new StringBuilder();
			    char[] indata = new char[32768];
			    int num = reader.read(indata);
			    while (num > -1) {
			      	buf.append(indata, 0, num);
			      	num = reader.read(indata);
			    }
			    return buf.toString();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

	public static String postURL(String strurl,String body,Map<String,String> headers){
		try{
			URL url=new URL(strurl);
			HttpURLConnection con = (HttpURLConnection)url.openConnection();
			con.setRequestMethod("POST");
			con.addRequestProperty("Content-Type", "application/json");
			byte[] outdata=body.getBytes("UTF-8");
			con.setRequestProperty("Content-Length",""+outdata.length);
			for(String key:headers.keySet()){
				con.setRequestProperty(key,headers.get(key));
			}
			con.setDoOutput(true);
			con.getOutputStream().write(outdata);

			try(Reader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"))){
				StringBuilder buf = new StringBuilder();
			    char[] indata = new char[32768];
			    int num = reader.read(indata);
			    while (num > -1) {
			      	buf.append(indata, 0, num);
			      	num = reader.read(indata);
			    }
			    return buf.toString();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
}