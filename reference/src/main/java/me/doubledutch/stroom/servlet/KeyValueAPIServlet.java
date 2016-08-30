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

public class KeyValueAPIServlet extends HttpServlet{
	private final Logger log = Logger.getLogger("KeyValueAPI");

	private static StreamHandler streamHandler;

	public static void setStreamHandler(StreamHandler streamHandlerArg){
		streamHandler=streamHandlerArg;
	}

	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		try{
			response.setContentType("application/json; charset=UTF-8");
			Writer out=response.getWriter();
			String uriPath=request.getRequestURI().substring(request.getServletPath().length());
			if(uriPath.startsWith("/"))uriPath=uriPath.substring(1).trim();
			String[] splitPath=uriPath.split("/");
			if(uriPath.length()==0){
				// List kvstores
				// JSONArray result=new JSONArray();
				JSONArray result=KeyValueManager.get().list();
				/*
				for(Stream stream:streamHandler.getStreams()){
					if(stream.getTopic().indexOf(".")==-1){
						result.put(stream.toJSON());
					}
				}*/
				out.write(result.toString());
				return;
			}else if(splitPath.length==1){
				// List keys
				JSONArray result=new JSONArray();
				for(String key:KeyValueManager.get().listKeys(splitPath[0])){
					result.put(key);
				}
				out.write(result.toString());
				return;
			}else if(splitPath.length==2){
				// Get partitioned aggregate by partition key
				String id=splitPath[0];
				String key=splitPath[1];
				String result=KeyValueManager.get().getValue(id,key);
				out.append(result);
			}else{
				// response.sendError(HttpServletResponse.SC_NOT_FOUND,"You must specify both topic and location.");
			}
		}catch(Exception e){
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR ,"Internal server error");
		}
		return;
	}
}