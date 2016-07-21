package me.doubledutch.lazy;

public class LazyArray{
	private LazyToken root;
	// private String source;
	private char[] cbuf;

	private int length=-1;
	private LazyToken selectToken=null;
	private int selectInt=-1;

	public LazyArray(String raw) throws LazyException{
		LazyParser parser=new LazyParser(raw);
		parser.tokenize();	
		if(parser.root.type!=LazyToken.ARRAY){
			throw new LazyException("JSON Array must start with [",0);
		}
		root=parser.root;
		cbuf=parser.cbuf;
	}

	protected LazyArray(LazyToken root,char[] source){
		this.root=root;
		this.cbuf=source;
	}

	public int length(){
		if(root.child==null){
			return 0;
		}
		if(length>-1){
			return length;
		}
		length=root.getChildCount();
		return length;
	}

	public LazyArray getJSONArray(int index) throws LazyException{
		LazyToken token=getValueToken(index);
		if(token.type!=LazyToken.ARRAY)throw new LazyException("Requested value is not an array",token);
		return new LazyArray(token,cbuf);
	}

	public LazyObject getJSONObject(int index) throws LazyException{
		LazyToken token=getValueToken(index);
		if(token.type!=LazyToken.OBJECT)throw new LazyException("Requested value is not an object",token);
		return new LazyObject(token,cbuf);
	}

	public boolean getBoolean(int index){
		LazyToken token=getValueToken(index);
		if(token.type==LazyToken.VALUE_TRUE)return true;
		if(token.type==LazyToken.VALUE_FALSE)return false;
		throw new LazyException("Requested value is not a boolean",token);
	}

	public String getString(int index) throws LazyException{
		LazyToken token=getValueToken(index);
		return token.getStringValue(cbuf);
	}

	public int getInt(int index) throws LazyException{
		LazyToken token=getValueToken(index);
		return token.getIntValue(cbuf);
	}

	public long getLong(int index) throws LazyException{
		LazyToken token=getValueToken(index);
		return token.getLongValue(cbuf);
	}

	public double getDouble(int index) throws LazyException{
		LazyToken token=getValueToken(index);
		return token.getDoubleValue(cbuf);
	}

	public boolean isNull(int index) throws LazyException{
		LazyToken token=getValueToken(index);
		if(token.type==LazyToken.VALUE_NULL)return true;
		return false;
	}

	private LazyToken getValueToken(int index) throws LazyException{
		if(index<0)throw new LazyException("Array undex can not be negative");
		int num=0;
		LazyToken child=root.child;
		if(selectInt>-1 && index>=selectInt){
			num=selectInt;
			child=selectToken;
		}
		while(child!=null){
			if(num==index){
				selectInt=index;
				selectToken=child;
				return child;
			}
			num++;
			child=child.next;
		}
		throw new LazyException("Array index out of bounds "+index);
	}

	private String getString(LazyToken token){
		return token.getStringValue(cbuf);
	}

	public String toString(int pad){
		return root.toString(pad);
	}
}