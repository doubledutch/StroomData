package me.doubledutch.stroom.query.sql;

import java.util.*;
import org.json.*;

public class DerivedColumn{
	public static final int REFERENCE=0;

	public int type;
	public String as=null; // TODO: this should be a . separated path too
	public List<String> refList=null;

	public JSONArray pickArray(JSONObject source) throws JSONException{
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
	}

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
			buf.append(as);
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