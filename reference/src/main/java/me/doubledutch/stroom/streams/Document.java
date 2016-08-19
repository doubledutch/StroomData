package me.doubledutch.stroom.streams;

import org.json.*;
import java.io.*;
import java.util.zip.*;
import java.nio.charset.*;
import java.nio.charset.StandardCharsets;

public class Document{
	private String topic;
	private long location=-1;
	private String data;
	private byte[] raw=null;
	private int size=-1;

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
		this.data=new String(buffer,StandardCharsets.UTF_8);
	}

	public Document(String topic,byte[] buffer,int offset,int size, long location){
		this.topic=topic;
		this.location=location;
		this.data=new String(buffer,offset,size,StandardCharsets.UTF_8);
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

	public int getDataSize(){
		if(size>-1)return size;
		size=getData().length;
		return size;
	}

	public byte[] getData(){
		if(raw!=null){
			return raw;
		}
		raw=data.getBytes(StandardCharsets.UTF_8);
		return raw;
	}
}