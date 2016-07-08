package me.doubledutch.stroom.client;

import me.doubledutch.stroom.client.function.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.function.*;
import org.json.*;

public class Stroom{
	private String host=null;
	
	public Stroom(){

	}

	public Stroom(String host){
		if(!host.endsWith("/"))host=host+"/";
		this.host=host;
	}

	public Service filter(String inputStream,JSONObjectFunction func,String outputstream){
		return Service.filter(this,inputStream,func,outputstream);
	}

	public Service filter(String inputStream,JSONObjectArrayFunction func,String outputstream){
		return Service.filter(this,inputStream,func,outputstream);
	}

	public Service filter(String inputStream,StringListFunction func,String outputstream){
		return Service.filter(this,inputStream,func,outputstream);
	}

	public Service filter(String inputStream,StringFunction func,String outputstream){
		return Service.filter(this,inputStream,func,outputstream);
	}

	public Service filter(String inputStream,JSONObjectPredicate func,String outputstream){
		return Service.filter(this,inputStream,func,outputstream);
	}

	public Service filter(String inputStream,StringPredicate func,String outputstream){
		return Service.filter(this,inputStream,func,outputstream);
	}

	public Service aggregate(String inputStream,JSONObjectBiFunction func,String outputstream){
		return Service.aggregate(this,inputStream,func,outputstream);
	}

	public Service aggregate(String inputStream,StringBiFunction func,String outputstream){
		return Service.aggregate(this,inputStream,func,outputstream);
	}

	public StreamConnection openStream(String name){
		if(!name.endsWith("/"))name=name+"/";
		if(name.startsWith("http://")||name.startsWith("https://")){
			return new HttpStreamConnection(this,name);
		}else{
			return new HttpStreamConnection(this,host+"stream/"+name);
		}
	}

	protected String deleteURL(String strurl){
		return requestURL(strurl,"DELETE",null,null);
	}

	protected String postURL(String strurl,String body,Map<String,String> headers){
		return requestURL(strurl,"POST",body,headers);
	}

	protected String postURL(String strurl,String body){
		return requestURL(strurl,"POST",body,null);
	}

	protected String putURL(String strurl,String body){
		return requestURL(strurl,"PUT",body,null);
	}

	protected String getURL(String strurl){
		return requestURL(strurl,"GET",null,null);
	}

	protected String getURL(String strurl,String body){
		return requestURL(strurl,"GET",body,null);
	}

	private String requestURL(String strurl,String method,String body,Map<String,String> headers){
		try{
			URL url=new URL(strurl);
			HttpURLConnection con = (HttpURLConnection)url.openConnection();
			con.setRequestMethod(method);
			if(headers!=null){
				for(String key:headers.keySet()){
					con.addRequestProperty(key,headers.get(key));
				}
			}
			con.addRequestProperty("Content-Type", "application/json;charset=UTF-8");
			if(body!=null){
				byte[] outdata=body.getBytes("UTF-8");
				// con.setFixedLengthStreamingMode(outdata.length); 
				con.setRequestProperty("Content-Length",""+outdata.length);
				con.setDoOutput(true);
				con.getOutputStream().write(outdata);
			}
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