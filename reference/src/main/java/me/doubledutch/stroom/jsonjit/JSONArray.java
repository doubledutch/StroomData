package me.doubledutch.stroom.jsonjit;

public class JSONArray{
	private JSONToken root;
	private String source;

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
		if(root.children==null){
			return 0;
		}
		return root.children.size();
	}

	public JSONObject getJSONObject(int index){
		if(root.children==null){
			return null;
		}
		if(root.children.size()-1<index){
			return null;
		}
		JSONToken obj=root.children.get(index);
		if(obj.type!=JSONToken.OBJECT){
			// Throw error
		}
		return new JSONObject(obj,source);
	}

	public String getString(int index){
		JSONToken token=getValueToken(index);
		if(token!=null){
			String value=getString(token);
			return value;
			// if(value.startsWith("\"") && value.endsWith("\"")){
				// TODO: unescape value
			//	return value.substring(1,value.length()-1);
			//}
		}
		return null;
	}

	public int getInt(int index){
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
		if(index<root.children.size()){
			return root.children.get(index);
		}
		return null;
	}

	private String getString(JSONToken token){
		return source.substring(token.startIndex,token.endIndex);
	}

	// private String getQuotedString(JSONToken token){
	// 	return source.substring(token.startIndex+1,token.endIndex-1);
	// }

}