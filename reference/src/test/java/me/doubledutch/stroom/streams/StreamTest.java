package me.doubledutch.stroom.streams;

import java.io.File;
import org.junit.*;
import static org.junit.Assert.*;
import org.json.JSONObject;
import java.util.List;
import java.util.ArrayList;
import java.net.*;

public class StreamTest{
	private static String dataLocation="./streamtestdata/";
	private static StreamHandler streams=null;

	private static void createStreamHandler() throws Exception{
		JSONObject obj=new JSONObject();
		obj.putOnce("path",dataLocation);
		obj.putOnce("commit_batch_size",32);
		obj.putOnce("commit_batch_timeout",50);
		streams=new StreamHandler(obj);
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

    private static boolean stringInArray(String str,String[] data){
    	for(String d:data){
    		if(d.equals(str))return true;
    	}
    	return false;
    }

    private static boolean streamInArray(String str,Stream[] data){
    	for(Stream s:data){
    		if(s.getTopic().equals(str))return true; 
    	}
    	return false;
    }

	@Test
    public void testStreamCreation() throws Exception{
    	assertFalse(streamInArray("create-test",streams.getStreams()));
    	assertNotNull(streams.getOrCreateStream("create-test"));
    	assertTrue(streamInArray("create-test",streams.getStreams()));
    	Stream stream=streams.getOrCreateStream("create-test");
    	assertEquals(stream.getTopic(),"create-test");
    }

    @Test
    public void testDocumentStorage() throws Exception{
    	Document doc=new Document("document-test","{}");
    	streams.addDocument(doc);
    	Document doc2=streams.getDocument("document-test",doc.getLocation());
    	assertArrayEquals(doc.getData(),doc2.getData());
    	assertEquals(doc.getStringData(),doc2.getStringData());
    }

    @Test
    public void testUnicodeDocumentStorage() throws Exception{
    	String data="{\"data\":\"æøå😝\"}";
    	Document doc=new Document("document-test",data);
    	streams.addDocument(doc);
    	Document doc2=streams.getDocument("document-test",doc.getLocation());
    	assertArrayEquals(doc.getData(),doc2.getData());
    	assertEquals(doc.getStringData(),doc2.getStringData());
    }

    @Test
    public void testRangeRequests() throws Exception{
      List<Document> result=streams.getDocuments("range-test",0,99);
      assertEquals(result.size(),0);
    	for(int i=0;i<99;i++){
	    	Document doc=new Document("range-test","{\"value\":"+i+"}");
  			streams.addDocument(doc,Stream.NONE);
  		}
  		Document doc=new Document("range-test","{\"value\":99}");
  		streams.addDocument(doc);
  		result=streams.getDocuments("range-test",0,99);
  		assertEquals(result.size(),100);
  		for(int i=0;i<100;i++){
  			assertEquals(i,result.get(i).getLocation());
  		}
  		result=streams.getDocuments("range-test",0,1000);
  		assertEquals(result.size(),100);
  		for(int i=0;i<100;i++){
  			assertEquals(i,result.get(i).getLocation());
  		}
  		result=streams.getDocuments("range-test",10,19);
  		assertEquals(result.size(),10);
  		for(int i=10;i<20;i++){
  			assertEquals(i,result.get(i-10).getLocation());
  		}
  		result=streams.getDocuments("range-test",72,72);
  		assertEquals(result.size(),1);
  		assertEquals(72,result.get(0).getLocation());
    }

    @Test
    public void testTruncate() throws Exception{
      for(int i=0;i<99;i++){
        Document doc=new Document("truncate-test","{\"value\":"+i+"}");
        streams.addDocument(doc,Stream.NONE);
      }
      Document doc=new Document("truncate-test","{\"value\":99}");
      streams.addDocument(doc);

      List<Document> result=streams.getDocuments("truncate-test",0,99);
      assertEquals(result.size(),100);

      streams.truncateStream("truncate-test",50);
      result=streams.getDocuments("truncate-test",0,99);
      assertEquals(result.size(),50);

      streams.truncateStream("truncate-test",10);
      result=streams.getDocuments("truncate-test",0,99);
      assertEquals(result.size(),10);
      doc=new Document("truncate-test","{\"value\":100}");
      streams.addDocument(doc);
      result=streams.getDocuments("truncate-test",0,100);
      assertEquals(result.size(),11);

      streams.truncateStream("truncate-test",0);
      result=streams.getDocuments("truncate-test",0,99);
      assertEquals(result.size(),0);
    }

    @Test
    public void testDocumentCount() throws Exception{
    	Stream stream=streams.getOrCreateStream("count-test");
    	assertEquals(0,stream.getCount());
    	Document doc=new Document("count-test","{}");
    	streams.addDocument(doc);
    	assertEquals(1,stream.getCount());
    	doc=new Document("count-test","{}");
    	streams.addDocument(doc);
    	assertEquals(2,stream.getCount());
    }

    @Test
    public void testBatchAdd() throws Exception{
    	List<Document> data=new ArrayList<Document>(100);
       	for(int i=0;i<100;i++){
	    	Document doc=new Document("bulk-test","{\"value\":"+i+"}");
  			data.add(doc);
  		}
  		streams.addDocuments(data);

  		List<Document> result=streams.getDocuments("bulk-test",0,99);
  		assertEquals(result.size(),100);
  		for(int i=0;i<100;i++){
  			assertEquals(i,result.get(i).getLocation());
  		}
    }

}