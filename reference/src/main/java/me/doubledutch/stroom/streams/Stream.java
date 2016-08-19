package me.doubledutch.stroom.streams;

import java.util.concurrent.*;
import java.util.*;
import java.io.*;
import org.json.*;

public class Stream implements Runnable{
	public final static int LINEAR=0;
	public final static int SYNC=1;
	public final static int FLUSH=2;
	public final static int NONE=3;

	private int write_mode=SYNC;

	private long MAX_BLOCK_SIZE=32*1024*1024*1024l; // 32 gb block files
	private long MAX_INDEX_SIZE=50*1000*1000l; // close to 1gb index blocks
	private int MAX_COMMIT_BATCH;
	private int MAX_COMMIT_LAG;
	private String topic;
	private String folder;

	// NOTE: using map instead of list to allow random insertion and eventual pruning
	private Map<Short,Block> blockMap;
	private Map<Short,Index> indexMap;
	private short currentBlockNumber=0;
	private short currentIndexNumber=0;

	private long commitedLocation=-1;
	private long currentLocation=-1;

	private boolean shouldBeRunning=true;
	private boolean isRunning=true;

	private long lastCommitTime=System.currentTimeMillis();

	public Stream(String parentFolder,String topic,int batch,int lag,long block_size,long index_size) throws IOException{
		this.topic=topic;
		this.MAX_COMMIT_BATCH=batch;
		this.MAX_COMMIT_LAG=lag;
		this.MAX_BLOCK_SIZE=block_size;
		this.MAX_INDEX_SIZE=index_size;
		folder=parentFolder+topic+File.separator;
		File ftest=new File(folder);
		if(!ftest.exists()){
			ftest.mkdir();
		}
		// Open blocks and indexes
		indexMap=new Hashtable<Short,Index>();
		blockMap=new Hashtable<Short,Block>();
		String[] files=ftest.list();
		for(String filename:files){
			if(filename.startsWith(topic+"_")){
				short fileNumber=getFilenameNumber(filename);
				if(filename.endsWith(".data")){
					Block block=new Block(folder+filename,write_mode);
					blockMap.put(fileNumber,block);
					if(fileNumber>currentBlockNumber){
						currentBlockNumber=fileNumber;
					}
				}else if(filename.endsWith(".ndx")){
					Index index=new Index(folder+filename,(fileNumber-1)*MAX_INDEX_SIZE,fileNumber*MAX_INDEX_SIZE-1,write_mode);
					indexMap.put(fileNumber,index);
					if(fileNumber>currentIndexNumber){
						currentIndexNumber=fileNumber;
					}
				}
			}
		}
		if(currentBlockNumber==0){
			// No block files, create one
			createNewBlock(currentBlockNumber);
		}
		if(currentIndexNumber==0){
			// No index files, create one
			createNewIndex(currentIndexNumber);
		}
		Index index=indexMap.get(currentIndexNumber);
		currentLocation=index.getLastLocation();
		commitedLocation=currentLocation;

		new Thread(this).start();
	}

	public long getCount(){
		return commitedLocation+1;
	}

	public long getSize(){
		long size=0;
		for(Block block:blockMap.values()){
			size+=block.getSize();
		}
		return size;
	}

	public String getTopic(){
		return topic;
	}

	private short getFilenameNumber(String filename){
		// TODO: perhaps replace with regexp since it only happens at startup
		String numberStr=filename.substring(filename.lastIndexOf("_")+1);
		numberStr=numberStr.substring(0,numberStr.indexOf("."));
		while(numberStr.startsWith("0"))numberStr=numberStr.substring(1);
		return Short.parseShort(numberStr);
	}

	private void createNewBlock(short expectedNumber) throws IOException{
		synchronized(this){
			// Don't create a new block file if someone else just did
			if(expectedNumber==currentBlockNumber){
				commitData();
				expectedNumber++;
				String filename=""+expectedNumber;
				// TODO: perhaps change zero padding to printf formatting
				while(filename.length()<5)filename="0"+filename;
				filename=folder+topic+"_"+filename+".data";
				Block block=new Block(filename,write_mode);
				blockMap.put(expectedNumber,block);
				currentBlockNumber=expectedNumber;
			}
		}
	}

	private Index getIndexForLocation(long location){
		return indexMap.get((short)(location/MAX_INDEX_SIZE+1));
	}

	private void createNewIndex(short expectedNumber) throws IOException{
		// TODO: check for memory model issues 
		if(expectedNumber!=currentIndexNumber)return;
		synchronized(this){
			// Don't create a new index file if someone else just did
			if(expectedNumber==currentIndexNumber){
				commitData();
				expectedNumber++;
				String filename=""+expectedNumber;
				// TODO: perhaps change zero padding to printf formatting
				while(filename.length()<5)filename="0"+filename;
				filename=folder+topic+"_"+filename+".ndx";
				Index index=new Index(filename,(expectedNumber-1)*MAX_INDEX_SIZE,expectedNumber*MAX_INDEX_SIZE-1,write_mode);
				indexMap.put(expectedNumber,index);
				currentIndexNumber=expectedNumber;
			}
		}
	}

	public void run(){
		while(shouldBeRunning){
			while(System.currentTimeMillis()-lastCommitTime<MAX_COMMIT_LAG){
				try{
					long delta=System.currentTimeMillis()-lastCommitTime;
					if(delta>0){
						Thread.sleep(MAX_COMMIT_LAG-delta);
					}
				}catch(Exception e){}
			}
			commitData();
		}
		commitData();
		isRunning=false;
	}

	public void stop(){
		shouldBeRunning=false;
		while(isRunning){
			try{
				Thread.sleep(25);
			}catch(Exception e){}
		}
		for(Index index:indexMap.values()){
			try{
				index.close();
			}catch(Exception e){}
		}
		for(Block block:blockMap.values()){
			try{
				block.close();
			}catch(Exception e){}
		}
	}

	private void waitForCommit(long location){
		if(commitedLocation>=location)return;
		synchronized(this){
			if(currentLocation-commitedLocation>MAX_COMMIT_BATCH){
				commitData();
			}else{
				while(commitedLocation<location){
					try{
						this.wait();
					}catch(Exception e){

					}
				}
			}
		}
	}

	private void commitData(){
		synchronized(this){
			if(commitedLocation==currentLocation){
				lastCommitTime=System.currentTimeMillis();
				return;
			}
			try{
				blockMap.get(currentBlockNumber).commit();
				Index index=indexMap.get(currentIndexNumber);
				index.commitData();
				commitedLocation=currentLocation;
			}catch(Exception e){
				e.printStackTrace();
				// TODO: this REALLY needs to be handled
			}
			this.notifyAll();
			lastCommitTime=System.currentTimeMillis();
		}
	}

	public void addDocuments(List<Document> batch) throws IOException{
		addDocuments(batch,write_mode);
	}

	public void addDocuments(List<Document> batch,int wmode) throws IOException{
		final int batchCount=batch.size();
		long[] location=null;
		short blockNumber=currentBlockNumber;
		Block block=blockMap.get(blockNumber);
		long[] offset=new long[batchCount];
		Index index=null;
		long maxLocation=-1;
		long totalSize=0;

		// Collect batch of data
		int batchSize=0;
		int[] batchOffsets=new int[batchCount];
		for(Document doc:batch){
			batchSize+=doc.getDataSize();
		}
		byte[] fullData=new byte[batchSize];
		int currentBatchOffset=0;
		int[] sizeList=new int[batchCount];
		for(int i=0;i<batchCount;i++){
			Document doc=batch.get(i);
			byte[] data=doc.getData();
			sizeList[i]=doc.getDataSize();
			batchOffsets[i]=currentBatchOffset;
			System.arraycopy(data,0,fullData,currentBatchOffset,data.length);
			currentBatchOffset+=data.length;
		}
		long[] offsetList=new long[batchCount];
		
		if(wmode==LINEAR){
			synchronized(topic){
				long outputOffset=block.write(fullData);
				index=indexMap.get(currentIndexNumber);
				if(index.hasCapacityFor(batchCount)){
					for(int i=0;i<batchCount;i++){
						offsetList[i]=outputOffset+batchOffsets[i];
					}
					location=index.addEntries(blockNumber,offsetList,sizeList);
					for(int i=0;i<location.length;i++){
						batch.get(i).setLocation(location[i]);
					}
					currentLocation=location[location.length-1];
					if(index.isFull()){
						createNewIndex(currentIndexNumber);
					}
				}else{
					location=new long[batchCount];
					for(int i=0;i<batchCount;i++){
						Document doc=batch.get(i);
						index=indexMap.get(currentIndexNumber);
						location[i]=index.addEntry(blockNumber,outputOffset+batchOffsets[i],doc.getDataSize());
						currentLocation=location[i];
						if(index.isFull()){
							createNewIndex(currentIndexNumber);
						}
						doc.setLocation(location[i]);
					}
				}
			}
		}else{
			long outputOffset=block.write(fullData);
			synchronized(topic){
				index=indexMap.get(currentIndexNumber);
				if(index.hasCapacityFor(batchCount)){
					for(int i=0;i<batchCount;i++){
						offsetList[i]=outputOffset+batchOffsets[i];
					}
					location=index.addEntries(blockNumber,offsetList,sizeList);
					for(int i=0;i<location.length;i++){
						batch.get(i).setLocation(location[i]);
					}
					currentLocation=location[location.length-1];
					if(index.isFull()){
						createNewIndex(currentIndexNumber);
					}
				}else{
					location=new long[batchCount];
					for(int i=0;i<batchCount;i++){
						Document doc=batch.get(i);
						index=indexMap.get(currentIndexNumber);
						location[i]=index.addEntry(blockNumber,outputOffset+batchOffsets[i],doc.getDataSize());
						currentLocation=location[i];
						if(index.isFull()){
							createNewIndex(currentIndexNumber);
							index=indexMap.get(currentIndexNumber);
						}
						doc.setLocation(location[i]);
					}
				}
			}
		}
		if(wmode==FLUSH){
			commitData();
		}else if(wmode<NONE){
			waitForCommit(location[batch.size()-1]);
		}

		if(offset[batchCount-1]+batch.get(batchCount-1).getDataSize()>MAX_BLOCK_SIZE){
			createNewBlock(blockNumber);
		}
	}

	public void addDocument(Document doc) throws IOException{
		addDocument(doc,write_mode);
	}

	public void addDocument(Document doc,int wmode) throws IOException{
		byte[] data=doc.getData();
		long location=-1;
		short blockNumber=currentBlockNumber;
		Block block=blockMap.get(blockNumber);
		long offset=0;
		Index index=null;
		// TODO: find a way to do this more elegantly instead of a copy pasted code block
		if(wmode==LINEAR){
			synchronized(topic){
				offset=block.write(data);
				index=indexMap.get(currentIndexNumber);
				location=index.addEntry(blockNumber,offset,data.length);
				currentLocation=location;
			}
		}else{
			offset=block.write(data);
			synchronized(topic){
				index=indexMap.get(currentIndexNumber);
				location=index.addEntry(blockNumber,offset,data.length);
				currentLocation=location;
			}
		}
		// TODO: verify that we are not getting into issues with the memory model and the index.isFull call
		if(index.isFull()){
			createNewIndex(currentIndexNumber);
		}
		doc.setLocation(location);
		if(wmode==FLUSH){
			commitData();
		}else if(wmode<NONE){
			waitForCommit(location);
		}
		if(offset+data.length>MAX_BLOCK_SIZE){
			createNewBlock(blockNumber);
		}
	}

	private Document getDocument(short blockNumber,long offset, int size, long location) throws IOException{
		Block block=blockMap.get(blockNumber);
		byte[] buffer=block.read(offset,size);
		try{
			return new Document(topic,buffer,location);
		}catch(Exception e){
			// TODO: handle this better
			e.printStackTrace();
		}
		return null;
	}

	public Document getDocument(IndexEntry entry) throws IOException{
		return getDocument(entry.getBlock(),entry.getOffset(),entry.getSize(),entry.getLocation());
	}

	public Document getDocument(long location) throws IOException{
		if(location<0)return null;
		if(location>currentLocation)return null;
		waitForCommit(location);
		Index index=getIndexForLocation(location);
		IndexEntry entry=index.seekEntry(location);
		return getDocument(entry.getBlock(),entry.getOffset(),entry.getSize(),location);
	}

	private void getSequentialDocuments(List<Document> list,List<IndexEntry> indexList) throws IOException{
		if(indexList.size()==1){
			IndexEntry entry=indexList.get(0);
			list.add(getDocument(entry));
			return;
		}
		IndexEntry first=indexList.get(0);
		IndexEntry last=indexList.get(indexList.size()-1);
		int size=(int)((last.getOffset()+last.getSize())-first.getOffset());
		Block block=blockMap.get(first.getBlock());
		byte[] buffer=block.read(first.getOffset(),size);
		for(IndexEntry entry:indexList){
			list.add(new Document(topic,buffer,(int)(entry.getOffset()-first.getOffset()),entry.getSize(),entry.getLocation()));
		}
	}

	private void getDocuments(List<IndexEntry> indexList,List<Document> list) throws IOException{
		List<IndexEntry> sequential=new ArrayList<IndexEntry>();
		IndexEntry entry=indexList.get(0);
		short block=entry.getBlock();
		long offset=entry.getOffset()+entry.getSize();
		sequential.add(entry);
		for(int i=1;i<indexList.size();i++){
			entry=indexList.get(i);
			if(entry.getBlock()!=block || entry.getOffset()!=offset){
				getSequentialDocuments(list,sequential);
				sequential.clear();	
			}
			sequential.add(entry);
			block=entry.getBlock();
			offset=entry.getOffset()+entry.getSize();

		}
		getSequentialDocuments(list,sequential);
	}

	public List<Document> getDocuments(long startLocation,long endLocation) throws IOException{
		if(startLocation<0)startLocation=0;
		if(startLocation>currentLocation)return new LinkedList<Document>();
		if(endLocation>currentLocation)endLocation=currentLocation;
		waitForCommit(endLocation);
		List<Document> list=new ArrayList<Document>((int)(endLocation-startLocation+1));
		Index index=getIndexForLocation(startLocation);
		long currentStartLocation=startLocation;
		long currentEndLocation=endLocation;
		if(currentEndLocation>index.getEndLocationRange()){
			currentEndLocation=index.getEndLocationRange();
		}
		while(true){
			List<IndexEntry> indexList=index.seekEntries(currentStartLocation,currentEndLocation);
			getDocuments(indexList,list);
			if(currentEndLocation==endLocation){
				break;
			}else{
				currentStartLocation=currentEndLocation+1;
				currentEndLocation=endLocation;
				index=getIndexForLocation(currentStartLocation);
				if(currentEndLocation>index.getEndLocationRange()){
					currentEndLocation=index.getEndLocationRange();
				}
			}
		}
		return list;
	}

	public void truncate(long truncateindex) throws IOException{
		// TODO: implement me
		// 1. Acquire full lock
		// 2. If index is zero - dump everything
		// 3. Else find index file for index
		//    3.1 Truncate correct file
		//	  3.2 Delete all later files
		//    3.3 Possibly detect and remove full block files
		synchronized(this){
			// TODO: go through execution paths and make sure we can't get into a deadlock!
			synchronized(topic){
				if(truncateindex==0){
					// Make a clean wipe of indexes and block files
					for(Block block:blockMap.values()){
						block.delete();
					}
					for(Index index:indexMap.values()){
						index.delete();
					}
					// Reset state
					indexMap=new Hashtable<Short,Index>();
					blockMap=new Hashtable<Short,Block>();
					currentBlockNumber=0;
					currentIndexNumber=0;
					commitedLocation=-1;
					currentLocation=-1;
					createNewBlock(currentBlockNumber);
					createNewIndex(currentIndexNumber);
					Index index=indexMap.get(currentIndexNumber);
					currentLocation=index.getLastLocation();
					commitedLocation=currentLocation;
				}else{
					Index index=indexMap.get(currentIndexNumber);
					while(index.getStartLocationRange()>truncateindex){
						// delete full index and remove from map
						index.delete();
						indexMap.remove(currentIndexNumber);
						currentIndexNumber--;
						index=indexMap.get(currentIndexNumber);
					}
					index.truncate(truncateindex);
					currentLocation=index.getLastLocation();
					commitedLocation=currentLocation;
				}
			}
		}
	}

	public JSONObject toJSON() throws JSONException{
		JSONObject obj=new JSONObject();
		obj.put("topic",getTopic());
		obj.put("count",getCount());
		obj.put("size",getSize());
		return obj;
	}
}