package me.doubledutch.lazy;

import java.util.*;

public final class LazyParser{
	protected LazyToken root;
	protected final char[] cbuf;
	protected final int length;
	private int n=0;

	protected LazyParser(final String source){
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

	// The parser uses a crude stack while parsing that maintains a reference
	// to the top element on the stack and automatically establishes a parent
	// child relation ship when elements are pushed onto the stack.
	private final LazyToken[] stack=new LazyToken[128];
	private LazyToken stackTop=null;
	private int stackPointer=1;

	// Push a token onto the stack and attach it to the previous top as a child
	private void push(LazyToken token){
		stackTop.addChild(token);
		stack[stackPointer++]=token;
		stackTop=token;
	}

	// Pop a token off the stack and reset the stackTop pointer
	private LazyToken pop(){
		LazyToken value=stackTop;
		stackPointer--;
		stackTop=stack[stackPointer-1];
		return value;
	}

	// return the stackTop pointer
	private final LazyToken peek(){
		return stackTop;
	}

	private int size(){
		return stackPointer-1;
	}

	// Utility method to consume sections of whitespace
	private final int consumeWhiteSpace(final char[] cbuf, int index){
		char c=cbuf[index];
		while(c==' '|| c=='\n' || c=='\t' || c=='\r'){
			index++;
			c=cbuf[index];
		}
		return index;
	}

	protected void tokenize() throws LazyException{
		int preIndex=consumeWhiteSpace(cbuf,0);
		// We are going to manually push the first token onto the stack so
		// future push operations can avoid doing an if empty check when
		// setting the parent child relationship
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
		for(n=preIndex;n<length;n++){
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
							if(c=='n'){
								// Must be null value
								if(cbuf[n+1]=='u' && cbuf[n+2]=='l' && cbuf[n+3]=='l'){
									push(LazyToken.cValueNull(n));
									n+=4;
									token=pop();
									token.endIndex=n;
									if(stackTop.type==LazyToken.FIELD){
										// This was the end of the value for a field, pop that too
										pop();
									}
								}else{
									throw new LazyException("Syntax error",n);
								}
							}else if(c=='t'){
								// Must be true value
								if(cbuf[n+1]=='r' && cbuf[n+2]=='u' && cbuf[n+3]=='e'){
									push(LazyToken.cValueTrue(n));
									n+=4;
									token=pop();
									token.endIndex=n;
									if(stackTop.type==LazyToken.FIELD){
										// This was the end of the value for a field, pop that too
										pop();
									}
								}else{
									throw new LazyException("Syntax error",n);
								}
							}else if(c=='f'){
								// Must be false value
								if(cbuf[n+1]=='a' && cbuf[n+2]=='l' && cbuf[n+3]=='s' && cbuf[n+4]=='e'){
									push(LazyToken.cValueFalse(n));
									n+=5;
									token=pop();
									token.endIndex=n;
									if(stackTop.type==LazyToken.FIELD){
										// This was the end of the value for a field, pop that too
										pop();
									}
								}else{
									throw new LazyException("Syntax error",n);
								}
							}else if(c=='-' || !(c<'0' || c>'9')){
								// Must be a number
								push(LazyToken.cValue(n));
							}
							
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