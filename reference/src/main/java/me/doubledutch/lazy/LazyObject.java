package me.doubledutch.lazy;

import java.util.Iterator;

public class LazyObject{
	private LazyToken root;
	// private String source;
	private char[] cbuf;
	private int length=-1;

	public LazyObject(String raw) throws LazyException{
		LazyParser parser=new LazyParser(raw);
		parser.tokenize();	
		if(parser.root.type!=LazyToken.OBJECT){
			throw new LazyException("JSON Object must start with {",0);
		}
		root=parser.root;
		cbuf=parser.cbuf;
		// source=raw;
	}

	protected LazyObject(LazyToken root,char[] source){
		this.root=root;
		this.cbuf=source;
	}

	public String getString(String key) throws LazyException{
		LazyToken token=getFieldToken(key);
		return token.getStringValue(cbuf);
	}

	public int getInt(String key) throws LazyException{
		LazyToken token=getFieldToken(key);
		return token.getIntValue(cbuf);
	}

	public long getLong(String key) throws LazyException{
		LazyToken token=getFieldToken(key);
		return token.getLongValue(cbuf);
	}

	public double getDouble(String key) throws LazyException{
		LazyToken token=getFieldToken(key);
		return token.getDoubleValue(cbuf);
	}

	public boolean isNull(String key){
		LazyToken token=getFieldToken(key);
		if(token.type==LazyToken.VALUE_NULL)return true;
		return false;
	}

	public boolean getBoolean(String key){
		LazyToken token=getFieldToken(key);
		if(token.type==LazyToken.VALUE_TRUE)return true;
		if(token.type==LazyToken.VALUE_FALSE)return false;
		throw new LazyException("Requested value is not a boolean",token);
	}

	public LazyObject getJSONObject(String key) throws LazyException{
		LazyToken token=getFieldToken(key);
		if(token.type!=LazyToken.OBJECT)throw new LazyException("Requested value is not an object",token);
		return new LazyObject(token,cbuf);
	}

	public LazyArray getJSONArray(String key) throws LazyException{
		LazyToken token=getFieldToken(key);
		if(token.type!=LazyToken.ARRAY)throw new LazyException("Requested value is not an array",token);
		return new LazyArray(token,cbuf);
	}

	private boolean keyMatch(String key,LazyToken token){
		int length=key.length();
		if(token.endIndex-token.startIndex!=length){
			return false;
		}
		for(int i=0;i<length;i++){
			char c=key.charAt(i);
			if(c!=cbuf[token.startIndex+i]){
				return false;
			}
		}
		return true;
	}

	public boolean has(String key){
		LazyToken child=root.child;
		while(child!=null){
			if(child.type==LazyToken.FIELD){
				if(keyMatch(key,child)){
					return true;
				}
			}
			child=child.next;
		}
		return false;
	}

	public int length(){
		if(root.child==null){
			return 0;
		}
		if(length>-1){
			return length;
		}
		length=root.getChildCount();
		return length;
	}

	public Iterator<String> keys(){
		return root.getFieldIterator(cbuf);
	}

	private LazyToken getFieldToken(String key) throws LazyException{
		LazyToken child=root.child;
		while(child!=null){
			if(child.type==LazyToken.FIELD){
				if(keyMatch(key,child)){
					return child.child;
				}
			}
			child=child.next;
		}
		throw new LazyException("Unknown field '"+key+"'");
	}

	private String getString(LazyToken token){
		return token.getStringValue(cbuf);
	}

	public String toString(int pad){
		return root.toString(pad);
	}

	public String toString(){
		return new String(cbuf,root.startIndex,root.endIndex-root.startIndex);
		// return source.substring(root.startIndex,root.endIndex);
	}
}