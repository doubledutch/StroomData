package me.doubledutch.stroom.query.sql;

import me.doubledutch.stroom.query.*;
import java.net.*;
import java.util.*;
import me.doubledutch.stroom.client.StreamConnection;
import java.util.concurrent.atomic.*;

public class SQLRunner{
/*	private Query query;
	private QueryRunner runner;
	private List<String> tmpStreams=new ArrayList<String>();
	private SQLRunner app;
	private int counter=0;
	private long scanComplete=0;
	private QueryMetrics metrics;

	public SQLRunner(Query query,QueryRunner runner){
		this.query=query;
		this.runner=runner;
		app=this;
	}

	private void scanTable(StreamConnection input,long start,long end,StreamConnection output,Expression exp) throws Exception{
		TableScanner scanner=new TableScanner(input,start,end,output,exp);
		synchronized(app){
			counter++;
		}
		scanner.setCompletedListener(new QueryListener(){
			public void update(QueryMetrics m){
				try{
					synchronized(app){
						counter--;
						app.notify();
						metrics.children.add(m);
						scanComplete+=m.get("read.count");
						query.setScanCurrent(scanComplete);
					}
					// System.out.println(m.toJSON().toString(4));
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});
		runner.runTask(scanner);
	}

	public void run(){
		// System.out.println("So much running");
		query.startTimer();
		query.setState(Query.RUNNING);
		try{
			metrics=new QueryMetrics("sqlrunner");
			// Parse Query
			metrics.startTimer("sql.parse");
			SQLParser parser=new SQLParser(query.getText());
			SQLQuery sql=parser.parseQuery();
			metrics.stopTimer("sql.parse");
			// Load and filter data
			
			metrics.startTimer("query.run");
			synchronized(app){
				for(TableReference tbl:sql.tableList){
					long count=0;
					if(tbl.identifier!=null){
						StreamConnection input=runner.openStream(new URI("local://direct/stream/"+tbl.identifier));
						String outId=UUID.randomUUID().toString();
						StreamConnection output=runner.openStream(new URI("local://direct/stream/"+outId));
						tmpStreams.add(outId);

						long docNum=input.getCount();
						count+=docNum;
						query.setScanTotal(count);
						long scans=docNum/100000;
						for(int i=0;i<scans;i++){
							scanTable(input,i*100000l,(i+1)*100000l-1,output,sql.where);
						}
						scanTable(input,scans*100000l,docNum-1,output,sql.where);
						
					}else throw new ParseException("Only local streams are currently supported");
				}

				while(counter>0){
					try{
						app.wait();
					}catch(Exception ee){}
				}
				query.stopTimer();
			}
			metrics.stopTimer("query.run");
			for(String str:tmpStreams){
				StreamConnection con=runner.openStream(new URI("local://direct/stream/"+str));
				// con.truncate(0l);
			}
			System.out.println(metrics.toJSON().toString(4));
			long count=0;
			for(QueryMetrics m:metrics.children){
				count+=m.get("read.count");
			}
			metrics.set("read.count",count);
			long time=metrics.get("query.run");
			long rate=(int)(count/(time/1000000000.0));
			metrics.set("query.rate",rate);
			System.out.println("rate "+rate+" documents/s");
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

	}*/
}