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

import me.doubledutch.stroom.query.*;

import org.json.*;

public class QueryAPIServlet extends HttpServlet{
	private final Logger log = Logger.getLogger("SQLAPI");

	private static StreamHandler streamHandler;

	public static void setStreamHandler(StreamHandler streamHandlerArg){
		streamHandler=streamHandlerArg;
	}

	@Override
	protected void doDelete(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		try{
			Writer out=response.getWriter();
			
			String uriPath=request.getRequestURI().substring(request.getServletPath().length());
			if(uriPath.startsWith("/"))uriPath=uriPath.substring(1).trim();
			String[] splitPath=uriPath.split("/");
			// System.out.println(splitPath.length+" "+splitPath[0]);
			if(uriPath.length()>0){
				// System.out.println("id "+splitPath[0].trim());
				QueryManager.get().delete(splitPath[0].trim());
				response.setContentType("application/json");
				out.append("{\"result\":\"ok\"}");
				
				return;				
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
			Writer out=response.getWriter();
			response.setContentType("application/json");
			String script=readPostBody(request);
			System.out.println(script);
			JSONObject obj=new JSONObject(script);
			Query q=QueryManager.get().run(obj.getString("query"),obj.getString("type"));
			out.append(q.toJSON().toString());
		}catch(Exception e){
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR ,"Internal server error");
		}
	}

	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		try{
			Writer out=response.getWriter();
			
			String uriPath=request.getRequestURI().substring(request.getServletPath().length());
			if(uriPath.startsWith("/"))uriPath=uriPath.substring(1).trim();
			String[] splitPath=uriPath.split("/");
			// System.out.println(splitPath.length+" "+splitPath[0]);
			if(uriPath.length()>0){
				// System.out.println("id "+splitPath[0].trim());
				Query q=QueryManager.get().get(splitPath[0].trim());
				if(q!=null){

					response.setContentType("application/json");
					out.append(q.toJSON().toString());
				}else{
					response.sendError(HttpServletResponse.SC_NOT_FOUND);
				}
				return;				
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