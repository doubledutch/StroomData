package me.doubledutch.stroom.perf;

public class MetricManager implements Runnable{
	private static MetricManager app;
	// private static Map<String,Metrics> metricMap=new HashMap<String,Metrics>();

	private Thread thread;

	public MetricManager(){
		app=this;
		thread=new Thread(this);
		thread.start();
	}

	public void run(){

	}

	public static MetricManager get(){
		return app;
	}

	public static Metrics get(String id){
		return null;
	}
}