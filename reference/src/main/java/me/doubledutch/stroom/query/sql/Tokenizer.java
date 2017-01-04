package me.doubledutch.stroom.query.sql;

import java.io.*;
import java.util.*;
/**
 *	The Tokenizer takes a text input and tokenizes it into the following
 *	possible tokens: String, Integer, Float, Comment, Identifier, Symbol
 *	or Reserved Word.
 *
 *	The Tokenizer works by maintaining a state of the current type of token
 *	being read while processing characters one at a time. It uses a one
 *	character buffer called stored to temporarily store characters when it has
 *	read to far, such as when a symbol is read while in the process of reading
 *	an integer token. In the case of "256+7" the integer 256 would be collected
 *	as a complete token, the + would be put in the buffer and the state would
 *	be reset to "none".
*/

public class Tokenizer{
	private final static char[] blanks={	' ','\n','\r','\t'};
	private final static char[] symbols={	'=','.',',','-','+','*','/','%','?','&','!','|','\'',';',
											'<','>','(',')','{','}','[',']'};
	private final static String[] reservedWords={
		"SELECT","INSERT","UPDATE","DELETE","FROM","WHERE","GROUP","BY","ORDER","SET","INTO","VALUES",
		"LIKE","IS","NOT","AND","OR","NULL","HAVING","AS","IN","CAST"
	};
	private Reader reader;
	private int state;
	private StringBuilder buffer=new StringBuilder();
	private ArrayList<Token> result=new ArrayList<Token>();
	private int line=1,column=1,sline=1,scolumn=1;
	private int stored=-1,input=-1;
	private boolean escaped=false;
	private char quoteChar='"';
	
	public Tokenizer(String str){
		this.reader=new StringReader(str);
	}

	public Tokenizer(Reader reader){
		this.reader=reader;
	}
	
	private boolean isBlank(char c){
		for(char b : blanks)if(c==b)return true;
		return false;
	}
	
	private boolean isDigit(char c) {
		return (c >= '0' && c <= '9'); 
	}
	
	private boolean isSymbol(char c){
		for(char s : symbols)if(c==s)return true;
		return false;
	}

	private boolean isReservedWord(String str){
		str=str.toUpperCase();
		for(String s:reservedWords){
			if(str.equals(s))return true;
		}
		return false;
	}

	private void collectTokenAndPush(){
		collectToken();
		stored=input;
	}

	private void collectToken(){
		String sdata=buffer.toString();
		if(state==Token.IDENTIFIER && isReservedWord(sdata)){
			state=Token.RESERVED_WORD;
			sdata=sdata.toUpperCase();
		}
		Token tkn=new Token(state,sdata,sline,scolumn);
		result.add(tkn);
		buffer=new StringBuilder();
		state=Token.NONE;
	}
	
	public ArrayList<Token> tokenize() throws ParseException{
		try{
			input=reader.read();
			while(input>-1){
				char c=(char)input;
				switch(state){
					case Token.NONE:
							// we are not currently in a token, start a new state
							if(!isBlank(c)){
								if(c=='"' || c=='\''){
									state=Token.STRING;
									quoteChar=c;
								}else if(isDigit(c)){
									buffer.append(c);
									state=Token.INTEGER;
								}else if(c=='#'){
									state=Token.COMMENT;
								}else if(isSymbol(c)){
									result.add(new Token(Token.SYMBOL,Character.toString(c),line,column));
								}else{	
									buffer.append(c);
									state=Token.IDENTIFIER;
								}
								sline=line;
								scolumn=column;
							}
						break;
					case Token.STRING:
						// We are currently reading a string literal, transform escaped characters and collect when " is reached
						if(escaped){
							switch(c){
								case 'n':buffer.append("\n");break;
								case 'r':buffer.append("\r");break;
								case 't':buffer.append("\t");break;
								case '\\':buffer.append('\\');break;
								case '"':buffer.append('"');break;
								case '\'':buffer.append('\'');break;
								default: error("Unknown escape character '\\"+c+"'");
							}
							escaped=false;
						}else{
							if(c==quoteChar){
								collectToken();
							}else if(c=='\\'){
								escaped=true;
							}else{
								buffer.append(c);
							}
						}
						break;
					case Token.INTEGER: case Token.FLOAT:
						if(isDigit(c)){
							buffer.append(c);
						}else if(isBlank(c)){
							collectToken();
						}else if(c=='.'||c=='e'||c=='E'){
							buffer.append(c);
							state=Token.FLOAT;
						}else if(c=='-'){
							char c2=buffer.charAt(buffer.length()-1);
							if(c2=='e' || c2=='E'){
								buffer.append(c);
								// next must be digit
								input=reader.read();
								column++;
								if(input==-1)error("Unclosed floating point literal.");
								c=(char)input;
								if(isDigit(c)){
									buffer.append(c);
								}else error("Unclosed floating point literal.");
							}else{
								collectTokenAndPush();
							}
						}else{
							collectTokenAndPush();
						}
						break;
					case Token.COMMENT:
						// Collect comments until the end of the line
						if(c=='\n'){
							collectToken();
						}else{
							buffer.append(c);
						}
						break;
					case Token.IDENTIFIER:
						if(isBlank(c)){
							collectToken();
						}else if(isSymbol(c)){
							collectTokenAndPush();
						}else{
							buffer.append(c);
						}
						break;
				}
				if(stored>-1){
					// If a character has been pushed into stored, make this the new input
					input=stored;
					stored=-1;
				}else{
					// If there was no character waiting in stored, read a new character
					if(input=='\n'){
						line++;
						column=1;
					}else{
						column++;
					}
					input=reader.read();
				}
			}
			if(state==Token.STRING){
				// String literals must be closed before the end of the file, everything else is valid
				error("String literal never closed");
			}else if(state!=Token.NONE){
				collectToken();
			}
		}catch(IOException e){
			e.printStackTrace();
			error("IO Error while reading source file");
		}finally{
			try{
				reader.close();
			}catch(Exception ee){}
		}
		return result;
	}

	private void error(String msg) throws ParseException{
		throw new ParseException("Line "+sline+", Column "+scolumn+" : "+msg);
	}
}