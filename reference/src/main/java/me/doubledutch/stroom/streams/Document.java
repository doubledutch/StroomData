package me.doubledutch.stroom.streams;

import org.json.*;
import java.io.*;
import java.util.zip.*;
import java.nio.charset.*;

public class Document{
	private String topic;
	private long location=-1;
	private String data;

	public Document(String topic,String data){
		this.topic=topic;
		this.data=data;
	}

	public Document(String topic,String data, long location){
		this.topic=topic;
		this.data=data;
		this.location=location;
	}

	public Document(String topic,byte[] buffer, long location){
		this.topic=topic;
		this.location=location;
		try{
			this.data=new String(buffer,Charset.forName("UTF-8"));
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public Document(String topic,byte[] buffer,int offset,int size, long location){
		this.topic=topic;
		this.location=location;
		try{
			this.data=new String(buffer,offset,size,Charset.forName("UTF-8"));
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public String getTopic(){
		return topic;
	}

	public void setLocation(long location){
		this.location=location;
	}

	public long getLocation(){
		return location;
	}

	public String getStringData(){
		return data;
	}

	public byte[] getData(){
		try{
			byte[] raw=data.getBytes("UTF-8");
			return raw;
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
}