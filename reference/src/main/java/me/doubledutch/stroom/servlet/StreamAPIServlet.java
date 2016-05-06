package me.doubledutch.stroom.servlet;

import org.apache.log4j.Logger;

import java.io.IOException;

import java.lang.reflect.*;
import java.io.*;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import me.doubledutch.stroom.streams.*;

import org.json.*;

public class StreamAPIServlet extends HttpServlet{
	final static int MAX_EVENTS=1000;
	final static int MAX_SIZE=512*1024;
	private final Logger log = Logger.getLogger("StreamAPI");

	private static StreamHandler streamHandler;

	public static void setStreamHandler(StreamHandler streamHandlerArg){
		streamHandler=streamHandlerArg;
	}

	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		try{
			Writer out=response.getWriter();
			response.setContentType("application/json");
			String uriPath=request.getRequestURI().substring(request.getServletPath().length());
			if(uriPath.startsWith("/"))uriPath=uriPath.substring(1).trim();
			String[] splitPath=uriPath.split("/");
			if(uriPath.length()==0){
				// List streams
				JSONArray result=new JSONArray();
				for(Stream stream:streamHandler.getStreams()){
					if(stream.getTopic().indexOf(".")==-1){
						result.put(stream.toJSON());
					}
				}
				out.write(result.toString());
				return;
			}else if(splitPath.length==1){
				String topic=splitPath[0];
				Stream stream=streamHandler.getOrCreateStream(topic);
				out.append(stream.toJSON().toString());
			}else if(splitPath.length==2){
				String topic=splitPath[0];
				String range=splitPath[1];
				if(range.equals("_")){
					long location=streamHandler.getOrCreateStream(topic).getCount()-1;
					if(location>-1){
						Document doc=streamHandler.getDocument(topic,location);
						out.append(doc.getStringData());
					}
				}else if(range.indexOf("-")==-1){
					// Get a single event
					long location=Long.parseLong(splitPath[1]);
					Document doc=streamHandler.getDocument(topic,location);
					out.append(doc.getStringData());
				}else if(range.endsWith("-")){
					// Get everything from an index
					long location=Long.parseLong(range.substring(0,range.length()-1));
					int count=0;
					int size=0;
					List<Document> list=streamHandler.getDocuments(topic,location,location+MAX_EVENTS);
					// TODO: figure out if we should be doing json array output less hacky
					out.append("[");
					boolean first=true;
					for(Document doc:list){
						if(first){
							first=false;
						}else{
							out.append(",\n");
						}
						out.append(doc.getStringData());
						
						count++;
						size+=doc.getStringData().length();
						// Sanity check size of request
						if(size>MAX_SIZE){
							out.append("]");
							return;
							// TODO: perhaps this should throw an actual error?
						}
					}
					out.append("]");
				}else{
					// Get everything between two indexes
					long startLocation=Long.parseLong(range.substring(0,range.indexOf("-")));
					long endLocation=Long.parseLong(range.substring(range.indexOf("-")+1));
					if(endLocation-startLocation>MAX_EVENTS){
						endLocation=startLocation+MAX_EVENTS;
					}
					int count=0;
					int size=0;

					List<Document> list=streamHandler.getDocuments(topic,startLocation,endLocation);
					out.append("[");
					boolean first=true;
					for(Document doc:list){
						if(first){
							first=false;
						}else{
							out.append(",\n");
						}
						out.append(doc.getStringData());
						count++;
						size+=doc.getStringData().length();
						// Sanity check size of request
						if(size>MAX_SIZE){
							out.append("]");
							return;
							// TODO: perhaps this should throw an actual error?
						}
					}
					out.append("]");
				}
			}else{
				response.sendError(HttpServletResponse.SC_NOT_FOUND,"You must specify both topic and location.");
			}
		}catch(Exception e){
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR ,"Internal server error");
		}
		return;
	}

	@Override
	protected void doDelete(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		try{
			Writer out=response.getWriter();
			response.setContentType("application/json");
			String uriPath=request.getRequestURI().substring(request.getServletPath().length());
			if(uriPath.startsWith("/"))uriPath=uriPath.substring(1).trim();
			String[] splitPath=uriPath.split("/");
			if(splitPath.length==2){
				String topic=splitPath[0];
				String index=splitPath[1];
				streamHandler.truncateStream(topic,Long.parseLong(index));
			}else{
				response.sendError(HttpServletResponse.SC_NOT_FOUND,"You must specify both topic and location.");
			}
		}catch(Exception e){
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR ,"Internal server error");
		}
		return;
	}

	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		try{
			String uriPath=request.getRequestURI().substring(request.getServletPath().length());
			if(uriPath.startsWith("/"))uriPath=uriPath.substring(1);
			String[] splitPath=uriPath.split("/");
			if(splitPath.length==1){
				String topic=splitPath[0];
				String postBody=readPostBody(request);
				try{
					if(postBody.startsWith("[")){
						JSONArray jsonArr=new JSONArray(postBody);
						List<Document> batch=new ArrayList<Document>(jsonArr.length());
						for(int i=0;i<jsonArr.length();i++){
							JSONObject obj=jsonArr.getJSONObject(i);
							Document doc=new Document(topic,obj.toString());
							batch.add(doc);
						}
						streamHandler.addDocuments(batch);
						response.setContentType("application/json");
						JSONArray indexList=new JSONArray();
						for(Document doc:batch){
							indexList.put(doc.getLocation());
						}
						response.getWriter().append("{\"location_list\":"+indexList.toString()+"}");
					}else{
						JSONObject jsonObj=new JSONObject(postBody);
						Document doc=new Document(topic,postBody);
						streamHandler.addDocument(doc);
						response.setContentType("application/json");
						response.getWriter().append("{\"location\":"+doc.getLocation()+"}");
						// log.info("Document of "+postBody.length()+" bytes added into "+topic+" at location "+doc.getLocation());
					}
				}catch(JSONException pe){
					response.setContentType("application/json");
					response.getWriter().append("{\"result\":\"err\",\"msg\":\"JSON syntax error\"}");
					log.info("Document rejected due to JSON syntax error");
				}	
			}else{
				response.sendError(HttpServletResponse.SC_NOT_FOUND,"Documents posted to the api must be posted to a topic.");
			}
		}catch(Exception e){
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR ,"Internal server error");
		}
		return;
	}

	private String readPostBody(final HttpServletRequest request) throws IOException{
		BufferedReader reader = request.getReader();
		StringBuilder buf=new StringBuilder();
		char[] data=new char[32768];
		int num=reader.read(data);
		while(num>-1){
			buf.append(data,0,num);
			num=reader.read(data);
		}
		return buf.toString();
	}
}