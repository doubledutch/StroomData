package me.doubledutch.stroom.jsonjit;

import java.util.*;

public class JSONParser{
	private String source;
	List<JSONToken> tokens=new ArrayList<JSONToken>();
	protected JSONToken root=null;

	public JSONParser(String source){
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

	protected void tokenize() throws Exception{
		int length=source.length();
		char[] cbuf=new char[length];
		source.getChars(0,length,cbuf,0);
		int preIndex=0;
		char c=source.charAt(preIndex);
		while(c==' ' || c=='\n' || c=='\t' || c=='\r'){
			preIndex++;
			c=cbuf[preIndex];
		}
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
						}
						token.endIndex=n+1;
						// Error check that its {
					}else if(c=='"'){
						// JSONToken token=peek();
						if(stackTop.type==JSONToken.ARRAY){
							state=STRING;
							push(JSONToken.cValue(n));
							// Experiment
							n++;
							c=cbuf[n];
							while(c!='"'){
								n++;
								c=cbuf[n];
								if(c=='\\'){
									n+=2;
									c=cbuf[n];
								}
							}
							state=NONE;
							stackTop.endIndex=n+1;
						}else if(stackTop.type==JSONToken.FIELD){
							state=STRING;
							push(JSONToken.cValue(n));

							// Experiment
							n++;
							c=cbuf[n];
							while(c!='"'){
								n++;
								c=cbuf[n];
								if(c=='\\'){
									n+=2;
									c=cbuf[n];
								}
							}
							state=NONE;
							stackTop.endIndex=n+1;
						}else if(stackTop.type==JSONToken.OBJECT){
							state=FIELD;
							push(JSONToken.cField(n));
							// Experiment
							n++;
							c=cbuf[n];
							while(c!='"'){
								n++;
								c=cbuf[n];
								if(c=='\\'){
									n+=2;
									c=cbuf[n];
								}
							}
							state=VALUE_SEPARATOR;
							stackTop.endIndex=n+1;
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
				case FIELD:
					if(c=='"'){
						state=VALUE_SEPARATOR;
						// JSONToken token=peek();
						stackTop.endIndex=n+1;
						// Error check that its field
					}else if(c=='\\'){
						state=FIELD_INESCAPE;
					}
					break;
				case FIELD_INESCAPE:
					// possibly validate legal escapes
					state=FIELD;
					break;
				case VALUE_SEPARATOR:
					if(c==':'){
						state=NONE;
					}else{
						// Throw error
					}
					break;
				case STRING:
					if(c=='"'){
						state=NONE;
						// JSONToken token=peek();
						// if(token.type!=JSONToken.VALUE){
							// error
						// }
						stackTop.endIndex=n+1;
					}else if(c=='\\'){
						state=STRING_INESCAPE;
					}
					break;
				case STRING_INESCAPE:
					// possibly validate legal escapes
					state=STRING;
					break;
			}
		}
	}
}