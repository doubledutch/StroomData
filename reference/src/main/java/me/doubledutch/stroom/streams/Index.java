package me.doubledutch.stroom.streams;

import java.util.*;
import java.io.*;
import java.nio.channels.*;

public class Index{
	private final static int BUFFER_SIZE=128*IndexEntry.RECORD_SIZE;
	private String filename;
	private RandomAccessFile in;
	private FileOutputStream fout;
	private DataOutputStream out;
	private FileChannel fc;
	private long lastLocation=-1;
	private long startLocationRange;
	private long endLocationRange;
	private int write_mode;
	private byte[] buffer=new byte[IndexEntry.RECORD_SIZE];

	public Index(String filename,long startLocation,long endLocation,int write_mode) throws IOException{
		this.filename=filename;
		this.startLocationRange=startLocation;
		this.endLocationRange=endLocation;
		lastLocation=startLocationRange-1;
		this.write_mode=write_mode;
		verifyIndex();
		openIndex();
	}

	public long getLastLocation(){
		return lastLocation;
	}

	public long getEndLocationRange(){
		return endLocationRange;
	}

	public long getStartLocationRange(){
		return startLocationRange;
	}

	public boolean isFull(){
		return lastLocation==endLocationRange;
	}

	public void truncate(long index) throws IOException{
		if(lastLocation>=index){
			synchronized(out){
				//close();

				long offset=(index-startLocationRange)*IndexEntry.RECORD_SIZE;
				// System.out.println("Truncating at "+offset);
				fc.truncate(offset);
				lastLocation=index-1;
				/*File ftest=new File(filename);
		
				FileChannel fch = new FileOutputStream(ftest, true).getChannel();
			    fch.truncate(offset);
				fch.close();
				lastLocation=startLocationRange-1;
				verifyIndex();
				openIndex();*/
			}
		}
	}

	public void commitData() throws IOException{
		if(write_mode<Stream.NONE){
			if(write_mode==Stream.FLUSH){
				synchronized(out){		
					out.flush();
				}
			}else if(write_mode<Stream.FLUSH){
				synchronized(out){
					out.flush();
					fc.force(true);
				}
			}
		}
	}

	public void delete() throws IOException{
		synchronized(out){
			close();
			File ftest=new File(filename);
			ftest.delete();
		}
	}

	public void close(){
		synchronized(out){
			try{
				out.flush();
				fc.force(true);
			}catch(Exception e){}
			try{
				out.close();
			}catch(Exception e){}
			try{
				fc.close();
			}catch(Exception e){}
			try{
				in.close();
			}catch(Exception e){}
		}
	}

	private void openIndex() throws IOException{
		File ftest=new File(filename);
	    fout=new FileOutputStream(filename,true);
	    fc=fout.getChannel();
	    out=new DataOutputStream(new BufferedOutputStream(fout,BUFFER_SIZE));
	    in=new RandomAccessFile(filename,"r");
		if(ftest.length()>0){
			long n=ftest.length()-IndexEntry.RECORD_SIZE;
			lastLocation=lastLocation+ftest.length()/IndexEntry.RECORD_SIZE;
		}
	}

	private void verifyIndex() throws IOException{
		File ftest=new File(filename);
		if(!ftest.exists()){
			// No pre existing index, all is ok
			return;
		}
		if(ftest.length() % IndexEntry.RECORD_SIZE==0){
			// No half written index entries, all is ok
			return;
		}
		// Half written data, truncate
		FileChannel fch = new FileOutputStream(ftest, true).getChannel();
	    fch.truncate(ftest.length()-(ftest.length() % IndexEntry.RECORD_SIZE));
	    fch.close();
	}

	public IndexEntry seekEntry(long location) throws IOException,EOFException{
		if(location>lastLocation)return null;
		long offset=(location-startLocationRange)*IndexEntry.RECORD_SIZE;
		synchronized(in){
			in.seek(offset);
			in.readFully(buffer);
			return IndexEntry.read(location,buffer,0);
			// return IndexEntry.read(location,in);
		}
	}

	public List<IndexEntry> seekEntries(long startLocation,long endLocation) throws IOException,EOFException{
		
		if(startLocation>lastLocation)return null;
		if(endLocation>lastLocation)endLocation=lastLocation;
		// System.out.println("\nlastLocation: "+lastLocation+" startLocation:"+startLocation+" endLocation:"+endLocation);
		long offset=(startLocation-startLocationRange)*IndexEntry.RECORD_SIZE;
		byte[] data=new byte[((int)(endLocation-startLocation+1))*IndexEntry.RECORD_SIZE];
		List<IndexEntry> list=new ArrayList<IndexEntry>((int)(endLocation-startLocation+1));
		// System.out.println("Seeking to "+offset+" reading "+data.length+" in length "+fc.size());
		synchronized(in){
			in.seek(offset);
			in.readFully(data);
		}
		// System.out.println("Read completed");
		DataInputStream inStream=new DataInputStream(new ByteArrayInputStream(data));
		for(long i=startLocation;i<=endLocation;i++){
			list.add(IndexEntry.read(i,inStream));
		}
		inStream.close();
		return list;
	}

	public long addEntry(short block,long offset,int size) throws IOException{
		// Sync on out should not be needed since the add entry block in stream is already synced
		// synchronized(out){
			// TODO: Why was the lastlocation incrementation done so convoluted? re-read and document!
			synchronized(out){
				long newLocation=lastLocation+1;
				IndexEntry entry=new IndexEntry(newLocation,block,offset,size);
				entry.write(out);
				lastLocation+=1;
				return newLocation;
			}
			
		// }
	}
}