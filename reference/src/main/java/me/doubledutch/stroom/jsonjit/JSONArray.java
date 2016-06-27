package me.doubledutch.stroom.jsonjit;

public class JSONArray{
	private JSONToken root;
	private String source;

	private int length=-1;
	private JSONToken selectToken=null;
	private int selectInt=-1;

	public JSONArray(String raw) throws Exception{
		JSONParser parser=new JSONParser(raw);
		parser.tokenize();	
		if(parser.root.type!=JSONToken.ARRAY){
			// Throw error
		}
		root=parser.root;
		source=raw;
	}

	protected JSONArray(JSONToken root,String source){
		this.root=root;
		this.source=source;
	}

	public int length(){
		if(root.child==null){
			return 0;
		}
		if(length>-1){
			return length;
		}
		int num=0;
		JSONToken token=root.child;
		while(token!=null){
			num++;
			token=token.next;
		}
		length=num;
		return num;
	}

	public JSONObject getJSONObject(int index){
		JSONToken token=getValueToken(index);
		if(token!=null){
			if(token.type!=JSONToken.OBJECT){
				// Throw error
			}
			return new JSONObject(token,source);
		}
		return null;
	}

	public String getString(int index){
		JSONToken token=getValueToken(index);
		if(token!=null){
			String value=getString(token);
			// System.out.println("'"+value+"'");
			return value;
			// if(value.startsWith("\"") && value.endsWith("\"")){
				// TODO: unescape value
			//	return value.substring(1,value.length()-1);
			//}
		}
		return null;
	}

	public int getInt(int index){
		// System.out,println("requesting token for "+index);
		JSONToken token=getValueToken(index);
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

	private JSONToken getValueToken(int index){
		int num=0;
		JSONToken child=root.child;
		if(selectInt>-1 && index>=selectInt){
			num=selectInt;
			child=selectToken;
		}
		while(child!=null){
			if(num==index){
				selectInt=index;
				selectToken=child;
				return child;
			}
			num++;
			child=child.next;
		}
		// if(index<root.children.size()){
		//	return root.children.get(index);
		// }
		return null;
	}

	private String getString(JSONToken token){
		return source.substring(token.startIndex,token.endIndex);
	}

	public String toString(int pad){
		return root.toString(pad);
	}

	// private String getQuotedString(JSONToken token){
	// 	return source.substring(token.startIndex+1,token.endIndex-1);
	// }

}