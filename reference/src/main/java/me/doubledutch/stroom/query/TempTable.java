package me.doubledutch.stroom.query;

import me.doubledutch.lazyjson.*;

public abstract class TempTable{
	public abstract LazyObject next() throws Exception;
	public abstract boolean hasNext() throws Exception; 
	public abstract void append(LazyObject obj) throws Exception;
	public abstract void delete();
}