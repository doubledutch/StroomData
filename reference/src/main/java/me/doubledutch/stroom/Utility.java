package me.doubledutch.stroom;

import java.io.*;
import java.net.*;

public class Utility{
	/**
	 * Read the full contents of a text file
	 */
	public static String readFile(String filename) throws IOException{
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		StringBuilder buf=new StringBuilder();
		char[] data=new char[8192];
		int num=reader.read(data);
		while(num>-1){
			buf.append(data,0,num);
			num=reader.read(data);
		}
		return buf.toString();
	}

	public static String postURL(String strurl,String body){
		try{
			URL url=new URL(strurl);
			HttpURLConnection con = (HttpURLConnection)url.openConnection();
			con.setRequestMethod("POST");
			con.addRequestProperty("Content-Type", "application/json");
			byte[] outdata=body.getBytes("UTF-8");
			con.setRequestProperty("Content-Length",""+outdata.length);
			con.getOutputStream().write(outdata);

			try(Reader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"))){
				StringBuilder buf = new StringBuilder();
			    char[] indata = new char[32768];
			    int num = reader.read(indata);
			    while (num > -1) {
			      	buf.append(indata, 0, num);
			      	num = reader.read(indata);
			    }
			    return buf.toString();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
}