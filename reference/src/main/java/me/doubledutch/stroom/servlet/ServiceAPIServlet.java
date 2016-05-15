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

public class ServiceAPIServlet extends HttpServlet{
	private final Logger log = Logger.getLogger("ServiceAPI");

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
				// List services
				JSONArray result=new JSONArray();
				/*
				for(Stream stream:streamHandler.getStreams()){
					if(stream.getTopic().indexOf(".")==-1){
						result.put(stream.toJSON());
					}
				}*/
				out.write(result.toString());
				return;
			}else if(splitPath.length==1){
				// Get specific service status
				String id=splitPath[0];
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
}