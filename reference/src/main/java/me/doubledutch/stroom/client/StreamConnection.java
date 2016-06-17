package me.doubledutch.stroom.client;

import java.util.*;
import java.io.*;
import java.net.*;
import org.json.*;

public interface StreamConnection{
	public final static int LINEAR=0;
	public final static int SYNC=1;
	public final static int FLUSH=2;
	public final static int NONE=3;

	public List<String> get(long index,long endIndex) throws IOException;
	public String get(long index) throws IOException;
	public String getLast() throws IOException;
	public long getCount() throws IOException;
	
	public long append(JSONObject data) throws IOException,JSONException;
	public long append(String data) throws IOException,JSONException;
	public List<Long> append(List<String> data) throws IOException,JSONException;
	
	public long append(JSONObject data,int hint) throws IOException,JSONException;
	public long append(String data,int hint) throws IOException,JSONException;
	public List<Long> append(List<String> data,int hint) throws IOException,JSONException;

	public void truncate(long index) throws IOException;
}