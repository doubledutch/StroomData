package me.doubledutch.stroom.query.sql;

import java.util.*;
import org.json.*;
import me.doubledutch.lazyjson.*;

public class DerivedColumn{
	public static final int REFERENCE=0;

	public int type;
	public List<String> as=null; 
	public List<String> refList=null;

	public void pickAndPlace(LazyObject source,JSONObject destination) throws Exception{
		if(type==REFERENCE){
			Object value=pickValue(source);
			setValue(destination,value);
		}
	}

	public void setValue(JSONObject destination,Object value) throws Exception{
		JSONObject current=destination;
		if(as==null){
			// No as clause was specified, place where it was found
			for(int i=0;i<refList.size()-1;i++){
				String selector=refList.get(i);
				if(!current.has(selector)){
					current.put(selector,new JSONObject());
				}
				current=current.getJSONObject(selector);
			}
			String selector=refList.get(refList.size()-1);
			current.put(selector,value);
		}else{
			for(int i=0;i<as.size()-1;i++){
				String selector=as.get(i);
				if(!current.has(selector)){
					current.put(selector,new JSONObject());
				}
				current=current.getJSONObject(selector);
			}
			String selector=as.get(as.size()-1);
			System.out.println("Should be placing at "+selector);
			current.put(selector,value);
		}
	}

	public Object pickValue(LazyObject source){
		LazyObject current=source;
		for(int i=0;i<refList.size()-1;i++){
			String selector=refList.get(i);
			current=current.getJSONObject(selector);
		}
		String selector=refList.get(refList.size()-1);
		LazyType t=current.getType(selector);
		switch(t){
			case ARRAY:return current.getJSONArray(selector);
			case OBJECT:return current.getJSONObject(selector);
			case FLOAT:return current.getDouble(selector);
			case INTEGER:return current.getLong(selector);
			case BOOLEAN:return current.getBoolean(selector);
			case STRING:return current.getString(selector);
		}
		return null;
	}

	/*public JSONArray pickArray(JSONObject source) throws JSONException{
		if(type==REFERENCE){
			JSONObject current=source;
			for(int i=0;i<refList.size()-1;i++){
				String selector=refList.get(i);
				current=current.getJSONObject(selector);
			}
			String selector=refList.get(refList.size()-1);
			return current.getJSONArray(selector);
		}
		return null;
	}*/

	/*
	public void pick(JSONObject source,JSONObject destination) throws JSONException{
		if(type==REFERENCE){
			JSONObject current=source;
			for(int i=0;i<refList.size()-1;i++){
				String selector=refList.get(i);
				current=current.getJSONObject(selector);
			}
			String selector=refList.get(refList.size()-1);
			Object value=current.get(selector);
			if(as!=null){
				destination.put(as,value);
			}else{
				current=destination;
				for(int i=0;i<refList.size()-1;i++){
					selector=refList.get(i);
					if(!current.has(selector)){
						current.put(selector,new JSONObject());
					}
					current=current.getJSONObject(selector);
				}
				selector=refList.get(refList.size()-1);
				current.put(selector,value);
			}
		}
	}
*/
	public String toString(){
		StringBuilder buf=new StringBuilder();
		if(type==REFERENCE){
			for(int i=0;i<refList.size();i++){
				if(i>0)buf.append(".");
				buf.append(refList.get(i));
			}
		}
		if(as!=null){
			buf.append(" AS ");
			for(int i=0;i<as.size();i++){
				if(i>0)buf.append(".");
				buf.append(as.get(i));
			}
		}
		return buf.toString();
	}

	public static DerivedColumn createReference(List<String> ref){
		DerivedColumn col=new DerivedColumn();
		col.refList=ref;
		col.type=REFERENCE;
		return col;
	}
}