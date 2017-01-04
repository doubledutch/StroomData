package me.doubledutch.stroom.query;

import me.doubledutch.lazyjson.*;
import me.doubledutch.stroom.*;
import org.json.*;
import java.util.List;

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

	public final static int FUNCTION=16;
	public final static int ADD=17;
	public final static int SUB=18;
	public final static int DIV=19;
	public final static int MUL=20;
	public final static int MOD=21;

	public final static int SIGN_POS=22;
	public final static int SIGN_NEG=23;

	public final static int CAST=24;

	private int type;
	private Expression left;
	private Expression right;

	private List<Expression> arguments=null;

	private String valString;
	private double valDouble;
	private boolean valBoolean;
	private long valInteger;

	// Changed derived column to fix this shit
	public List<String> ref;

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
			StringBuilder buf=new StringBuilder();
			for(int i=0;i<ref.size();i++){
				if(i>0)buf.append(".");
				buf.append(ref.get(i));
			}
			return buf.toString();
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
		}else if(type==FUNCTION){
			StringBuilder buf=new StringBuilder();
			buf.append(valString);
			buf.append("(");
			for(int i=0;i<arguments.size();i++){
				if(i>0)buf.append(",");
				buf.append(arguments.get(i).toString());
			}
			buf.append(")");
			return buf.toString();
		}else if(type==ADD){
			return "("+left.toString()+" + "+right.toString()+")";
		}else if(type==SUB){
			return "("+left.toString()+" - "+right.toString()+")";
		}else if(type==MUL){
			return "("+left.toString()+" * "+right.toString()+")";
		}else if(type==DIV){
			return "("+left.toString()+" / "+right.toString()+")";
		}else if(type==MOD){
			return "("+left.toString()+" % "+right.toString()+")";
		}else if(type==CAST){
			return "CAST ( "+left.toString()+" AS "+valString+" )";
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
			// System.out.println("Trying to evaluate a reference");
			return pickValue(obj,ref);
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
		}else if(type==ADD){
			if(v1.type==FLOAT && v2.type==FLOAT){
				return value(v1.valDouble+v2.valDouble);
			}else if(v1.type==INTEGER && v2.type==INTEGER){
				return value(v1.valInteger+v2.valInteger);
			}else{
				return value(v1.getDoubleValue()+v2.getDoubleValue());
			}
		}else if(type==SUB){
			if(v1.type==FLOAT && v2.type==FLOAT){
				return value(v1.valDouble-v2.valDouble);
			}else if(v1.type==INTEGER && v2.type==INTEGER){
				return value(v1.valInteger-v2.valInteger);
			}else{
				return value(v1.getDoubleValue()-v2.getDoubleValue());
			}
		}else if(type==MUL){
			if(v1.type==FLOAT && v2.type==FLOAT){
				return value(v1.valDouble*v2.valDouble);
			}else if(v1.type==INTEGER && v2.type==INTEGER){
				return value(v1.valInteger*v2.valInteger);
			}else{
				return value(v1.getDoubleValue()*v2.getDoubleValue());
			}
		}else if(type==DIV){
			if(v1.type==FLOAT && v2.type==FLOAT){
				return value(v1.valDouble/v2.valDouble);
			}else if(v1.type==INTEGER && v2.type==INTEGER){
				return value(v1.valInteger/v2.valInteger);
			}else{
				return value(v1.getDoubleValue()/v2.getDoubleValue());
			}
		}else if(type==MOD){
			if(v1.type==FLOAT && v2.type==FLOAT){
				return value(v1.valDouble % v2.valDouble);
			}else if(v1.type==INTEGER && v2.type==INTEGER){
				return value(v1.valInteger % v2.valInteger);
			}else{
				return value(v1.getDoubleValue() % v2.getDoubleValue());
			}
		}else if(type==CAST){
			if(valString.equals("string")){
				if(v1.type==INTEGER)return value(""+v1.valInteger);
				if(v1.type==FLOAT)return value(""+v1.valDouble);
				if(v1.type==STRING)return v1;
				if(v1.type==BOOLEAN)return value(""+v1.valBoolean);
			}else if(valString.equals("int")){
				if(v1.type==INTEGER)return v1;
				if(v1.type==FLOAT)return value((int)v1.valDouble);
				if(v1.type==STRING){
					try{
						return value(Integer.parseInt(v1.valString));
					}catch(Exception e){}
				}
			}else if(valString.equals("float")){
				if(v1.type==INTEGER)return value((double)v1.valInteger);
				if(v1.type==FLOAT)return v1;
				if(v1.type==STRING){
					try{
						return value(Double.parseDouble(v1.valString));
					}catch(Exception e){}
				}
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

	public Expression pickValue(LazyObject obj,List<String> ref) throws LazyException{
		Object val=Utility.pickValue(obj,ref);
		if(val==null){
			return Expression.value();
		}else if(val instanceof String){
			return Expression.value((String)val);
		}else if(val instanceof Long || val instanceof Integer){
			return Expression.value((Long)val);
		}else if(val instanceof Double || val instanceof Float){
			return Expression.value((Double)val);
		}else if(val instanceof Boolean){
			return Expression.value((Boolean)val);
		}
		return null;
	}

	public boolean getBoolean(){
		return valBoolean;
	}

	public boolean isBoolean(){
		return type==BOOLEAN;
	}

	public double getDoubleValue(){
		if(type==FLOAT)return valDouble;
		if(type==INTEGER)return (double)valInteger;
		if(type==STRING){
			try{
				return Double.parseDouble(valString);
			}catch(Exception e){}
		}
		// TODO: is this really what we want to do?
		return 0;
	}

	public Object getValue(){
		if(type==FLOAT)return (Double)valDouble;
		if(type==INTEGER)return (Long)valInteger;
		if(type==STRING)return valString;
		if(type==BOOLEAN)return (Boolean)valBoolean;
		return null;
	}

	public int getType(){
		return type;
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

	public static Expression operator(Expression left,String symbol,Expression right){
		if(symbol.equals("+"))return new Expression(ADD,left,right);
		if(symbol.equals("-"))return new Expression(SUB,left,right);
		if(symbol.equals("/"))return new Expression(DIV,left,right);
		if(symbol.equals("*"))return new Expression(MUL,left,right);
		if(symbol.equals("%"))return new Expression(MOD,left,right);
		return null;
	}

	public static Expression operator(String symbol,Expression right){
		if(symbol.equals("+"))return new Expression(SIGN_POS,null,right);
		if(symbol.equals("-"))return new Expression(SIGN_NEG,null,right);
		return null;
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

	public static Expression reference(List<String> list){
		Expression e=new Expression(REFERENCE);
		e.ref=list;
		return e;
	}

	public static Expression function(String name, List<Expression> args){
		Expression e=new Expression(FUNCTION);
		e.arguments=args;
		e.valString=name;
		return e;
	}

	public static Expression cast(Expression expr,String type){
		Expression e=new Expression(CAST);
		e.valString=type;
		e.left=expr;
		return e;
	}

}

