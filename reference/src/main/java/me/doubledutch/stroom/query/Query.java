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
	private JSONObject executionPlan;

	private long tStart=-1;
	private long tEnd=-1;
	private long scanTotal=0;
	private long scanCurrent=0;

	public Query(String text,int type){
		this.text=text;
		this.type=type;
		this.id=UUID.randomUUID().toString();
	}

	public void startTimer(){
		tStart=System.currentTimeMillis();
	}

	public void stopTimer(){
		tEnd=System.currentTimeMillis();
	}


	public void setScanTotal(long s){
		scanTotal=s;
	}

	public void setScanCurrent(long s){
		scanCurrent=s;
	}

	public void setState(int state){
		this.state=state;
	}

	public void setError(String msg){
		this.error=msg;
	}

	public void setExecutionPlan(JSONObject obj){
		this.executionPlan=obj;
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
		if(executionPlan!=null){
			obj.put("execution_plan",executionPlan);
		}

		if(tStart>-1){
			if(tEnd>-1){
				obj.put("time",(tEnd-tStart));
			}else{
				obj.put("time",(System.currentTimeMillis()-tStart));
			}
		}

		obj.put("scan_total",scanTotal);
		obj.put("scan_current",scanCurrent);

		return obj;
	}
}