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

public class ScriptAPIServlet extends HttpServlet{
	private final Logger log = Logger.getLogger("ScriptAPI");

	private static StreamHandler streamHandler;

	public static void setStreamHandler(StreamHandler streamHandlerArg){
		streamHandler=streamHandlerArg;
	}

	@Override
	protected void doDelete(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		try{
			Writer out=response.getWriter();
			response.setContentType("application/json");
			String uriPath=request.getRequestURI().substring(request.getServletPath().length());
			if(uriPath.startsWith("/"))uriPath=uriPath.substring(1).trim();
			String[] splitPath=uriPath.split("/");
			if(uriPath.length()==0){
				response.sendError(HttpServletResponse.SC_NOT_FOUND,"You must specify a script name...");
			}else{
				// Delete a script
				ScriptManager.get().deleteScript("/"+uriPath);
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
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		try{
			Writer out=response.getWriter();
			response.setContentType("application/json");
			String uriPath=request.getRequestURI().substring(request.getServletPath().length());
			if(uriPath.startsWith("/"))uriPath=uriPath.substring(1).trim();
			String[] splitPath=uriPath.split("/");
			if(uriPath.length()==0){
				response.sendError(HttpServletResponse.SC_NOT_FOUND,"You must specify a script name...");
			}else{
				// Create new script
				String script=readPostBody(request);
				ScriptManager.get().setScript("/"+uriPath,script);
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
		doPost(request,response);
	}

	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		try{
			Writer out=response.getWriter();
			
			String uriPath=request.getRequestURI().substring(request.getServletPath().length());
			if(uriPath.startsWith("/"))uriPath=uriPath.substring(1).trim();
			String[] splitPath=uriPath.split("/");
			// System.out.println(splitPath.length+" "+splitPath[0]);
			if(uriPath.length()==0){
				// List scripts
				response.setContentType("application/json");
				JSONArray result=ScriptManager.get().toJSON();
				out.write(result.toString());
				return;
			}else{
				// TODO: for now, we always get a specific script.. eventually, a sublist ability would be great
				
				String script=ScriptManager.get().getScript("/"+uriPath);
				if(script==null){
					response.sendError(HttpServletResponse.SC_NOT_FOUND,"Script '/"+uriPath+"' not found...");
				}else{
					response.setContentType("text/plain");
					out.append(script);
				}
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