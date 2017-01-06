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
	private Map<String,TempTable> result=null;
	private long time;

	public QueryRunner(SQLQuery query){
		this.query=query;
	}

	public long getTime(){
		return time;
	}

	private TempTable getPartition(String key) throws Exception{
		if(result==null){
			result=new HashMap<String,TempTable>();
		}
		if(!result.containsKey(key)){
			result.put(key,createTempTable());
		}
		return result.get(key);	
	}

	public Set<String> getPartitions(){
		if(result==null){
			run();
		}
		return result.keySet();
	}

	public TempTable getResult(){
		return getResult("");
	}

	public TempTable getResult(String key){
		if(result==null){
			run();
		}
		return result.get(key);
	}

	public void run(){
		try{
			long pre=System.nanoTime();
			// Scan tables
			TempTable raw=scan(query.tableList.get(0));

			// Pick columns
			if(!query.selectAll){
				// TempTable picked=pick(raw,query.selectList);
				pick(raw,query.selectList,query);
				raw.delete();
				// raw=picked;
			}else{
				result=new HashMap<String,TempTable>();
				if(query.isPartitioned()){
					raw.reset();
					while(raw.hasNext()){
						LazyObject next=raw.next();
						String key=query.getPartitionKey(next);
						TempTable partition=getPartition(key);
						partition.append(next);
					}
				}else{
					result.put("",raw);
				}
			}
			// Partition data
			/*if(query.isPartitioned()){
				raw.reset();
				while(raw.hasNext()){
					LazyObject next=raw.next();
					String key=query.getPartitionKey(next);
					System.out.println("key:"+key);
					TempTable partition=getPartition(key);
					partition.append(next);
				}
			}else{
				result=new HashMap<String,TempTable>();
				result.put("",raw);
			}*/
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

	public void pick(TempTable table,List<DerivedColumn> columns,SQLQuery query) throws Exception{
		// TempTable picked=createTempTable();
		table.reset();
		while(table.hasNext()){
			LazyObject obj=table.next();
			JSONObject out=new JSONObject();
			for(DerivedColumn col:columns){
				col.pickAndPlace(obj,out);
			}
			LazyObject outObj=new LazyObject(out.toString());
			String key=query.getPartitionKey(obj);
			// TODO: this is such a hack - make smarter
			if(key==null)key=query.getPartitionKey(outObj);
			TempTable partition=getPartition(key);
			partition.append(outObj);
			// picked.append(new LazyObject(out.toString()));
		}
		// return picked;
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
			// System.out.println("scanning url");
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