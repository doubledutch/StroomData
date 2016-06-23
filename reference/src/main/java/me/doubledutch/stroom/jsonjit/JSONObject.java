package me.doubledutch.stroom.jsonjit;

public class JSONObject{
	private JSONToken root;
	private String source;

	public JSONObject(String raw) throws Exception{
		JSONParser parser=new JSONParser(raw);
		parser.tokenize();	
		if(parser.root.type!=JSONToken.OBJECT){
			// Throw error
		}
		root=parser.root;
		source=raw;
	}

	protected JSONObject(JSONToken root,String source){
		this.root=root;
		this.source=source;
	}

	public String getString(String key){
		JSONToken token=getFieldToken(key);
		if(token!=null){
			String value=getRawString(token);
			if(value.startsWith("\"") && value.endsWith("\"")){
				// TODO: unescape value
				return value.substring(1,value.length()-1);
			}
		}
		return null;
	}

	public int getInt(String key){
		JSONToken token=getFieldToken(key);
		if(token!=null){
			// TODO: this trim is a hack! fix the parser
			String value=getRawString(token).trim();
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
		JSONToken token=getFieldToken(key);
		if(token!=null){
			String value=getRawString(token).trim();
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

	public JSONObject getJSONObject(String key){
		JSONToken token=getFieldToken(key);
		if(token!=null){
			return new JSONObject(token,source);
		}
		return null;
	}

	private JSONToken getFieldToken(String key){
		for(JSONToken child:root.children){
			if(child.type==JSONToken.FIELD){
				String value=getQuotedString(child);
				if(value.equals(key)){
					JSONToken token=child.children.get(0);
					return token;
				}
			}
		}
		return null;
	}

	private String getRawString(JSONToken token){
		return source.substring(token.startIndex,token.endIndex);
	}

	private String getQuotedString(JSONToken token){
		return source.substring(token.startIndex+1,token.endIndex-1);
	}

	public String toString(int pad){
		return root.toString(pad);
	}

	public String toString(){
		return source.substring(root.startIndex,root.endIndex);
	}
}