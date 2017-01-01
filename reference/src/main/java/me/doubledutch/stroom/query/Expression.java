package me.doubledutch.stroom.query;

import me.doubledutch.lazyjson.*;
import me.doubledutch.stroom.*;
import org.json.*;

public class Expression{
	public final static int OR=1;
	public final static int AND=2;
	
	public final static int NOT=3;

	public final static int EQ=4;
	public final static int NEQ=5;
	public final static int GT=6;
	public final static int LT=7;
	public final static int GTE=8;
	public final static int LTE=9;

	public final static int REFERENCE=10;
	public final static int STRING=11;
	public final static int FLOAT=12;
	public final static int BOOLEAN=13;
	public final static int INTEGER=14;
	public final static int NULL=15;	

	private int type;
	private Expression left;
	private Expression right;

	private String valString;
	private double valDouble;
	private boolean valBoolean;
	private long valInteger;

	private String refString;

	public String toString(){
		if(type==OR){
			return "("+left.toString()+" OR "+right.toString()+")";
		}else if(type==AND){
			return "("+left.toString()+" AND "+right.toString()+")";
		}else if(type==NOT){
			return "(NOT "+left.toString()+")";
		}else if(type==EQ){
			return "("+left.toString()+" = "+right.toString()+")";
		}else if(type==NEQ){
			return "("+left.toString()+" != "+right.toString()+")";
		}else if(type==GT){
			return "("+left.toString()+" > "+right.toString()+")";
		}else if(type==LT){
			return "("+left.toString()+" < "+right.toString()+")";
		}else if(type==GTE){
			return "("+left.toString()+" >= "+right.toString()+")";
		}else if(type==LTE){
			return "("+left.toString()+" <= "+right.toString()+")";
		}else if(type==REFERENCE){
			return refString;
		}else if(type==STRING){
			return valString;
		}else if(type==FLOAT){
			return ""+valDouble;
		}else if(type==BOOLEAN){
			return ""+valBoolean;
		}else if(type==INTEGER){
			return ""+valInteger;
		}else if(type==NULL){
			return "NULL";
		}
		return null;
	}

	public boolean evaluateBoolean(LazyObject obj) throws LazyException{
		Expression e=evaluate(obj);
		if(e.isBoolean())return e.getBoolean();
		return false;
	}

	public Expression evaluate(LazyObject obj) throws LazyException{
		if(type==STRING || type==FLOAT || type==BOOLEAN || type==INTEGER || type==NULL){
			return this;
		}
		if(type==REFERENCE){
			return pickValue(obj,refString);
		}

		Expression v1=left.evaluate(obj);
		Expression v2=null;
		if(right!=null){
			v2=right.evaluate(obj);
		}

		if(type==EQ){
			return value(v1.equalTo(v2));
		}else if(type==LT){
			return value(v1.lessThan(v2));
		}else if(type==GT){
			return value(v1.greaterThan(v2));
		}else if(type==AND){
			if(v1.isBoolean() && v1.getBoolean() && v2.isBoolean() && v2.getBoolean()){
				return value(true);
			}else{
				return value(false);
			}
		}
		return null;
	}

	public boolean equalTo(Expression val){
		if(type!=val.type){
			// TODO: we should try to coalesce!
			return false;
		}
		if(type==STRING){
			return valString.equals(val.valString);
		}else if(type==FLOAT){
			return valDouble==val.valDouble;
		}else if(type==BOOLEAN){
			return valBoolean==val.valBoolean;
		}else if(type==INTEGER){
			return valInteger==val.valInteger;
		}
		return false;
	}

	public boolean lessThan(Expression val){
		if(type!=val.type){
			// TODO: we should try to coalesce!
			return false;
		}
		if(type==STRING){
			// TODO: we should maybe do a sort order?
		}else if(type==FLOAT){
			return valDouble<val.valDouble;
		}else if(type==BOOLEAN){
			// TODO: what's our semantics here?
		}else if(type==INTEGER){
			return valInteger<val.valInteger;
		}
		return false;
	}

	public boolean greaterThan(Expression val){
		if(type!=val.type){
			// TODO: we should try to coalesce!
			return false;
		}
		if(type==STRING){
			// TODO: we should maybe do a sort order?
		}else if(type==FLOAT){
			return valDouble>val.valDouble;
		}else if(type==BOOLEAN){
			// TODO: what's our semantics here?
		}else if(type==INTEGER){
			return valInteger>val.valInteger;
		}
		return false;
	}

	public Expression pickValue(LazyObject obj,String ref) throws LazyException{
		Object val=Utility.pickValue(obj,ref);
		if(val==null){
			return Expression.value();
		}else if(val instanceof String){
			return Expression.value((String)val);
		}else if(val instanceof Long){
			return Expression.value((Long)val);
		}
		return null;
	}

	public boolean getBoolean(){
		return valBoolean;
	}

	public boolean isBoolean(){
		return type==BOOLEAN;
	}

	public Expression(int type,Expression left,Expression right){
		this.type=type;
		this.left=left;
		this.right=right;
	}

	public Expression(int type,Expression left){
		this.type=type;
		this.left=left;
		this.right=null;
	}

	public Expression(int type){
		this.type=type;
		this.left=null;
		this.right=null;
	}

	public static Expression or(Expression left,Expression right){
		return new Expression(OR,left,right);
	}

	public static Expression and(Expression left,Expression right){
		return new Expression(AND,left,right);
	}

	public static Expression not(Expression left){
		return new Expression(NOT,left);
	}

	public static Expression eq(Expression left,Expression right){
		return new Expression(EQ,left,right);
	}

	public static Expression neq(Expression left,Expression right){
		return new Expression(NEQ,left,right);
	}

	public static Expression gt(Expression left,Expression right){
		return new Expression(GT,left,right);
	}

	public static Expression gte(Expression left,Expression right){
		return new Expression(GTE,left,right);
	}

	public static Expression lt(Expression left,Expression right){
		return new Expression(LT,left,right);
	}

	public static Expression lte(Expression left,Expression right){
		return new Expression(LTE,left,right);
	}

	public static Expression value(String str){
		Expression e=new Expression(STRING);
		e.valString=str;
		return e;
	}

	public static Expression value(double d){
		Expression e=new Expression(FLOAT);
		e.valDouble=d;
		return e;
	}

	public static Expression value(long l){
		Expression e=new Expression(INTEGER);
		e.valInteger=l;
		return e;
	}

	public static Expression value(boolean b){
		Expression e=new Expression(BOOLEAN);
		e.valBoolean=b;
		return e;
	}

	public static Expression value(){
		Expression e=new Expression(NULL);
		return e;
	}

	public static Expression reference(String str){
		Expression e=new Expression(REFERENCE);
		e.refString=str;
		return e;
	}

}

