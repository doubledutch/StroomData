package me.doubledutch.stroom.jsonjit;

import java.util.*;

public class JSONParser{
	private final String source;
	List<JSONToken> tokens=new ArrayList<JSONToken>();
	protected JSONToken root;

	public JSONParser(final String source){
		this.source=source;
	}

	public JSONObject parseObject() throws Exception{
		tokenize();	
		if(root.type!=JSONToken.OBJECT){
			// Throw error
		}
		return new JSONObject(root,source);	
	}

	public JSONArray parseArray() throws Exception{
		tokenize();	
		if(root.type!=JSONToken.ARRAY){
			// Throw error
		}
		return new JSONArray(root,source);
	}


	private final int NONE=0;
	private final int FIELD=1;
	private final int FIELD_INESCAPE=2;
	private final int VALUE_SEPARATOR=3;
	private final int VALUE=4;
	private final int STRING=5;
	private final int STRING_INESCAPE=6;

	private int state=NONE;

	private final JSONToken[] stack=new JSONToken[64];
	private JSONToken stackTop=null;
	private int stackPointer=1;


	private void push(JSONToken token){
		// JSONToken parent=peek();
		stackTop.addChild(token);
		stack[stackPointer++]=token;
		stackTop=token;
	}

	private JSONToken pop(){
		// System.out.println("pre pop "+stackPointer);
		JSONToken value=stackTop;
		stackPointer--;
		// System.out.println("new top "+(stackPointer-1));
		stackTop=stack[stackPointer-1];
		return value;
	}

	private JSONToken peek(){
		// return stack[stackPointer-1];
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

	protected void tokenize() throws Exception{
		int length=source.length();
		char[] cbuf=new char[length];
		source.getChars(0,length,cbuf,0);
		int preIndex=consumeWhiteSpace(cbuf,0);
		// char c=source.charAt(preIndex);
		/*char c=cbuf[preIndex];
		while(c==' ' || c=='\n' || c=='\t' || c=='\r'){
			preIndex++;
			c=cbuf[preIndex];
			// c=source.charAt(preIndex);
		}*/
		char c=cbuf[preIndex];
		if(c=='{'){
			stack[stackPointer++]=JSONToken.cObject(preIndex);
		}else if(c=='['){
			stack[stackPointer++]=JSONToken.cArray(preIndex);
		}else{
			// throw error
		}
		root=stack[1];
		stackTop=root;
		preIndex++;
		for(int n=preIndex;n<length;n++){
			c=cbuf[n];
			// c=source.charAt(n);
			switch(state){
				case NONE:
					// buf=new StringBuilder();
					if(c=='{'){
						push(JSONToken.cObject(n));
					}else if(c=='}'){
						JSONToken token=pop();

						if(token.type!=JSONToken.OBJECT){
							if(token.endIndex==-1){
								token.endIndex=n;
							}
							token=pop();
							if(token.type==JSONToken.FIELD){
								token=pop();
							}
							if(token.type!=JSONToken.OBJECT){
								// Throw error
							}
						}else{

						}
						token.endIndex=n+1;
						if(stackTop!=null && stackTop.type==JSONToken.FIELD){
							pop();
						}
						// Error check that its {
					}else if(c=='"'){
						// JSONToken token=peek();
						if(stackTop.type==JSONToken.ARRAY){
							state=STRING;
							push(JSONToken.cValue(n+1));
							// Experiment
							n++;
							c=cbuf[n];
							// c=source.charAt(n);
							while(c!='"'){
								n++;
								c=cbuf[n];
								// c=source.charAt(n);
								if(c=='\\'){
									n+=2;
									c=cbuf[n];
									// c=source.charAt(n);
								}
							}
							state=NONE;
							stackTop.endIndex=n;
							pop();
						}else if(stackTop.type==JSONToken.FIELD){
							state=STRING;
							push(JSONToken.cValue(n+1));

							// Experiment
							n++;
							c=cbuf[n];
							// c=source.charAt(n);
							while(c!='"'){
								n++;
								c=cbuf[n];
								// c=source.charAt(n);
								if(c=='\\'){
									n+=2;
									c=cbuf[n];
									// c=source.charAt(n);
								}
							}
							state=NONE;
							stackTop.endIndex=n;
							// Remove value again
							pop();
							// Remove field again
							pop();
						}else if(stackTop.type==JSONToken.OBJECT){
							state=FIELD;
							push(JSONToken.cField(n+1));
							// Experiment
							n++;
							c=cbuf[n];
							// c=source.charAt(n);
							while(c!='"'){
								n++;
								c=cbuf[n];
								// c=source.charAt(n);
								if(c=='\\'){
									n+=2;
									c=cbuf[n];
									// c=source.charAt(n);
								}
							}
							state=VALUE_SEPARATOR;
							stackTop.endIndex=n;
						}else{
							// This shouldn't occur should it?
						}
					}else if(c==','){
						// This must be the end of a value and the start of another
						// JSONToken token=peek();
						if(stackTop.type==JSONToken.VALUE){
							JSONToken token=pop();
							if(token.endIndex==-1){
								token.endIndex=n;
							}
							if(stackTop.type==JSONToken.FIELD){
								// This was the end of the value for a field, pop that too
								pop();
							}else{
								// System.out.println("Not a field");
							}
						}
					}else if(c=='['){
						push(JSONToken.cArray(n));
					}else if(c==']'){
						JSONToken token=pop();
						if(token.type!=JSONToken.ARRAY){
							if(token.endIndex==-1){
								token.endIndex=n;
							}
							token=pop();
							if(token.type!=JSONToken.ARRAY){
								// Throw error
							}
						}
						token.endIndex=n+1;
					}else if(c==' ' || c=='\t' || c=='\n' || c=='\r'){
						if(stackTop!=null && stackTop.type==JSONToken.VALUE){
							JSONToken token=pop();
							if(token.endIndex==-1){
								token.endIndex=n;
							}
							if(stackTop.type==JSONToken.FIELD){
								// This was the end of the value for a field, pop that too
								pop();
							}else{
								// System.out.println("Not a field");
							}
						}
						// Do nothing

					}else{
						// This must be a new value
						// JSONToken token=peek();
						if(stackTop.type==JSONToken.VALUE){
							// We are just collecting more data for the current value

						}else{
							// System.out.println("Starting value with "+c);
							push(JSONToken.cValue(n));
						}
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
						// Throw error
					}
					break;
			}
		}
	}
}