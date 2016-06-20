package me.doubledutch.stroom.client;

import me.doubledutch.stroom.client.function.*;
import java.util.function.*;
import java.util.*;
import org.json.*;
import java.io.*;

public class Service implements Runnable{
	private final static int FILTER=0;
	private final static int AGGREGATE=1;

	private long BATCH_WAIT=1000;
	private int BATCH_SIZE=1000;

	private int type;
	private String inputTopic;
	private String outputTopic;

	private StreamConnection inputStream,outputStream,stateStream;
	private Stroom con;

	private JSONObjectFunction filterObjectFunc;
	private JSONObjectArrayFunction filterObjectArrayFunc;
	private StringFunction filterStringFunc;
	private StringListFunction filterStringListFunc;
	private JSONObjectPredicate filterObjectPredicate;
	private StringPredicate filterStringPredicate;
	private JSONObjectBiFunction aggregateObjectBiFunc;
	private StringBiFunction aggregateStringBiFunc;

	private Thread thread;
	private boolean shouldBeRunning=false;
	private boolean isRunning=false;

	private List<String> outputBuffer;
	private long currentLocation=-1;
	private long outputLocation=-1;
	private String aggregateString=null;
	private JSONObject aggregateObject=null;

	private Service(Stroom con){
		this.con=con;
	}

	public void loadState() throws IOException,JSONException{
		if(type==FILTER){
			JSONObject obj=new JSONObject(stateStream.getLast());
			currentLocation=obj.getLong("i");
			outputLocation=obj.getLong("o");
		}else if(type==AGGREGATE){
			JSONObject obj=new JSONObject(stateStream.getLast());
			currentLocation=obj.getLong("i");
			outputLocation=obj.getLong("o");
			aggregateString=outputStream.get(outputLocation);
		}
	}

	public void saveState() throws IOException,JSONException{
		if(type==FILTER){
			JSONObject obj=new JSONObject();
			obj.put("i",currentLocation);
			obj.put("o",outputLocation);
			stateStream.append(obj,StreamConnection.FLUSH);
		}
	}

	public Service reset() throws IOException{
		boolean shouldRestart=isRunning;
		if(isRunning){
			stop();
		}
		if(outputStream!=null){
			outputStream.truncate(0l);
		}
		if(stateStream!=null){
			stateStream.truncate(0l);
		}
		currentLocation=-1;
		outputLocation=-1;
		if(shouldRestart){
			start();
		}
		return this;
	}

	public void processDocument(String str) throws JSONException{
		if(type==FILTER){
			if(filterObjectFunc!=null){
				JSONObject out=filterObjectFunc.apply(new JSONObject(str));
				if(out!=null){
					outputBuffer.add(out.toString());
				}
			}else if(filterObjectArrayFunc!=null){
				JSONArray out=filterObjectArrayFunc.apply(new JSONObject(str));
				if(out!=null){
					for(int i=0;i<out.length();i++){
						outputBuffer.add(out.getJSONObject(i).toString());
					}
				}
			}else if(filterStringFunc!=null){
				String out=filterStringFunc.apply(str);
				if(out!=null){
					outputBuffer.add(out);
				}
			}else if(filterStringListFunc!=null){
				List<String> out=filterStringListFunc.apply(str);
				if(out!=null){
					outputBuffer.addAll(out);
				}
			}else if(filterObjectPredicate!=null){
				boolean bool=filterObjectPredicate.test(new JSONObject(str));
				if(bool){
					outputBuffer.add(str);
				}
			}else if(filterStringPredicate!=null){
				boolean bool=filterStringPredicate.test(str);
				if(bool){
					outputBuffer.add(str);
				}
			}

		}else if(type==AGGREGATE){
			if(aggregateObjectBiFunc!=null){
				aggregateObject=aggregateObjectBiFunc.apply(aggregateObject,new JSONObject(str));
			}else if(aggregateStringBiFunc!=null){
				aggregateString=aggregateStringBiFunc.apply(aggregateString,str);
			}
		}
	}

	public void run(){
		isRunning=true;
		try{
			if(stateStream.getCount()>0){
				loadState();
			}
			while(shouldBeRunning){
				// TODO: implement output batching with timeout too
				List<String> batch=inputStream.get(currentLocation+1,currentLocation+BATCH_SIZE+1);
				if(batch.size()>0){
					for(String str:batch){
						processDocument(str);
					}
					if(type==FILTER && outputBuffer.size()>0){
						List<Long> result=outputStream.append(outputBuffer);
						outputBuffer.clear();
						outputLocation=result.get(result.size()-1);
					}else if(type==AGGREGATE){
						if(aggregateObject!=null){
							outputLocation=outputStream.append(aggregateObject.toString());
						}else if(aggregateString!=null){
							outputLocation=outputStream.append(aggregateString);
						}
					}
					currentLocation+=batch.size();
					saveState();
				}else{
					try{
						Thread.sleep(BATCH_WAIT);
					}catch(Exception e){}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		isRunning=false;
	}

	public Service start(){
		outputBuffer=new ArrayList<String>(BATCH_SIZE*2);
		shouldBeRunning=true;
		thread=new Thread(this);
		thread.start();
		return this;
	}

	private void initialize(){
		if(inputTopic!=null){
			inputStream=con.openStream(inputTopic);
		}
		if(outputTopic!=null){
			outputStream=con.openStream(outputTopic);
			stateStream=con.openStream(outputTopic+".state");
		}
	}

	public Service stop(){
		shouldBeRunning=false;
		while(isRunning){
			// TODO: implement this in a less hacky way using notify/wait
			try{
				Thread.sleep(25);
			}catch(Exception e){}
		}
		return this;
	}

	protected static Service filter(Stroom con,String input,JSONObjectFunction func,String output){
		Service s=new Service(con);
		s.type=FILTER;
		s.inputTopic=input;
		s.outputTopic=output;
		s.filterObjectFunc=func;
		s.initialize();
		return s;
	}

	protected static Service filter(Stroom con,String input,JSONObjectArrayFunction func,String output){
		Service s=new Service(con);
		s.type=FILTER;
		s.inputTopic=input;
		s.outputTopic=output;
		s.filterObjectArrayFunc=func;
		s.initialize();
		return s;
	}

	protected static Service filter(Stroom con,String input,StringFunction func,String output){
		Service s=new Service(con);
		s.type=FILTER;
		s.inputTopic=input;
		s.outputTopic=output;
		s.filterStringFunc=func;
		s.initialize();
		return s;
	}

	protected static Service filter(Stroom con,String input,StringListFunction func,String output){
		Service s=new Service(con);
		s.type=FILTER;
		s.inputTopic=input;
		s.outputTopic=output;
		s.filterStringListFunc=func;
		s.initialize();
		return s;
	}

	protected static Service filter(Stroom con,String input,JSONObjectPredicate func,String output){
		Service s=new Service(con);
		s.type=FILTER;
		s.inputTopic=input;
		s.outputTopic=output;
		s.filterObjectPredicate=func;
		s.initialize();
		return s;
	}

	protected static Service filter(Stroom con,String input,StringPredicate func,String output){
		Service s=new Service(con);
		s.type=FILTER;
		s.inputTopic=input;
		s.outputTopic=output;
		s.filterStringPredicate=func;
		s.initialize();
		return s;
	}

	protected static Service aggregate(Stroom con,String input,JSONObjectBiFunction func,String output){
		Service s=new Service(con);
		s.type=AGGREGATE;
		s.inputTopic=input;
		s.outputTopic=output;
		s.aggregateObjectBiFunc=func;
		s.initialize();
		return s;
	}

	protected static Service aggregate(Stroom con,String input,StringBiFunction func,String output){
		Service s=new Service(con);
		s.type=AGGREGATE;
		s.inputTopic=input;
		s.outputTopic=output;
		s.aggregateStringBiFunc=func;
		s.initialize();
		return s;
	}
}