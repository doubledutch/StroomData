package me.doubledutch.stroom.perf;

import java.util.*;
import java.util.concurrent.*;

public class MetricManager implements Runnable{
	private static MetricManager app;
	private static Map<String,Metrics> metricMap=new ConcurrentHashMap<String,Metrics>();

	private Thread thread;

	public MetricManager(){
		app=this;
		thread=new Thread(this);
		thread.start();
	}

	public void run(){
		while(true){
			long pre=System.currentTimeMillis();
			for(Metrics m:metricMap.values()){
				m.cycle();
			}
			long post=System.currentTimeMillis();
			while(post-pre<60*1000l){
				try{
					Thread.sleep(60*1000l-(post-pre));
				}catch(Exception e){}
				post=System.currentTimeMillis();
			}
		}
	}

	public static MetricManager get(){
		return app;
	}

	public static Metrics get(String id){
		if(!metricMap.containsKey(id)){
			metricMap.put(id,new Counter());
		}
		return metricMap.get(id);
	}
}