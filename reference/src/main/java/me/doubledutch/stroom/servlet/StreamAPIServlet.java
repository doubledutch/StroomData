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
	final static int MAX_EVENTS=5000;
	final static int MAX_SIZE=5*1024*1024;
	private final Logger log = Logger.getLogger("StreamAPI");

	private static StreamHandler streamHandler;

	public static void setStreamHandler(StreamHandler streamHandlerArg){
		streamHandler=streamHandlerArg;
	}

	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		try{
			response.setCharacterEncoding("UTF-8");
			Writer out=response.getWriter();
			response.setContentType("application/json");
			String uriPath=request.getRequestURI().substring(request.getServletPath().length());
			if(uriPath.startsWith("/"))uriPath=uriPath.substring(1).trim();
			String[] splitPath=uriPath.split("/");
			if(uriPath.trim().length()==0){
				// List streams
				JSONArray result=new JSONArray();
				for(Stream stream:streamHandler.getStreams()){
					if(stream.getTopic().indexOf(".")==-1 && !stream.getTopic().startsWith("_")){
						result.put(stream.toJSON());
					}
				}
				out.write(result.toString());
				return;
			}else if(splitPath.length==1 || (splitPath.length==2 && splitPath[1].trim().length()==0)){
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
					if(range.equals("-1")){
						response.sendError(HttpServletResponse.SC_NOT_FOUND,"You can't specify a negative location");
						return;
					}
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
							out.append(",");
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
			if(splitPath.length==1){
				String topic=splitPath[0];
				streamHandler.deleteStream(topic);
				response.setContentType("application/json");
				response.getWriter().append("{\"result\":\"ok\"}");
			}else if(splitPath.length==2){
				String topic=splitPath[0];
				String index=splitPath[1];
				streamHandler.truncateStream(topic,Long.parseLong(index));
				response.setContentType("application/json");
				response.getWriter().append("{\"result\":\"ok\"}");
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
			request.setCharacterEncoding("UTF-8");
			String uriPath=request.getRequestURI().substring(request.getServletPath().length());
			if(uriPath.startsWith("/"))uriPath=uriPath.substring(1);
			String[] splitPath=uriPath.split("/");
			String hintHeader=request.getHeader("X-stroom-hint");
			int hint=-1;
			if(hintHeader!=null){
				hintHeader=hintHeader.toUpperCase().trim();
				if(hintHeader.equals("NONE")){
					hint=Stream.NONE;
				}else if(hintHeader.equals("FLUSH")){
					hint=Stream.FLUSH;
				}else if(hintHeader.equals("SYNC")){
					hint=Stream.SYNC;
				}else if(hintHeader.equals("LINEAR")){
					hint=Stream.LINEAR;
				}
			}
			if(splitPath.length==1){
				String topic=splitPath[0];
				String postBody=readPostBody(request);
				try{
					if(postBody.startsWith("[")){
						//  long pre=System.nanoTime();

						// DD.JSONJIT version

						me.doubledutch.stroom.jsonjit.JSONParser parser=new me.doubledutch.stroom.jsonjit.JSONParser(postBody);
						me.doubledutch.stroom.jsonjit.JSONArray array=parser.parseArray();
						List<Document> batch=new ArrayList<Document>(array.length());
						
						for(int n=0;n<array.length();n++){
							me.doubledutch.stroom.jsonjit.JSONObject jobj=array.getJSONObject(n);
							Document doc=new Document(topic,jobj.toString());
							batch.add(doc);
						}
						/*
						// JSON.simple version

						org.json.simple.parser.JSONParser parser = new org.json.simple.parser.JSONParser();
						Object obj = parser.parse(postBody);
						org.json.simple.JSONArray jsonArr=(org.json.simple.JSONArray)obj;
						List<Document> batch=new ArrayList<Document>(jsonArr.size());
						for(int i=0;i<jsonArr.size();i++){
							org.json.simple.JSONObject jobj=(org.json.simple.JSONObject)jsonArr.get(i);
							Document doc=new Document(topic,jobj.toString());
							batch.add(doc);
						}*/
						
						/*
						// Old version
						
						JSONArray jsonArr=new JSONArray(postBody);
						List<Document> batch=new ArrayList<Document>(jsonArr.length());
						for(int i=0;i<jsonArr.length();i++){
							JSONObject obj=jsonArr.getJSONObject(i);
							Document doc=new Document(topic,obj.toString());
							batch.add(doc);
						}
						
						*/
						// long post=System.nanoTime();
						
						// System.out.println(batch.size());
						// if(true)return;
						if(batch.size()>0){
							if(hint>-1){
								streamHandler.addDocuments(batch,hint);
							}else{
								streamHandler.addDocuments(batch);
							}
						}
						// long split1=System.nanoTime();
						response.setContentType("application/json");
						// JSONArray indexList=new JSONArray();
						StringBuilder buf=new StringBuilder();
						// buf.append("[");
						boolean first=true;
						for(Document doc:batch){
							if(first){
								first=false;
							}else{
								buf.append(",");
							}
							// indexList.put(doc.getLocation());
							buf.append(doc.getLocation());
						}
						// buf.append("]");
						response.getWriter().append("{\"location_list\":["+buf.toString()+"]}");
						// long split2=System.nanoTime();
						// if(Math.random()<0.01){
						//	System.out.println("Parse: "+((post-pre)/1000000.0)+" Write: "+((split1-post)/1000000.0)+" Result: "+((split2-split1)/1000000.0));
						// }

					}else{
						JSONObject jsonObj=new JSONObject(postBody);
						Document doc=new Document(topic,postBody);
						// streamHandler.addDocument(doc);
						if(hint>-1){
							streamHandler.addDocument(doc,hint);
						}else{
							streamHandler.addDocument(doc);
						}
						response.setContentType("application/json");
						response.getWriter().append("{\"location\":"+doc.getLocation()+"}");
						// log.info("Document of "+postBody.length()+" bytes added into "+topic+" at location "+doc.getLocation());
					}
				}catch(JSONException pe){
					response.setContentType("application/json");
					response.getWriter().append("{\"result\":\"err\",\"msg\":\"JSON syntax error\"}");
					log.info("Document rejected due to JSON syntax error");
					// System.out.println(postBody);
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
		char[] data=new char[4096];//32768];
		int num=reader.read(data);
		while(num>-1){
			buf.append(data,0,num);
			num=reader.read(data);
		}
		return buf.toString();
	}
}