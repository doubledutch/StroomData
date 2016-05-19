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
import me.doubledutch.stroom.*;

import org.json.*;

public class ServiceAPIServlet extends HttpServlet{
	private final Logger log = Logger.getLogger("ServiceAPI");

	private static StreamHandler streamHandler;

	public static void setStreamHandler(StreamHandler streamHandlerArg){
		streamHandler=streamHandlerArg;
	}

	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		try{
			Writer out=response.getWriter();
			response.setContentType("application/json");
			String uriPath=request.getRequestURI().substring(request.getServletPath().length());
			if(uriPath.startsWith("/"))uriPath=uriPath.substring(1).trim();
			String[] splitPath=uriPath.split("/");
			if(uriPath.length()==0){
				// Create new service
				String config=readPostBody(request);
				ServiceManager.get().addService(new JSONObject(config));
				JSONObject result=new JSONObject();
				result.put("status","ok");
				out.append(result.toString());
			}else if(splitPath.length==2){
				// Send command to service
				// /service/:id/:command - the available commands are stop, start, restart, reset, disable and enable.
				String id=splitPath[0];
				String command=splitPath[1].trim().toLowerCase();
				if(command.equals("stop")){
					ServiceManager.get().stopService(id);
				}else if(command.equals("start")){
					ServiceManager.get().startService(id);
				}else if(command.equals("restart")){
					ServiceManager.get().restartService(id);
				}else if(command.equals("reset")){
					ServiceManager.get().resetService(id);
				}else if(command.equals("disable")){
					ServiceManager.get().disableService(id);
				}else if(command.equals("enable")){
					ServiceManager.get().enableService(id);
				}else{
					JSONObject result=new JSONObject();
					result.put("status","err");
					result.put("error","Unknown command "+command);
					out.append(result.toString());
					return;
				}
				JSONObject result=new JSONObject();
				result.put("status","ok");
				out.append(result.toString());
			}
		}catch(Exception e){
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR ,"Internal server error");
		}
	}

	@Override
	protected void doPut(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		try{
			Writer out=response.getWriter();
			response.setContentType("application/json");
			String uriPath=request.getRequestURI().substring(request.getServletPath().length());
			if(uriPath.startsWith("/"))uriPath=uriPath.substring(1).trim();
			String[] splitPath=uriPath.split("/");
			if(splitPath.length==1){
				// Update a service
				String config=readPostBody(request);
				ServiceManager.get().updateService(splitPath[0],new JSONObject(config));
				JSONObject result=new JSONObject();
				result.put("status","ok");
				out.append(result.toString());
			}else if(splitPath.length==2){
				// Update and send command to service
				// /service/:id/:command - the available commands are stop, start, restart, reset, disable and enable.
			}
		}catch(Exception e){
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR ,"Internal server error");
			/*JSONObject obj=new JSONObject();
			obj.put("status","err");
			obj.put("error",e.toString());*/
		}
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
				// List services
				JSONArray result=ServiceManager.get().toJSON();
				out.write(result.toString());
				return;
			}else if(splitPath.length==1){
				// Get specific service status
				String id=splitPath[0];
				JSONObject result=ServiceManager.get().toJSON(id);
				// Stream stream=streamHandler.getOrCreateStream(topic);
				// out.append(stream.toJSON().toString());
			}else{
				response.sendError(HttpServletResponse.SC_NOT_FOUND,"You must specify either a service id or nothing at all...");
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