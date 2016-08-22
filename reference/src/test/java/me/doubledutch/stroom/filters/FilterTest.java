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
    public void testFilterCycle() throws Exception{
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
   	     	streams.addDocument(doc,Stream.FLUSH);
   	     }
    	assertEquals(output.getCount(),0);
    	filter.start();
        Thread.sleep(200);
        assertNotEquals(output.getCount(),0);
        // filter.reset();
        filter.stop();
        // System.out.println("state count should NOT be 0: "+streams.getOrCreateStream("state1").getCount());
        filter.start();
        assertNotEquals(output.getCount(),0);
        // assertEquals(output.getCount(),0);
        while(!filter.isRunning()){
	        Thread.sleep(50);
	    }
    	filter.stop();
    	filter.reset();
    	assertEquals(output.getCount(),0);
    	// System.out.println("state count should be 0: "+streams.getOrCreateStream("state1").getCount());
    	filter.start();
    	while(!filter.isRunning()){
	        Thread.sleep(50);
	    }
    	// System.out.println("state count should be 0: "+streams.getOrCreateStream("state1").getCount());
    	filter.stop();

    	assertNotNull(filter.toJSON());
    }

    @Test
    public void testMultipleOutputFilter() throws Exception{
    	scripts.setScript("bar.js","function map(obj){return [{\"foo\":1},{\"foo\":2},{\"foo\":3},{\"foo\":4}];}");
    	JSONObject obj=new JSONObject();
    	obj.put("id","test-3");
    	obj.put("batch_size",1);
    	JSONObject streamsObj=new JSONObject();
    	streamsObj.put("input","local://direct/stream/input3");
    	streamsObj.put("output","local://direct/stream/output3");
    	streamsObj.put("state","local://direct/stream/state3");
    	obj.put("streams",streamsObj);
    	obj.put("type","javascript");
    	obj.put("script","bar.js");

    	FilterService filter=new FilterService(streams,obj);
        Stream output=streams.getOrCreateStream("output3");
        Document doc=new Document("input3","{\"foo\":42}");
   	    streams.addDocument(doc,Stream.FLUSH);
    	assertEquals(output.getCount(),0);
    	filter.start();
        Thread.sleep(200);
        assertEquals(output.getCount(),4);
        filter.stop();
    }

    @Test
    public void testSampleFilter() throws Exception{
    	JSONObject obj=new JSONObject();
    	obj.put("id","test-2");
    	obj.put("batch_size",1);
    	JSONObject streamsObj=new JSONObject();
    	streamsObj.put("input","local://direct/stream/input2");
    	streamsObj.put("output","local://direct/stream/output2");
    	streamsObj.put("state","local://direct/stream/state2");
    	obj.put("streams",streamsObj);
    	obj.put("type","sample");
    	obj.put("sample_rate","0.1");

    	FilterService filter=new FilterService(streams,obj);
        Stream output=streams.getOrCreateStream("output2");
        for(int i=0;i<100;i++){
	    	Document doc=new Document("input2","{\"foo\":"+i+"}");
   	     	streams.addDocument(doc,Stream.FLUSH);
   	     }
    	assertEquals(output.getCount(),0);
    	filter.start();
        Thread.sleep(200);
        assertNotEquals(output.getCount(),0);
        // filter.reset();
        filter.stop();
    }
}