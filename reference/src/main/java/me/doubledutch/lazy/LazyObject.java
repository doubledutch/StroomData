package me.doubledutch.lazy;

public class LazyObject{
	private LazyToken root;
	private String source;

	public LazyObject(String raw) throws LazyException{
		LazyParser parser=new LazyParser(raw);
		parser.tokenize();	
		if(parser.root.type!=LazyToken.OBJECT){
			throw new LazyException("JSON Object must start with {",0);
		}
		root=parser.root;
		source=raw;
	}

	protected LazyObject(LazyToken root,String source){
		this.root=root;
		this.source=source;
	}

	public String getString(String key){
		LazyToken token=getFieldToken(key);
		if(token!=null){
			String value=getString(token);
			return value;
			// if(value.startsWith("\"") && value.endsWith("\"")){
				// TODO: unescape value
				//return value.substring(1,value.length()-1);
			//}
		}
		return null;
	}

	public int getInt(String key){
		LazyToken token=getFieldToken(key);
		if(token!=null){
			// TODO: this trim is a hack! fix the parser
			String value=getString(token);
			// System.out.println("'"+value+"'");
			try{
				int ivalue=Integer.parseInt(value);
				return ivalue;
			}catch(Exception e){
				// not a number
			}
		}
		// TODO: Throw exception instead!
		return 0;
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
			return new LazyObject(token,source);
		}
		return null;
	}

	public LazyArray getJSONArray(String key){
		LazyToken token=getFieldToken(key);
		if(token!=null){
			return new LazyArray(token,source);
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
			if(c!=source.charAt(token.startIndex+i)){
				return false;
			}
		}
		return true;
	}

	private LazyToken getFieldToken(String key){
		LazyToken child=root.child;
		while(child!=null){
		// for(JSONToken child:root.children){
			if(child.type==LazyToken.FIELD){
				if(keyMatch(key,child)){
				//String value=getString(child);
				// if(value.equals(key)){
					// JSONToken token=child.children.get(0);
					LazyToken token=child.child;
					return token;
				}
			}
			child=child.next;
		}
		return null;
	}

	private String getString(LazyToken token){
		return source.substring(token.startIndex,token.endIndex);
	}

	// private String getQuotedString(JSONToken token){
	//	return source.substring(token.startIndex,token.endIndex);
	// }

	public String toString(int pad){
		return root.toString(pad);
	}

	public String toString(){
		return source.substring(root.startIndex,root.endIndex);
	}
}