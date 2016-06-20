package me.doubledutch.stroom;

import me.doubledutch.stroom.streams.*;
import me.doubledutch.stroom.query.*;

import java.util.*;
import java.util.concurrent.*;

public class QueryManager{
	private static QueryManager app=null;
	private QueryRunner runner;
	private StreamHandler streams;
	private Map<String,Query> queryMap=new ConcurrentHashMap<String,Query>();

	public QueryManager(StreamHandler streams){
		app=this;
		this.streams=streams;
		runner=new QueryRunner(streams);
	}

	public static QueryManager get(){
		return app;
	}

	public Query run(String script,String type) throws Exception{
		if(type.equals("sql")){
			Query q=new Query(script,Query.SQL);
			queryMap.put(q.getId(),q);
			runner.runQuery(q);
			return q;
		}
		return null;
	}

	public void delete(String id){
		Query q=queryMap.get(id);
		if(q!=null){
			queryMap.remove(id);
			// Now remove all tasks and streams
		}
	}

	public Query get(String id){
		return queryMap.get(id);
	}
}