package me.doubledutch.stroom.query;

import java.util.*;
import org.json.*;

public class Query{
	public static final int WAITING=0;
	public static final int RUNNING=1;
	public static final int ERROR=3;
	public static final int COMPLETED=4;

	private int state=WAITING;

	public static final int SQL=0;
	private int type=SQL;

	private String text;
	private String error;
	private String id;

	public Query(String text,int type){
		this.text=text;
		this.type=type;
		this.id=UUID.randomUUID().toString();
	}

	public void setState(int state){
		this.state=state;
	}

	public void setError(String msg){
		this.error=msg;
	}

	public String getId(){
		return id;
	}

	public String getText(){
		return text;
	}

	public int getType(){
		return type;
	}

	public JSONObject toJSON() throws JSONException{
		JSONObject obj=new JSONObject();
		obj.put("id",id);
		if(type==SQL){
			obj.put("type","SQL");
		}
		if(state==WAITING){
			obj.put("state","WAITING");
		}else if(state==RUNNING){
			obj.put("state","RUNNING");
		}else if(state==ERROR){
			obj.put("state","ERROR");
		}else if(state==COMPLETED){
			obj.put("state","COMPLETED");
		}
		if(error!=null){
			obj.put("error",error);
		}

		return obj;
	}
}