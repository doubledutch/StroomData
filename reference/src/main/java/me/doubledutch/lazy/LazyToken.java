package me.doubledutch.lazy;

import java.util.*;

public final class LazyToken{
	public static final int OBJECT=0;
	public static final int ARRAY=1;
	public static final int FIELD=2;
	public static final int VALUE=3;

	public final int startIndex;
	public int endIndex=-1;

	public boolean escaped=false;

	public final int type;

	public LazyToken child;
	public LazyToken lastChild;
	public LazyToken next;

	public LazyToken(int type,int startIndex){
		this.startIndex=startIndex;
		this.type=type;
	}

	public void addChild(LazyToken token){
		if(lastChild==null){
			child=token;
			lastChild=token;
			return;
		}
		lastChild.next=token;
		lastChild=token;
	}

	public static LazyToken cArray(int index){
		return new LazyToken(ARRAY,index);
	}
	public static LazyToken cObject(int index){
		return new LazyToken(OBJECT,index);
	}
	public static LazyToken cField(int index){
		return new LazyToken(FIELD,index);
	}
	public static LazyToken cValue(int index){
		return new LazyToken(VALUE,index);
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
		if(child!=null){
			LazyToken token=child;
			while(token!=null){
				out+=token.toString(pad+2);
				token=token.next;
			}
		}else{
			if(child!=null){
				out+=child.toString(pad+2);
			}
		}
		return out;
	}
}