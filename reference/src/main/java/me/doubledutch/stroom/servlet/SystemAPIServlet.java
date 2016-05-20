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

public class SystemAPIServlet extends HttpServlet{
	private static final Logger log = Logger.getLogger("SystemAPI");

	private static String BUILD_DATE;
	private static String BUILD_VERSION;
	private static String BUILD_NUMBER;

	static{
		try{
			Properties props = new Properties();
			props.load(SystemAPIServlet.class.getResourceAsStream("/version.properties"));
			BUILD_DATE=props.getProperty("BUILD_DATE");
			BUILD_VERSION=props.getProperty("BUILD_VERSION");
			BUILD_NUMBER=props.getProperty("BUILD_NUMBER");
		}catch(Exception e){
			e.printStackTrace();
		}
	}

		@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		try{
			JSONObject obj=new JSONObject();
			obj.put("build_date",BUILD_DATE);
			obj.put("build_version",BUILD_VERSION);
			obj.put("build_number",BUILD_NUMBER);

			Runtime rt=Runtime.getRuntime();
			obj.put("cpu_cores",rt.availableProcessors());
			obj.put("memory_free",rt.freeMemory());
			obj.put("memory_max",rt.maxMemory());
			obj.put("memory_total",rt.totalMemory());
			obj.put("memory_used",rt.totalMemory()-rt.freeMemory());

			ThreadGroup group=ServiceManager.get().getThreadGroup();
			obj.put("service_threads",group.activeCount());

			File ftest=new File(MultiHostServer.getConfig().getJSONObject("streams").getString("path"));
			obj.put("disk_free",ftest.getFreeSpace());
			obj.put("disk_total",ftest.getTotalSpace());
			obj.put("disk_used",ftest.getTotalSpace()-ftest.getFreeSpace());
			// System.out.println("threads "+group.activeCount());
			Writer out=response.getWriter();
			response.setContentType("application/json");
			out.append(obj.toString());
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}