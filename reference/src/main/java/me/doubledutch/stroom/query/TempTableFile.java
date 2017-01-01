package me.doubledutch.stroom.query;

import me.doubledutch.lazyjson.*;
import java.io.*;

public class TempTableFile extends TempTable{
	File file;
	PrintWriter out=null;
	BufferedReader in=null;
	LazyObject next=null;

	public TempTableFile(String folder) throws Exception{
		file=File.createTempFile("stroom_","json",new File(folder));
		file.deleteOnExit();
	}

	public TempTableFile() throws Exception{
		file=File.createTempFile("stroom_","json");
		file.deleteOnExit();
	}

	public void reset() throws Exception{
		if(in!=null){
			in.close();
		}
		in=null;
		next=null;
	}

	public void append(LazyObject obj) throws Exception{
		if(out==null){
			out=new PrintWriter(new FileWriter(file,true));
		}
		out.println(obj.toString());
		out.flush();
	}

	public void delete(){
		file.delete();
	}

	public LazyObject next() throws Exception{
		LazyObject tmp=next;
		next=null;
		return tmp;
	}

	public boolean hasNext() throws Exception{
		if(next==null){
			if(in==null){
				in=new BufferedReader(new FileReader(file));
			}
			String str=in.readLine();
			if(str!=null){
				if(str.trim().length()==0){
					return hasNext();
				}
				next=new LazyObject(str);
			}
		}
		if(next!=null)return true;
		return false;
	}
}