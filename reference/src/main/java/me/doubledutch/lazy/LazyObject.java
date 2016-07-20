package me.doubledutch.lazy;

public class LazyObject{
	private LazyToken root;
	// private String source;
	private char[] cbuf;

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

	public String getString(String key){
		LazyToken token=getFieldToken(key);
		if(token!=null){
			String value=getString(token);
			return value;
		}
		return null;
	}

	public int getInt(String key) throws LazyException{
		LazyToken token=getFieldToken(key);
		if(token!=null){
			return token.getIntValue(cbuf);
			/*
			String value=getString(token);
			try{
				int ivalue=Integer.parseInt(value);
				return ivalue;
			}catch(Exception e){
				// not a number
			}*/
		}else{
			throw new LazyException("Unknown field");
		}
		// TODO: Throw exception instead!
		// return 0;
	}

	public long getLong(String key){
		LazyToken token=getFieldToken(key);
		if(token!=null){
			String value=getString(token);
			try{
				long lvalue=Long.parseLong(value);
				return lvalue;
			}catch(Exception e){
				// not a number
			}
		}
		// TODO: Throw exception instead!
		return 0l;
	}

	public LazyObject getJSONObject(String key){
		LazyToken token=getFieldToken(key);
		if(token!=null){
			return new LazyObject(token,cbuf);
		}
		return null;
	}

	public LazyArray getJSONArray(String key){
		LazyToken token=getFieldToken(key);
		if(token!=null){
			return new LazyArray(token,cbuf);
		}
		return null;
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

	private LazyToken getFieldToken(String key){
		LazyToken child=root.child;
		while(child!=null){
			if(child.type==LazyToken.FIELD){
				if(keyMatch(key,child)){
					LazyToken token=child.child;
					return token;
				}
			}
			child=child.next;
		}
		return null;
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