package me.doubledutch.stroom.filters;

import java.io.File;
import org.junit.*;
import static org.junit.Assert.*;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.List;
import java.util.ArrayList;
import java.net.*;
import java.nio.charset.StandardCharsets;
import me.doubledutch.stroom.perf.*;
import me.doubledutch.stroom.streams.*;
import me.doubledutch.stroom.*;


public class FilterTest{
	private static String dataLocation="./filtertestdata/";
	private static StreamHandler streams=null;
	private static ScriptManager scripts=null;

	private static void createStreamHandler() throws Exception{
		JSONObject obj=new JSONObject();
		obj.putOnce("path",dataLocation);
		obj.putOnce("commit_batch_size",32);
		obj.putOnce("commit_batch_timeout",50);
		streams=new StreamHandler(obj);
		scripts=new ScriptManager(streams);
	}

	private static void deleteFile(String fileName) throws Exception{
		File ftest=new File(fileName);
		if(ftest.isFile()){
			ftest.delete();
		}else{
			if(!fileName.endsWith(File.separator))fileName+=File.separator;
			String[] list=ftest.list();
			for(String child:list){
				deleteFile(fileName+child);
			}
			ftest.delete();
		}
	}

	private static void cleanup() throws Exception{
		File ftest=new File(dataLocation);
		if(ftest.exists()){
			deleteFile(dataLocation);
		}
	}

	@BeforeClass
    public static void init() throws Exception{
    	cleanup();
    	createStreamHandler();
    }

    @AfterClass
    public static void destroy() throws Exception{
    	cleanup();
    }

    @Test
    public void testFilterCreation() throws Exception{
    	scripts.setScript("foo.js","function map(obj){return obj;}");
    	JSONObject obj=new JSONObject();
    	obj.put("id","test-1");
    	obj.put("batch_size",1);
    	JSONObject streamsObj=new JSONObject();
    	streamsObj.put("input","local://direct/stream/input1");
    	streamsObj.put("output","local://direct/stream/output1");
    	streamsObj.put("state","local://direct/stream/state1");
    	obj.put("streams",streamsObj);
    	obj.put("type","javascript");
    	obj.put("script","foo.js");

    	FilterService filter=new FilterService(streams,obj);
        Stream output=streams.getOrCreateStream("output1");
        for(int i=0;i<10;i++){
	    	Document doc=new Document("input1","{\"foo\":"+i+"}");
   	     	streams.addDocument(doc);
   	     }
    	assertEquals(output.getCount(),0);
    	filter.start();
        Thread.sleep(200);
        assertNotEquals(output.getCount(),0);
    	filter.stop();
    }
}