package me.doubledutch.stroom.query.sql;

/**
 *	Token objects are simple data structures used to hold the output from the
 *	Tokenizer and serves as the input for the Parser.
 */

public class Token{
	public static final int NONE=0;
	public static final int STRING=1;
	public static final int INTEGER=2;
	public static final int FLOAT=3;
	public static final int COMMENT=4;
	public static final int SYMBOL=5;
	public static final int IDENTIFIER=6;
	public static final int RESERVED_WORD=7;

	public int type;
	public String data;
	public int line;
	public int column;	

	public Token(int type,String data,int line,int column){
		this.type=type;
		this.data=data;
		this.line=line;
		this.column=column;
	}

	public Token(int type,String data){
		this.type=type;
		this.data=data;
		this.line=-1;
		this.column=-1;
	}

	public static Token stringT(String data){
		return new Token(STRING,data);
	}

	public static Token integerT(String data){
		return new Token(INTEGER,data);
	}

	public static Token floatT(String data){
		return new Token(FLOAT,data);
	}

	public static Token commentT(String data){
		return new Token(COMMENT,data);
	}

	public static Token symbolT(String data){
		return new Token(SYMBOL,data);
	}

	public static Token identifierT(String data){
		return new Token(IDENTIFIER,data);
	}

	public static Token wordT(String data){
		return new Token(RESERVED_WORD,data);
	}
}