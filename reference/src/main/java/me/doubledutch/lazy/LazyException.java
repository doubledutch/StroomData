package me.doubledutch.lazy;

public final class LazyException extends RuntimeException{
	private int position;
	
	public LazyException(String str){
		super(str);
	}

	public LazyException(String str,int position){
		super(str);
		this.position=position;
	}
}