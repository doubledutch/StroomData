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
	private final int VALUE_SEPARATOR=2;
	private final int VALUE=3;
	private final int STRING=4;

	private int state=NONE;
	private boolean inEscape=false;

	private JSONToken[] stack=new JSONToken[10];
	private int stackPointer=0;


	private void push(JSONToken token){
		// System.out.println(token.toString(stackPointer*2)+" push");
		if(size()>0){
			JSONToken parent=peek();
			parent.addChild(token);

		}else{
			root=token;
		}
		stack[stackPointer++]=token;
		// tokens.add(token);
	}

	// private void add(JSONToken token){
	// 	tokens.add(token);
	// }

	private JSONToken pop(){
		return stack[--stackPointer];
	}

	private JSONToken peek(){
		return stack[stackPointer-1];
	}

	private int size(){
		return stackPointer;
	}


	protected void tokenize() throws Exception{
		int length=source.length();
		for(int n=0;n<length;n++){
			char c=source.charAt(n);
			switch(state){
				case NONE:
					// buf=new StringBuilder();
					if(c==' ' || c=='\t' || c=='\n' || c=='\r'){
						// Do nothing
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
					}else if(c=='{'){
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
						JSONToken token=peek();
						if(token.type==JSONToken.ARRAY){
							state=STRING;
							push(JSONToken.cValue(n));
						}else if(token.type==JSONToken.FIELD){
							state=STRING;
							push(JSONToken.cValue(n));
						}else if(token.type==JSONToken.OBJECT){
							state=FIELD;
							push(JSONToken.cField(n));
						}else{
							// This shouldn't occur should it?
						}
					}else if(c==','){
						// This must be the end of a value and the start of another
						JSONToken token=pop();
						if(token.endIndex==-1){
							token.endIndex=n;
						}
						if(peek().type==JSONToken.FIELD){
							// This was the end of the value for a field, pop that too
							pop();
						}else{
							// System.out.println("Not a field");
						}
					}else{
						// This must be a new value
						JSONToken token=peek();
						if(token.type==JSONToken.VALUE){
							// We are just collecting more data for the current value
						}else{
							// System.out.println("Starting value with "+c);
							push(JSONToken.cValue(n));
						}
					}
					break;
				case FIELD:
					// buf.append(c);
					if(inEscape){
						inEscape=false;
					}else if(c=='\\'){
						inEscape=true;
					}else if(c=='"'){
						state=VALUE_SEPARATOR;
						JSONToken token=peek();
						token.endIndex=n+1;
						// Error check that its field
					}
					break;
				case VALUE_SEPARATOR:
					if(c==':'){
						state=NONE;
					}else{
						// Throw error
					}
					break;
				case STRING:
					if(inEscape){
						inEscape=false;
					}else if(c=='\\'){
						inEscape=true;
					}else if(c=='"'){
						state=NONE;
						JSONToken token=peek();
						// if(token.type!=JSONToken.VALUE){
							// error
						// }
						token.endIndex=n+1;
					}
					break;
			}
		}
	}
}