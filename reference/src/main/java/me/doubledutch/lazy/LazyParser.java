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
	private final void consumeWhiteSpace(){
		char c=cbuf[n];
		while(c==' '|| c=='\n' || c=='\t' || c=='\r'){
			n++;
			c=cbuf[n];
		}
	}

	// Attempt to advance and consume any whitespace
	// Roll back the index counter to prepare for another iteration
	// through the main loop afterwards.
	private final void tryToConsumeWhiteSpace(){
		n++;
		consumeWhiteSpace();
		n--;
	}

	// Consume all characters in a string and correctly mark the stackTop
	// element if an escape character is found
	private final void consumeString(){
		n++;
		char c=cbuf[n];
		while(c!='"'){
			n++;
			c=cbuf[n];
			if(c=='\\'){
				n+=2;
				c=cbuf[n];
				stackTop.escaped=true;
			}
		}
	}

	protected void tokenize() throws LazyException{
		consumeWhiteSpace();
		// We are going to manually push the first token onto the stack so
		// future push operations can avoid doing an if empty check when
		// setting the parent child relationship
		char c=cbuf[n];
		if(c=='{'){
			stack[stackPointer++]=LazyToken.cObject(n);
		}else if(c=='['){
			stack[stackPointer++]=LazyToken.cArray(n);
		}else{
			throw new LazyException("Can not parse raw JSON value, must be either object or array",0);
		}
		root=stack[1];
		stackTop=root;
		n++;
		LazyToken token=null;
		for(;n<length;n++){
			c=cbuf[n];
			switch(state){
				case NONE:
					switch(c){
						case '{':
							push(LazyToken.cObject(n));
							break;
						case '}':
							// The end of an object, pop off the last value and field if any
							token=pop();
							if(token.type!=LazyToken.OBJECT){
								if(token.endIndex==-1){
									token.endIndex=n;
								}
								token=pop();
								if(token.type==LazyToken.FIELD){
									token=pop();
								}
								// We should now be down to the actual object
								if(token.type!=LazyToken.OBJECT){
									throw new LazyException("Unexpected end of object",n);
								}
							}
							token.endIndex=n+1;
							// If this object was the value for a field, pop off that field too
							if(stackTop!=null && stackTop.type==LazyToken.FIELD){
								pop();
							}
							break;
					case '"':
						if(stackTop.type==LazyToken.ARRAY){
							push(LazyToken.cValue(n+1));
							consumeString();
							stackTop.endIndex=n;
							// Remove value again
							pop();
						}else if(stackTop.type==LazyToken.FIELD){
							push(LazyToken.cValue(n+1));
							consumeString();
							stackTop.endIndex=n;
							// Remove value again
							pop();
							// Remove field
							pop();
						}else if(stackTop.type==LazyToken.OBJECT){
							push(LazyToken.cField(n+1));
							consumeString();
							state=VALUE_SEPARATOR;
							stackTop.endIndex=n;
							tryToConsumeWhiteSpace();
						}else{
							// This shouldn't occur should it?
							throw new LazyException("Syntax error",n);
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
						// If this array was the value for a field, pop off that field too
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
						break;
					default:
						if(stackTop.type==LazyToken.VALUE){
							// We are just collecting more data for the current value

						}else{
							// This must be a new value
							if(c=='n'){
								// Must be null value
								if(cbuf[++n]=='u' && cbuf[++n]=='l' && cbuf[++n]=='l'){
									push(LazyToken.cValueNull(n));
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
								if(cbuf[++n]=='r' && cbuf[++n]=='u' && cbuf[++n]=='e'){
									push(LazyToken.cValueTrue(n));
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
								if(cbuf[++n]=='a' && cbuf[++n]=='l' && cbuf[++n]=='s' && cbuf[++n]=='e'){
									push(LazyToken.cValueFalse(n));
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
								// TODO: we should really validate the syntax of a number
							}
							
						}
						break;
					}
					break;
				case VALUE_SEPARATOR:
					if(c==':'){
						state=NONE;
						tryToConsumeWhiteSpace();
					}else{
						throw new LazyException("Unexpected character! Was expecting field separator ':'",n);
					}
					break;
			}
		}
	}
}