package me.doubledutch.stroom.streams;

import java.io.*;

public class IndexEntry{
	public final static int RECORD_SIZE=2+8+4; // short + long + int
	private long location;
	private short block;
	private long offset;
	private int size;

	public IndexEntry(long location,short block,long offset,int size){
		this.location=location;
		this.block=block;
		this.offset=offset;
		this.size=size;
	}

	public long getLocation(){
		return location;
	}

	public short getBlock(){
		return block;
	}

	public long getOffset(){
		return offset;
	}

	public int getSize(){
		return size;
	}

	public static IndexEntry read(long location,byte[] data, int dataOffset) throws IOException{
		DataInputStream in=new DataInputStream(new ByteArrayInputStream(data,dataOffset,RECORD_SIZE));
		short block=in.readShort();
		long offset=in.readLong();
		int size=in.readInt();
		in.close();
		return new IndexEntry(location,block,offset,size);
	}

	public static IndexEntry read(long location,DataInput in) throws IOException{
		short block=in.readShort();
		long offset=in.readLong();
		int size=in.readInt();
		return new IndexEntry(location,block,offset,size);
	}

	public void write(DataOutput out) throws IOException{
		out.writeShort(block);
		out.writeLong(offset);
		out.writeInt(size);
	}
}