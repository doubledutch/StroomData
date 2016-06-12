package me.doubledutch.stroom.query;

import me.doubledutch.stroom.*;

import java.util.*;

public class TableScanner extends Observable implements Runnable{
	private StreamConnection input,output;
	private long start,end;

	public TableScanner(StreamConnection input,long start,long end,StreamConnection output){
		this.input=input;
		this.output=output;
		this.start=start;
		this.end=end;
	}

	public void run(){
		try{
			long index=start;
			long range=end;
			if(range-index>1000){
				range=index+1000;
			}
			while(index<end){
				List<String> list=input.get(index,range);
				List<String> out=new ArrayList<String>(list.size());
				for(String str:list){
					out.add(str);
				}
				if(out.size()>0){
					output.append(out);
				}
				index=range+1;
				range=end;
				if(range-index>1000){
					range=index+1000;
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		notifyObservers();
	}
}