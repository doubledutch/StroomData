package me.doubledutch.stroom.query.sql;

import java.io.*;
import java.net.*;
import java.util.*;
import org.json.*;

public class SQLQueryEngine{
	private SQLQuery query;

	public SQLQueryEngine(SQLQuery query){
		this.query=query;
	}
/*
	public List<JSONObject> execute() throws Exception{
		List<JSONObject> result=new ArrayList<JSONObject>();
		// 1. load primary table
		TableReference table=query.tableList.get(0); // Only one source table for now
		List<JSONObject> data=loadResource(table);
		// 2. EXPAND ON
		if(table.expandOn!=null){
			// TODO: should this be pulled into derived column?
			List<JSONObject> tmp=new ArrayList<JSONObject>();
			for(JSONObject obj:data){
				String str=obj.toString(); // TODO: this is horrible, fix it!
				JSONArray internal=table.expandOn.pickArray(obj);
				for(int i=0;i<internal.length();i++){
					JSONObject newObj=new JSONObject(str);
					JSONObject current=newObj;
					for(int n=0;n<table.expandOn.refList.size()-1;n++){
						String selector=table.expandOn.refList.get(n);
						if(!current.has(selector)){
							current.put(selector,new JSONObject());
						}
						current=current.getJSONObject(selector);
					}
					String selector=table.expandOn.refList.get(table.expandOn.refList.size()-1);
					current.put(selector,internal.get(i));
					tmp.add(newObj);
				}
			}
			data=tmp;
		}
		// X. Pick out results
		for(int i=0;i<data.size();i++){
			JSONObject obj=data.get(i);
			JSONObject resultObj=pickValues(query,obj);
			result.add(resultObj);
		}
		return result;
	}

	private JSONObject pickValues(SQLQuery query, JSONObject obj) throws JSONException{
		if(query.selectAll)return obj;
		JSONObject result=new JSONObject();
		// TODO: pick columns
		for(DerivedColumn col:query.selectList){
			col.pick(obj,result);
		}
		return result;
	}

	private List<JSONObject> loadResource(TableReference table) throws IOException,JSONException,ParseException{
		/* if(table.filename!=null){
			if(table.filename.startsWith("http://")||table.filename.startsWith("https://")){
				return loadHttpTable(table.filename);
			}else{
				return loadFileTable(table.filename);
			}
		}*/
		// throw new ParseException("Only files and urls are valid table sources at this point");
	// }*/

}