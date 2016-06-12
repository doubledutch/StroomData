package me.doubledutch.stroom;

import org.apache.log4j.Logger;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.io.*;
import org.json.*;
import java.util.*;

import me.doubledutch.stroom.streams.StreamHandler;
import me.doubledutch.stroom.servlet.*;
import me.doubledutch.stroom.filters.*;
import me.doubledutch.stroom.aggregates.*;

public class MultiHostServer implements Runnable{
	private final Logger log = Logger.getLogger("MultiHost");
	private static JSONObject config=null;

	private Server server=null;

	private StreamHandler streamHandler=null;
	private AggregateManager aggregateManager=null;
	private KeyValueManager kvManager=null;
	private ServiceManager serviceManager=null;
	private ScriptManager scriptManager=null;
	private QueryManager queryManager=null;

	private static String BUILD_DATE;
	private static String BUILD_VERSION;
	private static String BUILD_NUMBER;

	// private List<Service> serviceList=new ArrayList<Service>();

	public static JSONObject getConfig(){
		return config;
	}

	public MultiHostServer(String configLocation){
		try{
			log.info("Starting Stroom MultiHost");
			Properties props = new Properties();
			props.load(SystemAPIServlet.class.getResourceAsStream("/version.properties"));
			BUILD_DATE=props.getProperty("BUILD_DATE");
			BUILD_VERSION=props.getProperty("BUILD_VERSION");
			BUILD_NUMBER=props.getProperty("BUILD_NUMBER");

			log.info("v"+BUILD_VERSION+" b"+BUILD_NUMBER+" (built on "+BUILD_DATE+")");

			// Load configuration
			if(configLocation!=null){
				config=new JSONObject(Utility.readFile(configLocation));
			}else{
				config=new JSONObject();
			}
			patchConfiguration(config);
			// Create the services
			log.info("Creating services");
			streamHandler=new StreamHandler(config.getJSONObject("streams"));

			aggregateManager=new AggregateManager();
			kvManager=new KeyValueManager();
			serviceManager=new ServiceManager(streamHandler,aggregateManager,kvManager);
			scriptManager=new ScriptManager(streamHandler);
			queryManager=new QueryManager(streamHandler);
			
			// Start servlets
			log.info("Starting servlets");
			StreamAPIServlet.setStreamHandler(streamHandler);
			startServlets(config.getJSONObject("api"));

			// Start services
			log.info("Starting services");
			serviceManager.start();
			// for(Service service:serviceList){
			//	service.start();
			// }

			// Setup shutdown hook
			Runtime.getRuntime().addShutdownHook(new Thread(this));
			log.info("Access web interface at http://127.0.0.1:8080/");
			log.info("Ready!");
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * This method is added as a shutdown hook to the runtime.
	 */
	public void run(){
		// Stop services
		// for(Service service:serviceList){
		//	try{
		//		service.stop();
		//	}catch(Exception e){}
		//}
		try{
			serviceManager.stop();
		}catch(Exception e){}
		// Stop servlets
		try{
			server.stop();
		}catch(Exception e){}
		// Stop streamHandler
		try{
			streamHandler.stop();
		}catch(Exception e){}
	}



	private void patchConfiguration(JSONObject config) throws JSONException{
		// 1. Patch config with defauls
		/*
		config.putOnce("streams",new JSONObject());
		JSONObject obj=config.getJSONObject("streams");
		obj.putOnce("path","./streamdata/");
		obj.putOnce("commit_batch_size",32);
		obj.putOnce("commit_batch_timeout",50);

		config.putOnce("api",new JSONObject());
		obj=config.getJSONObject("api");
		obj.putOnce("port",8080);*/
		if(!config.has("streams")){
			config.put("streams",new JSONObject());
		}
		JSONObject obj=config.getJSONObject("streams");
		if(!obj.has("path")){
			obj.put("path","./streamdata/");
		}
		if(!obj.has("commit_batch_size")){
			obj.put("commit_batch_size",32);
		}
		if(!obj.has("commit_batch_timeout")){
			obj.put("commit_batch_timeout",50);
		}
		if(!config.has("api")){
			config.put("api",new JSONObject());
		}
		obj=config.getJSONObject("api");
		if(!obj.has("port")){
			obj.put("port",8080);
		}
		// 2. Overlay environment variables
		Map<String,String>env=System.getenv();
		
	}

	

	/**
	 * Setup and start the Jetty servlet container
	 */
	private void startServlets(JSONObject config) throws JSONException{
		try{
			
			server = new Server();
			ServerConnector c = new ServerConnector(server);
			c.setIdleTimeout(15000);
			c.setAcceptQueueSize(256);
			c.setPort(config.getInt("port"));
			// if(!bind.equals("*")){
			//	c.setHost(bind);
			// }

			ServletContextHandler handler = new ServletContextHandler(server,"/", true, false);
			ServletHolder servletHolder = new ServletHolder(StreamAPIServlet.class);
			handler.addServlet(servletHolder, "/stream/*");
			servletHolder = new ServletHolder(ServiceAPIServlet.class);
			handler.addServlet(servletHolder, "/service/*");
			servletHolder = new ServletHolder(ScriptAPIServlet.class);
			handler.addServlet(servletHolder, "/script/*");
			servletHolder = new ServletHolder(AggregateAPIServlet.class);
			handler.addServlet(servletHolder, "/aggregate/*");
			servletHolder = new ServletHolder(KeyValueAPIServlet.class);
			handler.addServlet(servletHolder, "/kvstore/*");
			servletHolder = new ServletHolder(SystemAPIServlet.class);
			handler.addServlet(servletHolder, "/system/*");
			servletHolder = new ServletHolder(QueryAPIServlet.class);
			handler.addServlet(servletHolder, "/query/*");
			servletHolder = new ServletHolder(FileServlet.class);
			handler.addServlet(servletHolder, "/*");

			server.addConnector(c);
			server.start();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * Starting point for the PhatData reference implementation server
	 */
	public static void main(String args[]){
		String configLocation=null;
		if(args.length==1){ // The only argument accepted for now is the path to a config file
			configLocation=args[0];
		}else if(args.length==0){

		}else{
			System.out.println("ERROR! The Stroom reference implementation must be started with either one or zero arguments");
			System.out.println("java -jar multihost.jar <configuration location>");
			System.exit(1);
		}
		new MultiHostServer(configLocation);
	}
}