package me.doubledutch.stroom.query;

import me.doubledutch.stroom.query.sql.*;
import me.doubledutch.lazyjson.*;

public class QueryTool{
	public QueryTool(String sql){
		try{
			SQLParser parser=new SQLParser(sql);
			SQLQuery query=parser.parseQuery();
			System.out.println("Parse query:");
			System.out.println(query.toString());
			System.out.println("Executing query");
			QueryRunner runner=new QueryRunner(query);
			runner.run();
			System.out.println("*********** RESULT SET");
			TempTable table=runner.getResult();
			table.reset();
			int count=0;
			while(table.hasNext()){
				LazyObject next=table.next();
				System.out.println(next.toString());
				count++;
			}
			System.out.println("   "+count+" rows returned in "+(runner.getTime()/1000.0)+" s");
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public static void main(String[] args){
		new QueryTool(args[0]);
	}
}