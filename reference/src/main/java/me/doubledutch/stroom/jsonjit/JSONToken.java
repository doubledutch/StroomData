package me.doubledutch.stroom.jsonjit;

import java.util.*;

public class JSONToken{
	public static final int OBJECT=0;
	public static final int ARRAY=1;
	public static final int FIELD=2;
	public static final int VALUE=3;

	public int startIndex=-1;
	public int endIndex=-1;
	public int type;
	public List<JSONToken> children=null;
	public JSONToken child;

	public JSONToken(int type,int startIndex){
		this.startIndex=startIndex;
		this.type=type;
		if(type==OBJECT || type==ARRAY){
			children=new ArrayList<JSONToken>();
		}
	}

	public void addChild(JSONToken token){
		if(children==null){
			child=token;
			return;
		}
		/*if(children==null){
			children=new ArrayList<JSONToken>();
		}*/
		children.add(token);
	}

	public static JSONToken cArray(int index){
		return new JSONToken(ARRAY,index);
	}
	public static JSONToken cObject(int index){
		return new JSONToken(OBJECT,index);
	}
	public static JSONToken cField(int index){
		return new JSONToken(FIELD,index);
	}
	public static JSONToken cValue(int index){
		return new JSONToken(VALUE,index);
	}

	public String toString(int pad){
		String out="";
		for(int i=0;i<pad;i++)out+=" ";
		if(type==OBJECT){
			out+="{";
		}else if(type==ARRAY){
			out+="[";
		}else if(type==FIELD){
			out+="\"";
		}else if(type==VALUE){
			out+="V";
		}
		out+=":["+startIndex+","+endIndex+"]";
		out+="\n";
		if(children!=null){
			for(JSONToken token:children){
				out+=token.toString(pad+2);
			}
		}
		return out;
	}
}