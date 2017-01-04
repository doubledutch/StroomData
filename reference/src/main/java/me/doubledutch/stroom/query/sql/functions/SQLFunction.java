package me.doubledutch.stroom.query.sql.functions;

import me.doubledutch.stroom.query.Expression;
import java.util.*;

public abstract class SQLFunction{
	private static Map<String,SQLFunction> functionMap=new HashMap<String,SQLFunction>();
	static{
		functionMap.put("abs",new ABSFunction());
	}


	public abstract Expression run(List<Expression> arguments) throws Exception;

	public static SQLFunction get(String name){
		name=name.toLowerCase();
		if(functionMap.containsKey(name)){
			return functionMap.get(name);
		}
		return null;
	}
}