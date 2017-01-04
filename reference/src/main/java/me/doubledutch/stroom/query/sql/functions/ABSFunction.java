package me.doubledutch.stroom.query.sql.functions;

import me.doubledutch.stroom.query.Expression;
import java.util.*;

public class ABSFunction extends SQLFunction{
	public Expression run(List<Expression> arguments) throws Exception{
		if(arguments.size()!=1)throw new Exception("abs(v) takes one numeric argument");
		Expression v=arguments.get(0);
		if(v.getType()==Expression.INTEGER){
			if(v.valInteger<0)return Expression.value(-v.valInteger);
			return v;
		}
		if(v.getType()==Expression.FLOAT){
			if(v.valDouble<0)return Expression.value(-v.valDouble);
			return v;
		}
		if(v.getType()==Expression.STRING){
			try{
				int i=Integer.parseInt(v.valString);
				if(i<0)return Expression.value(-i);
				return Expression.value(i);
			}catch(Exception e){}
			try{
				double d=Double.parseDouble(v.valString);
				if(d<0)return Expression.value(-d);
				return Expression.value(d);
			}catch(Exception e){}
		}
		return null;
	}
}