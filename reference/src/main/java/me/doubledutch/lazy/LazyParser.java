package me.doubledutch.lazy;

import java.util.*;

public final class LazyParser{
	// private final String source;
	protected LazyToken root;
	protected char[] cbuf;
	protected int length;

	protected LazyParser(final String source){
		// this.source=source;
		length=source.length();
		cbuf=new char[length];
		source.getChars(0,length,cbuf,0);
	}

	private final int NONE=0;
	private final int FIELD=1;
	private final int FIELD_INESCAPE=2;
	private final int VALUE_SEPARATOR=3;
	private final int VALUE=4;
	private final int STRING=5;
	private final int STRING_INESCAPE=6;

	private int state=NONE;

	// Internal stack implementation
	private final LazyToken[] stack=new LazyToken[64];
	private LazyToken stackTop=null;
	private int stackPointer=1;

	private void push(LazyToken token){
		stackTop.addChild(token);
		stack[stackPointer++]=token;
		stackTop=token;
	}

	private LazyToken pop(){
		LazyToken value=stackTop;
		stackPointer--;
		stackTop=stack[stackPointer-1];
		return value;
	}

	private LazyToken peek(){
		return stackTop;
	}

	private int size(){
		return stackPointer-1;
	}

	private final int consumeWhiteSpace(final char[] cbuf, int index){
		char c=cbuf[index];
		while(c==' '|| c=='\n' || c=='\t' || c=='\r'){
			index++;
			c=cbuf[index];
		}
		return index;
	}

	protected void tokenize() throws LazyException{
		// int length=source.length();
		// char[] cbuf=new char[length];
		// source.getChars(0,length,cbuf,0);
		int preIndex=consumeWhiteSpace(cbuf,0);
		char c=cbuf[preIndex];
		if(c=='{'){
			stack[stackPointer++]=LazyToken.cObject(preIndex);
		}else if(c=='['){
			stack[stackPointer++]=LazyToken.cArray(preIndex);
		}else{
			throw new LazyException("Can not parse raw JSON value, must be either object or array",0);
		}
		root=stack[1];
		stackTop=root;
		preIndex++;
		LazyToken token=null;
		for(int n=preIndex;n<length;n++){
			c=cbuf[n];
			switch(state){
				case NONE:
					switch(c){
						case '{':
							push(LazyToken.cObject(n));
							break;
						case '}':
							token=pop();

							if(token.type!=LazyToken.OBJECT){
								if(token.endIndex==-1){
									token.endIndex=n;
								}
								token=pop();
								if(token.type==LazyToken.FIELD){
									token=pop();
								}
								if(token.type!=LazyToken.OBJECT){
									throw new LazyException("Unexpected end of object",n);
								}
							}
							token.endIndex=n+1;
							if(stackTop!=null && stackTop.type==LazyToken.FIELD){
								pop();
							}
							break;
					case '"':
						if(stackTop.type==LazyToken.ARRAY){
							state=STRING;
							push(LazyToken.cValue(n+1));
							// Experiment
							n++;
							c=cbuf[n];
							while(c!='"'){
								n++;
								c=cbuf[n];
								if(c=='\\'){
									n+=2;
									c=cbuf[n];
									stackTop.escaped=true;
								}
							}
							state=NONE;
							stackTop.endIndex=n;
							pop();
						}else if(stackTop.type==LazyToken.FIELD){
							state=STRING;
							push(LazyToken.cValue(n+1));

							// Experiment
							n++;
							c=cbuf[n];
							while(c!='"'){
								n++;
								c=cbuf[n];
								if(c=='\\'){
									n+=2;
									c=cbuf[n];
									stackTop.escaped=true;
								}
							}
							state=NONE;
							stackTop.endIndex=n;
							// Remove value again
							pop();
							// Remove field again
							pop();
						}else if(stackTop.type==LazyToken.OBJECT){
							state=FIELD;
							push(LazyToken.cField(n+1));
							// Experiment
							n++;
							c=cbuf[n];
							while(c!='"'){
								n++;
								c=cbuf[n];
								if(c=='\\'){
									n+=2;
									c=cbuf[n];
									stackTop.escaped=true;
								}
							}
							state=VALUE_SEPARATOR;
							stackTop.endIndex=n;
							n=consumeWhiteSpace(cbuf,n+1);
							n--;
						}else{
							// This shouldn't occur should it?
						}
						break;
					case ',':
						// This must be the end of a value and the start of another
						if(stackTop.type==LazyToken.VALUE){
							token=pop();
							if(token.endIndex==-1){
								token.endIndex=n;
							}
							if(stackTop.type==LazyToken.FIELD){
								// This was the end of the value for a field, pop that too
								pop();
							}
						}
						break;
					case '[':
						push(LazyToken.cArray(n));
						break;
					case ']':
						token=pop();
						if(token.type!=LazyToken.ARRAY){
							if(token.endIndex==-1){
								token.endIndex=n;
							}
							token=pop();
							if(token.type!=LazyToken.ARRAY){
								throw new LazyException("Unexpected end of array",n);
							}
						}
						token.endIndex=n+1;
						if(stackTop!=null && stackTop.type==LazyToken.FIELD){
							pop();
						}
						break;
					case ' ':
					case '\t':
					case '\n':
					case '\r':
						if(stackTop!=null && stackTop.type==LazyToken.VALUE){
							token=pop();
							if(token.endIndex==-1){
								token.endIndex=n;
							}
							if(stackTop.type==LazyToken.FIELD){
								// This was the end of the value for a field, pop that too
								pop();
							}else{
								// System.out.println("Not a field");
							}
						}
						// Do nothing
						break;
					default:
						// This must be a new value
						if(stackTop.type==LazyToken.VALUE){
							// We are just collecting more data for the current value

						}else{
							// System.out.println("Starting value with "+c);
							push(LazyToken.cValue(n));
						}
						break;
					}
					break;

				case FIELD_INESCAPE:
					// possibly validate legal escapes
					state=FIELD;
					break;
				case VALUE_SEPARATOR:
					if(c==':'){
						state=NONE;
						n=consumeWhiteSpace(cbuf,n+1);
						n--;
						/*c=cbuf[n+1];
						while(c==' ' || c=='\n' || c=='\t' || c=='\r'){
							n++;
							c=cbuf[n+1];
						}*/
					}else{
						throw new LazyException("Unexpected character! Was expecting field separator ':'",n);
					}
					break;
			}
		}
	}
}