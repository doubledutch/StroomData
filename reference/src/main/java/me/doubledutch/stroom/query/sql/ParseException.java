package me.doubledutch.stroom.query.sql;

public class ParseException extends Exception{
	public final static long serialVersionUID=0;
	public int line=-1;
	public int column=-1;

	public ParseException(String str){
		super(str);
	}

	public ParseException(String str, int line, int column){
		super(str);
		this.line=line;
		this.column=column;
	}
	
	public ParseException(String str, Token tkn){
		super(str);
		if(tkn!=null){
			this.line=tkn.line;
			this.column=tkn.column;
		}
	}
}