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

	public int getIntValue(char[] source) throws LazyException{
		int i=startIndex;
		boolean sign=false;
		if(source[i]=='-'){
			sign=true;
			i++;
		}
		int value=0;
		for(;i<endIndex;i++){
			char c=source[i];
			if(c<'0'||c>'9')throw new LazyException("'"+getStringValue(source)+"' is not a valid integer",startIndex);
			value+='0'-c;
			if(i+1<endIndex){
				value*=10;
			}
		}
		return sign?value:-value;
	}

	public String getStringValue(char[] source){
		if(!escaped){
			return new String(source,startIndex,endIndex-startIndex);
		}else{
			StringBuilder buf=new StringBuilder(endIndex-startIndex);
			for(int i=startIndex;i<endIndex;i++){
				char c=source[i];
				if(c=='\\'){
					i++;
					c=source[i];
					if(c=='"' || c=='\\' || c=='/'){
						buf.append(c);
					}else if(c=='b'){
						buf.append("\b");
					}else if(c=='f'){
						buf.append("\f");
					}else if(c=='n'){
						buf.append("\n");
					}else if(c=='r'){
						buf.append("\r");
					}else if(c=='u'){
						String code=new String(source,i,i+4);
						// TODO: extract and add properly to string
						i+=4;
					}
				}else{
					buf.append(c);
				}
			}
			return buf.toString();
		}
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