package me.doubledutch.lazy;

public final class LazyException extends RuntimeException{
	private int position;
	private String message;
	
	public LazyException(String str){
		super(str);
		this.message=str;
	}

	public LazyException(String str,int position){
		super(str);
		this.position=position;
		this.message=str;
	}

	public LazyException(String str,LazyToken token){
		super(str);
		this.position=token.startIndex;
		this.message=str;
	}

	public String toString(){
		if(position>-1){
			return position+":"+message;
		}
		return message;
	}
}