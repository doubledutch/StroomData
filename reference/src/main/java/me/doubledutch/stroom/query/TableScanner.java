package me.doubledutch.stroom.query;

import me.doubledutch.stroom.*;
import me.doubledutch.stroom.client.StreamConnection;
import org.json.*;
import java.util.*;

public class TableScanner implements Runnable{
	private final static int BATCH_SIZE=1000;
	private StreamConnection input,output;
	private long start,end;
	private QueryListener completedListener;
	private Expression expression=null;

	public TableScanner(StreamConnection input,long start,long end,StreamConnection output){
		this.input=input;
		this.output=output;
		this.start=start;
		this.end=end;
	}

	public TableScanner(StreamConnection input,long start,long end,StreamConnection output,Expression expression){
		this.input=input;
		this.output=output;
		this.start=start;
		this.end=end;
		this.expression=expression;
	}

	public void setCompletedListener(QueryListener listener){
		completedListener=listener;
	}

	public void run(){
		QueryMetrics metric=null;
		try{
			metric=new QueryMetrics("tablescanner");
			long index=start;
			long range=end;
			if(range-index>BATCH_SIZE){
				range=index+BATCH_SIZE;
			}
			while(index<end){
				// System.out.println("scan "+index+"-"+range);
				metric.startTimer("read.time");
				List<String> list=input.get(index,range);
				metric.stopTimer("read.time");
				metric.inc("read.count",list.size());
				metric.startTimer("query.eval");
				List<String> out=new ArrayList<String>(list.size());
				for(String str:list){
					JSONObject obj=new JSONObject(str);
					metric.inc("read.size",str.length());
					boolean match=true;
					if(expression!=null){
						Expression result=expression.evaluate(obj);
						if(result.isBoolean() && result.getBoolean()){
							match=true;
						}else{
							match=false;
						}
					}
					if(match){
						// System.out.println(str);
						out.add(str);
					}
				}
				metric.stopTimer("query.eval");
				metric.inc("write.count",out.size());
				if(out.size()>0){
					metric.startTimer("write.time");
					output.append(out);
					metric.stopTimer("write.time");
				}
				index=range+1;
				range=end;
				if(range-index>BATCH_SIZE){
					range=index+BATCH_SIZE;
				}
			}

		}catch(Exception e){
			e.printStackTrace();
		}
		completedListener.update(metric);
	}
}