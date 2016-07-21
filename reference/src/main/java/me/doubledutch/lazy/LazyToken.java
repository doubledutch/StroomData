package me.doubledutch.lazy;

import java.util.*;

public final class LazyToken{
	protected static final int OBJECT=0;
	protected static final int ARRAY=1;
	protected static final int FIELD=2;
	protected static final int VALUE=3;
	protected static final int VALUE_TRUE=4;
	protected static final int VALUE_FALSE=5;
	protected static final int VALUE_NULL=6;

	protected final int startIndex;
	protected int endIndex=-1;

	protected boolean escaped=false;

	protected final int type;

	protected LazyToken child;
	protected LazyToken lastChild;
	protected LazyToken next;

	protected LazyToken(int type,int startIndex){
		this.startIndex=startIndex;
		this.type=type;
	}

	protected void addChild(LazyToken token){
		if(lastChild==null){
			child=token;
			lastChild=token;
			return;
		}
		lastChild.next=token;
		lastChild=token;
	}

	protected int getChildCount(){
		if(child==null){
			return 0;
		}
		int num=0;
		LazyToken token=child;
		while(token!=null){
			num++;
			token=token.next;
		}
		return num;
	}

	protected static LazyToken cArray(int index){
		return new LazyToken(ARRAY,index);
	}
	protected static LazyToken cObject(int index){
		return new LazyToken(OBJECT,index);
	}
	protected static LazyToken cField(int index){
		return new LazyToken(FIELD,index);
	}
	protected static LazyToken cValue(int index){
		return new LazyToken(VALUE,index);
	}
	protected static LazyToken cValueTrue(int index){
		return new LazyToken(VALUE_TRUE,index);
	}
	protected static LazyToken cValueFalse(int index){
		return new LazyToken(VALUE_FALSE,index);
	}
	protected static LazyToken cValueNull(int index){
		return new LazyToken(VALUE_NULL,index);
	}

	protected int getIntValue(char[] source) throws LazyException{
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

	protected long getLongValue(char[] source) throws LazyException{
		int i=startIndex;
		boolean sign=false;
		if(source[i]=='-'){
			sign=true;
			i++;
		}
		long value=0;
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

	protected double getDoubleValue(char[] source) throws LazyException{
		String str=getStringValue(source);
		try{
			double d=Double.parseDouble(str);
			return d;
		}catch(NumberFormatException nfe){
			throw new LazyException("'"+str+"' is not a valid double",startIndex);
		}
	}

	protected String getStringValue(char[] source){
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
						// sb.append((char)Integer.parseInt(this.next(4), 16));
						i+=4;
					}
				}else{
					buf.append(c);
				}
			}
			return buf.toString();
		}
	}

	protected Iterator<String> getFieldIterator(char[] cbuf){
		return new FieldIterator(this,cbuf);
	}

	protected String toString(int pad){
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

	private final class FieldIterator implements Iterator<String>{
		private LazyToken next;
		private char[] cbuf;

		protected FieldIterator(LazyToken token,char[] cbuf){
			next=token.child;
			this.cbuf=cbuf;
		}

		public boolean hasNext(){
			return next!=null;
		}

		public String next() throws NoSuchElementException{
			if(hasNext()){
				String value=next.getStringValue(cbuf);
				next=next.next;
				return value;
			}
			throw new NoSuchElementException();
		}
	}
}