package me.doubledutch.stroom.query.sql;

import me.doubledutch.stroom.query.*;
import java.net.*;
import java.util.*;
import me.doubledutch.stroom.StreamConnection;

public class SQLRunner implements Runnable{
	private Query query;
	private QueryRunner runner;
	private List<String> tmpStreams=new ArrayList<String>();

	public SQLRunner(Query query,QueryRunner runner){
		this.query=query;
		this.runner=runner;
	}

	public void run(){
		// System.out.println("So much running");
		query.setState(Query.RUNNING);
		try{
			// Parse Query
			SQLParser parser=new SQLParser(query.getText());
			SQLQuery sql=parser.parseQuery();
			// Load and filter data
			for(TableReference tbl:sql.tableList){
				if(tbl.identifier!=null){
					StreamConnection input=runner.openStream(new URI("local://direct/stream/"+tbl.identifier));
					String outId=UUID.randomUUID().toString();
					StreamConnection output=runner.openStream(new URI("local://direct/stream/"+outId));
					tmpStreams.add(outId);

					TableScanner scanner=new TableScanner(input,0l,input.getCount()-1,output);
					runner.runTask(scanner);
				}else throw new ParseException("Only local streams are currently supported");
			}
			query.setState(Query.COMPLETED);
		}catch(ParseException pe){
			pe.printStackTrace();
			query.setError(pe.getMessage());
			query.setState(Query.ERROR);
		}catch(Exception e){
			e.printStackTrace();
			query.setError(e.getMessage());
			query.setState(Query.ERROR);
		}

	}
}