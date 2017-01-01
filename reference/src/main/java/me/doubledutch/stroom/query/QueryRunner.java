package me.doubledutch.stroom.query;

import me.doubledutch.stroom.client.*;
import me.doubledutch.stroom.streams.*;
import me.doubledutch.stroom.query.sql.*;
import me.doubledutch.stroom.*;
import me.doubledutch.stroom.client.StreamConnection;
import me.doubledutch.lazyjson.*;

import org.json.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class QueryRunner implements Runnable{
	private final int BATCH_SIZE=1000;
	private SQLQuery query;
	private TempTable result=null;
	private long time;

	public QueryRunner(SQLQuery query){
		this.query=query;
	}

	public long getTime(){
		return time;
	}

	public TempTable getResult(){
		if(result==null){
			run();
		}
		return result;
	}

	public void run(){
		try{
			long pre=System.nanoTime();
			result=scan(query.tableList.get(0));
			if(!query.selectAll){
				result=pick(result,query.selectList);
			}
			long post=System.nanoTime();
			time=post-pre;
			time=time/1000000;

		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private TempTable createTempTable() throws Exception{
		// Could some day switch between local files and streams
		return new TempTableFile();
	}

	public TempTable pick(TempTable table,List<DerivedColumn> columns) throws Exception{
		TempTable picked=createTempTable();
		table.reset();
		while(table.hasNext()){
			LazyObject obj=table.next();
			JSONObject out=new JSONObject();
			for(DerivedColumn col:columns){
				col.pickAndPlace(obj,out);
			}
			picked.append(new LazyObject(out.toString()));
		}
		return picked;
	}

	public TempTable scan(TableReference table) throws Exception{
		if(table.query!=null){
			QueryRunner sub=new QueryRunner(table.query);
			sub.run();
			if(table.condition!=null){
				// Run through it
			}else{
				return sub.getResult();
			}
		}else if(table.url!=null){
			System.out.println("scanning url");
			TempTable temp=createTempTable();
			Stroom s=new Stroom();
			StreamConnection stream=s.openStream(table.url);
			long num=stream.getCount();
			long count=0;
			while(count<num){
				List<String> batch=stream.get(count,count+BATCH_SIZE);
				for(String str:batch){
					LazyObject obj=new LazyObject(str);
					if(table.condition!=null){
						if(table.condition.evaluateBoolean(obj)){
							temp.append(obj);
						}
					}else{
						temp.append(obj);
					}
					count++;
				}
				if(batch.size()==0){
					// Possible connection error, retry - also possible table wipe
					try{
						Thread.sleep(250);
					}catch(Exception e){}
				}
			}
			return temp;
		}
		return null;
	}
}