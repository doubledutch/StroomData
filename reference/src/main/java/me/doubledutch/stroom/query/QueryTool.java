package me.doubledutch.stroom.query;

import me.doubledutch.stroom.query.sql.*;
import me.doubledutch.lazyjson.*;
import org.apache.commons.cli.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class QueryTool{
	private static boolean verbose=false;
	private static String queryText=null;
	private static Options options=null;

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

	private static void error(String msg){
		System.out.println("ERR: "+msg);
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp( "querytool", options);
	}

	private static void log(String msg){
		if(verbose)System.out.println("# "+msg);
	}

	private static String readFile(String filename) throws Exception{
		return new String(Files.readAllBytes(Paths.get(filename)),java.nio.charset.StandardCharsets.UTF_8);
	}

	public static void main(String[] args){
		try{
			// Setup command line options
			options = new Options();
			options.addOption("v","verbose", false, "print out verbose runtime information");
			options.addOption("q","query", true, "read query from a file");
			options.addOption("o","output", true, "write output to a file instead of stdout");
			CommandLineParser parser = new DefaultParser();
			CommandLine cmd = parser.parse( options, args);

			// Validate and extract arguments
			if(cmd.hasOption("v"))verbose=true;
			args=cmd.getArgs();
			if(cmd.hasOption("q") && args.length>0){
				error("You can not specify a query both in a file and as an argument");
			}else if(cmd.hasOption("q")){
				queryText=readFile(cmd.getOptionValue("q"));
			}else if(args.length==1){
				queryText=args[0];
			}else if(args.length>1){
				error("You can not specify more than one query");
			}else{
				error("You must specify a query");
			}

			// Parse the query
			log("Parsing query");
			try{
				long pre=System.nanoTime();
				SQLParser sqlparser=new SQLParser(queryText);
				SQLQuery query=sqlparser.parseQuery();
				long post=System.nanoTime();
				log(((post-pre)/100000)/10.0+" ms");
				log(query.toString());
				log("Executing query");
				pre=System.nanoTime();
				QueryRunner runner=new QueryRunner(query);
				runner.run();
				post=System.nanoTime();
				log(((post-pre)/100000)/10.0+" ms");
				TempTable table=runner.getResult();
				table.reset();
				int count=0;
				while(table.hasNext()){
					LazyObject next=table.next();
					
					System.out.println(next.toString());
					count++;
				}
			}catch(me.doubledutch.stroom.query.sql.ParseException pe){

			}
			// new QueryTool(args[0]);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}