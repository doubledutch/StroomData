package me.doubledutch.stroom.query.sql;

import java.util.*;
import me.doubledutch.stroom.query.*;

/*
	<QUERY>						::= 'SELECT' <SELECT-LIST> <TABLE-EXPRESSION>
	<SELECT-LIST>				::= '*' | ( <DERIVED-COLUMN> ( ',' <DERIVED-COLUMN> ) * )
	<DERIVED-COLUMN>			::= <VALUE-EXPRESSION> ( 'AS' <IDENTIFIER> )?
	<TABLE-EXPRESSION>			::= <FROM-CLAUSE> ( <WHERE-CLAUSE> )? ( <GROUP-BY-CLAUSE> )? ( <HAVING-CLAUSE> )?
	
	<FROM-CLAUSE>				::= 'FROM' <TABLE-LIST>
	<TABLE-LIST>				::= <TABLE-REFERENCE> ( ',' <TABLE-REFERENCE> )*
	<TABLE-REFERENCE> 			::= ( <STRING-LITERAL> | <IDENTIFIER> | ( '(' <QUERY> ')' ) ) ( 'AS' <IDENTIFIER> )?

	<WHERE-CLAUSE>				::= 'WHERE' <BOOLEAN-VALUE-EXPRESSION>
	<BOOLEAN-VALUE-EXPRESSION> 	::= <BOOLEAN-TERM> | ( <BOOLEAN-VALUE-EXPRESSION> 'OR' <BOOLEAN-TERM> )
	<BOOLEAN-TERM>				::= <BOOLEAN-FACTOR> | ( <BOOLEAN-TERM> 'AND' <BOOLEAN-FACTOR> )
	<BOOLEAN-FACTOR>			::= ( 'NOT' )? <BOOLEAN-TEST>
	<BOOLEAN-TEST>				::= <PREDICATE> | ( '(' <PREDICATE> ')' )
	<PREDICATE>					::= <COMPARISON-PREDICATE> | <IN-PREDICATE> | <LIKE-PREDICATE> | <NULL-PREDICATE> |
									<EXISTS-PREDICATE>

	<COMPARISON-PREDICATE> 		::= <VALUE-EXPRESSION> <COMPARISON-OPERATOR> <VALUE-EXPRESSION>
	<COMPARISON-OPERATOR>		::= '=' | '!=' | '<' | '<=' | '>' | '>='

	<VALUE-EXPRESSION>			::= <NUMERIC-VALUE-EXPRESSION> |
									<STRING-VALUE-EXPRESSION> |
									<DATETIME-VALUE-EXPRESSION> |
									<BOOLEAN-VALUE-EXPRESSION> |
									<COLUMN-REFERENCE>

	<COLUMN-REFERENCE>			::= <IDENTIFIER> ( '.' <IDENTIFIER> )*
*/

public class SQLParser{
	ArrayList<Token> tokens;

	public SQLParser(String str) throws ParseException{
		Tokenizer t=new Tokenizer(str);
		tokens=t.tokenize();
	}

	/**
	 * Returns the next available token or null if no more tokens are available.
	 */
	public Token getToken(){
		if(tokens.size()>0){
			Token t=tokens.remove(0);
			if(t!=null && t.type==Token.COMMENT)return getToken();
			return t;
		}
		return null;
	}
	
	/**
	 * Returns an unused token to the buffer of tokens.	
	 */
	public void returnToken(Token t){
		tokens.add(0,t);
	}

	/**
	 * Returns the next available token or throws an exception with the given
	 * message if no more tokens are available.
	 */
	private Token requireToken(String err) throws ParseException{
		Token t=getToken();
		if(t==null)throw new ParseException(err);
		return t;
	}

	/**
	 * Returns the next available token if it is a symbol and equal to the given string.
	 * If there are no more tokens, or the next token is not a symbol an exception will
	 * be thrown with the given message.
	*/
	private Token requireSymbol(String str,String err) throws ParseException{
		Token t=getToken();
		if(t==null)throw new ParseException(err);
		if(t.type==Token.SYMBOL && t.data.equals(str)){
			return t;
		}
		throw new ParseException(err,t);
	}

	/**
	 * Returns the next available token if it is a symbol.
	 * If there are no more tokens, or the next token is not a symbol an exception will
	 * be thrown with the given message.
	*/
	private Token requireSymbol(String err) throws ParseException{
		Token t=getToken();
		if(t==null)throw new ParseException(err);
		if(t.type==Token.SYMBOL){
			return t;
		}
		throw new ParseException(err,t);
	}

	/**
	 * Returns the next available token if it is an identifier. If there are no more
	 * tokens, or the next token is not an identifier an exception will be thrown with
	 * the given message.
	*/
	private Token requireIdentifier(String err) throws ParseException{
		Token t=getToken();
		if(t==null)throw new ParseException(err);
		if(t.type==Token.IDENTIFIER){
			return t;
		}
		throw new ParseException(err,t);
	}

	/**
	 * If the next available token is a symbol that matches the given string, it
	 * is consumed and true is returned. If not, the token is returned to the
	 * buffer and false is returned. If no more tokens are available, false is
	 * also returned.
	*/
	private boolean consumeSymbol(String str){
		Token t=getToken();
		if(t==null)return false;
		if(t.type==Token.SYMBOL && t.data.equals(str)){
			return true;
		}
		returnToken(t);
		return false;
	}

	/**
	 * If the next available token is an identifier that matches the given string, it
	 * is consumed and true is returned. If not, the token is returned to the
	 * buffer and false is returned. If no more tokens are available, false is
	 * also returned.
	*/
	private boolean consumeIdentifier(String str){
		Token t=getToken();
		if(t==null)return false;
		if(t.type==Token.IDENTIFIER && t.data.equals(str)){
			return true;
		}
		returnToken(t);
		return false;
	}

	/**
	 * If the next available token is a reserved word that matches the given string, it
	 * is consumed and true is returned. If not, the token is returned to the
	 * buffer and false is returned. If no more tokens are available, false is
	 * also returned. Reserved words are always case insensitive.
	*/
	private boolean consumeReservedWord(String str){
		Token t=getToken();
		if(t==null)return false;
		if(t.type==Token.RESERVED_WORD && t.data.equals(str.toUpperCase())){
			return true;
		}
		returnToken(t);
		return false;
	}

	/**
	 * If the next available token is an identifier that matches the given string, it
	 * is consumed and true is returned. If not, the token is returned to the
	 * buffer and false is returned. If no more tokens are available, false is
	 * also returned.
	 * This version of consumeIdentifer is case insensitive.
	*/
	private boolean consumeIdentifierI(String str){
		Token t=getToken();
		if(t==null)return false;
		if(t.type==Token.IDENTIFIER && t.data.toLowerCase().equals(str.toLowerCase())){
			return true;
		}
		returnToken(t);
		return false;
	}

	/*************************************************************************/

	// <QUERY> ::= 'SELECT' <SELECT-LIST> <TABLE-EXPRESSION>
	public SQLQuery parseQuery() throws ParseException{
		if(consumeReservedWord("SELECT")){
			SQLQuery query=new SQLQuery();
			requireSelectList(query);
			requireTableExpression(query);
			parseWhereClause(query);
			query.normalize();
			return query;
		}
		throw new ParseException("Query must start with SELECT",1,1);
	}

	// <SELECT-LIST> ::= '*' | ( <DERIVED-COLUMN> ( ',' <DERIVED-COLUMN> ) * )
	private void requireSelectList(SQLQuery query) throws ParseException{
		if(consumeSymbol("*")){
			query.selectAll=true;
		}else{
			query.selectAll=false;
			List<DerivedColumn> list=new ArrayList<DerivedColumn>();
			list.add(requireDerivedColumn());
			while(consumeSymbol(",")){
				list.add(requireDerivedColumn());
			}
			query.selectList=list;
		}
	}

	// <TABLE-EXPRESSION> ::= <FROM-CLAUSE> ( <WHERE-CLAUSE> )? ( <GROUP-BY-CLAUSE> )? ( <HAVING-CLAUSE> )?
	private void requireTableExpression(SQLQuery query) throws ParseException{
		requireFromClause(query);
		parseWhereClause(query);
	}

	// <WHERE-CLAUSE> ::= 'WHERE' <BOOLEAN-VALUE-EXPRESSION>
	private void parseWhereClause(SQLQuery query) throws ParseException{
		if(consumeReservedWord("WHERE")){
			query.where=requireBooleanValueExpression();
		}
	}
	
	// <BOOLEAN-VALUE-EXPRESSION> ::= <BOOLEAN-TERM> | ( <BOOLEAN-VALUE-EXPRESSION> 'OR' <BOOLEAN-TERM> )
	private Expression requireBooleanValueExpression() throws ParseException{
		// TODO: change this to get the boolean AST right
		Expression e1= requireComparisonPredicate();
		if(consumeReservedWord("OR")){
			Expression e2=requireBooleanValueExpression();
			return Expression.or(e1,e2);
		}else if(consumeReservedWord("AND")){
			Expression e2=requireBooleanValueExpression();
			return Expression.and(e1,e2);
		}
		return e1;
	}


	// <COMPARISON-PREDICATE> 		::= <VALUE-EXPRESSION> <COMPARISON-OPERATOR> <VALUE-EXPRESSION>
	// <COMPARISON-OPERATOR>		::= '=' | '!=' | '<' | '<=' | '>' | '>='
	private Expression requireComparisonPredicate() throws ParseException{
		// Token t=getToken();
		// System.out.println(t.data);
		// System.out.println("looking for v1");
		Expression v1=requireValueExpression();
		// System.out.println("looking for operator");
		int operator=-1;
		Token t1=requireSymbol("Comparison operator expected");
		if(t1.data.equals("=")){
			operator=Expression.EQ;
		}else if(t1.data.equals("<")){
			Token t2=requireToken("Right hand part of comparison expected");
			if(t2.type==Token.SYMBOL && t2.data.equals("=")){
				operator=Expression.LTE;
			}else{
				returnToken(t2);
				operator=Expression.LT;
			}
		}else if(t1.data.equals(">")){
			Token t2=requireToken("Right hand part of comparison expected");
			if(t2.type==Token.SYMBOL && t2.data.equals("=")){
				operator=Expression.GTE;
			}else{
				returnToken(t2);
				operator=Expression.GT;
			}
		}else if(t1.data.equals("!")){
			Token t2=requireToken("Right hand part of comparison expected");
			if(t2.type==Token.SYMBOL && t2.data.equals("!")){
				operator=Expression.NEQ;
			}else{
				throw new ParseException("! operator may only be used for != comparisons");
			}
		}
		Expression v2=requireValueExpression();
		return new Expression(operator,v1,v2);
	}


	// <BOOLEAN-VALUE-EXPRESSION> 	::= <BOOLEAN-TERM> | ( <BOOLEAN-VALUE-EXPRESSION> 'OR' <BOOLEAN-TERM> )
	// <BOOLEAN-TERM>				::= <BOOLEAN-FACTOR> | ( <BOOLEAN-TERM> 'AND' <BOOLEAN-FACTOR> )
	// <BOOLEAN-FACTOR>			::= ( 'NOT' )? <BOOLEAN-TEST>
	// <BOOLEAN-TEST>				::= <PREDICATE> | ( '(' <PREDICATE> ')' )
	// <PREDICATE>					::= <COMPARISON-PREDICATE> | <IN-PREDICATE> | <LIKE-PREDICATE> | <NULL-PREDICATE> |
	//								<EXISTS-PREDICATE>


	// <FROM-CLAUSE> ::= 'FROM' <TABLE-LIST>
	private void requireFromClause(SQLQuery query) throws ParseException{
		if(consumeReservedWord("FROM")){
			requireTableList(query);
		}else{
			throw new ParseException("FROM expected",getToken());
		}
	}

	// <TABLE-LIST>	::= <TABLE-REFERENCE> ( ',' <TABLE-REFERENCE> )*
	private void requireTableList(SQLQuery query) throws ParseException{
		List<TableReference> list=new ArrayList<TableReference>();
		list.add(requireTableReference());
		while(consumeSymbol(",")){
			list.add(requireTableReference());
		}
		query.tableList=list;
	}

	// <TABLE-REFERENCE> ::= ( <STRING-LITERAL> | <IDENTIFIER> | ( '(' <QUERY> ')' ) ) ( 'AS' <IDENTIFIER> )?
	private TableReference requireTableReference() throws ParseException{
		TableReference ref=new TableReference();
		if(consumeSymbol("(")){
			SQLQuery q=parseQuery();
			if(q==null)throw new ParseException("SELECT query expected",getToken());
			if(!consumeSymbol(")"))throw new ParseException(") expected to end sub query",getToken());
			ref.query=q;
		}else{
			Token t=requireToken("Table reference expected");
			if(t.type==Token.IDENTIFIER){
				ref.identifier=t.data;
			}else if(t.type==Token.STRING){
				ref.url=t.data;
			}else throw new ParseException("Table reference expected");
		}
		if(consumeReservedWord("AS")){
			ref.as=requireIdentifier("A new identifier for a table must follow the keyword AS").data;
		}
		return ref;
	}


	// <DERIVED-COLUMN>	::= <COLUMN-REFERENCE> ( 'AS' <IDENTIFIER> )?
	private DerivedColumn requireDerivedColumn() throws ParseException{
		DerivedColumn col=parseColumnReference();
		if(col==null){
			throw new ParseException("Column reference expected");
		}
		if(consumeReservedWord("AS")){
			Token t=requireIdentifier("A new identifier for a column must follow the keyword AS");
			col.as=t.data;
		}
		return col;
	}

	// 	<VALUE-EXPRESSION> ::= <NUMERIC-VALUE-EXPRESSION> | <STRING-VALUE-EXPRESSION> | <DATETIME-VALUE-EXPRESSION> | <BOOLEAN-VALUE-EXPRESSION> | <COLUMN-REFERENCE>
	private Expression requireValueExpression() throws ParseException{
		Expression val=parseNumericValueExpression();
		if(val!=null)return val;

		val=parseStringValueExpression();
		if(val!=null)return val;

		DerivedColumn col=parseColumnReference();
		if(col!=null){
			return Expression.reference(col.toString());
		}
		// if(col!=null)return col;
		throw new ParseException("Value expression expected",getToken());
	}

	private Expression parseStringValueExpression(){
		Token t=getToken();
		if(t!=null){
			// System.out.println("what is "+t.data);
			if(t.type==Token.STRING){
				return Expression.value(t.data);
			}
		}
		returnToken(t);
		return null;
	}

	private Expression parseNumericValueExpression(){
		Token t=getToken();
		if(t!=null){
			if(t.type==Token.INTEGER){
				return Expression.value(Integer.parseInt(t.data));
			}
			if(t.type==Token.FLOAT){
				return Expression.value(Double.parseDouble(t.data));
			}
		}
		returnToken(t);
		return null;
	}

	// <COLUMN-REFERENCE> ::= <IDENTIFIER> ( '.' <IDENTIFIER> )*
	private DerivedColumn parseColumnReference() throws ParseException{
		Token t=getToken();
		// System.out.println("We should be finding 'type' "+t.data);
		if(t.type==Token.IDENTIFIER){
			List<String> ref=new ArrayList<String>();
			ref.add(t.data);
			while(consumeSymbol(".")){
				t=getToken();
				if(t.type!=Token.IDENTIFIER){
					throw new ParseException("Unexpected data found after .",t);
				}
				ref.add(t.data);
			}
			return DerivedColumn.createReference(ref);
		}else{
			returnToken(t);
		}
		return null;
	}
}