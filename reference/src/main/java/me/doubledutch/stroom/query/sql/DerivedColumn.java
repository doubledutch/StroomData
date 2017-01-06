package me.doubledutch.stroom.query.sql;

import java.util.*;
import org.json.*;
import me.doubledutch.stroom.query.*;
import me.doubledutch.lazyjson.*;

public class DerivedColumn{
	// public static final int REFERENCE=0;
	private int number=0;
	// public int type;
	public List<String> as=null; 
	// public List<String> refList=null;
	public Expression expression=null;

	public void pickAndPlace(LazyObject source,JSONObject destination) throws Exception{
		// Object value=pickValue(source);
		Expression result=expression.evaluate(source);
		if(result==null){
			System.out.println("Found null result for expression");
			System.out.println(expression.toString());
		}
		// System.out.println(result.toString());
		Object value=result.getValue();
		setValue(destination,value);
	}

	public void setValue(JSONObject destination,Object value) throws Exception{
		JSONObject current=destination;
		if(as==null){
			// No as clause was specified, place where it was found
			if(expression.getType()==Expression.REFERENCE){
				for(int i=0;i<expression.ref.size()-1;i++){
					String selector=expression.ref.get(i);
					if(!current.has(selector)){
						current.put(selector,new JSONObject());
					}
					current=current.getJSONObject(selector);
				}
				String selector=expression.ref.get(expression.ref.size()-1);
				current.put(selector,value);
			}else{
				// Not from reference and no name given, auto name it
				destination.put("column_"+number,value);
			}
		}else{
			for(int i=0;i<as.size()-1;i++){
				String selector=as.get(i);
				if(!current.has(selector)){
					current.put(selector,new JSONObject());
				}
				current=current.getJSONObject(selector);
			}
			String selector=as.get(as.size()-1);
			// System.out.println("Should be placing at "+selector);
			current.put(selector,value);
		}
	}

	public Object pickValue(LazyObject source){
		LazyObject current=source;
		for(int i=0;i<expression.ref.size()-1;i++){
			String selector=expression.ref.get(i);
			current=current.getJSONObject(selector);
		}
		String selector=expression.ref.get(expression.ref.size()-1);
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

	public String toString(){
		StringBuilder buf=new StringBuilder();
		buf.append(expression.toString());
		if(as!=null){
			buf.append(" AS ");
			for(int i=0;i<as.size();i++){
				if(i>0)buf.append(".");
				buf.append(as.get(i));
			}
		}
		return buf.toString();
	}

	public static DerivedColumn createReference(int number,Expression exp){
		DerivedColumn col=new DerivedColumn();
		col.number=number;
		col.expression=exp;
		// col.type=REFERENCE;
		return col;
	}
}