package me.doubledutch.stroom.streams;

import java.io.File;
import org.junit.*;
import static org.junit.Assert.*;
import org.json.JSONObject;
import java.util.List;
import java.util.ArrayList;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class BlockTest{
	private static String dataLocation="./blocktestdata/";

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
        File ftest=new File(dataLocation);
        ftest.mkdir();
    }

    @AfterClass
    public static void destroy() throws Exception{
    	cleanup();
    }

	@Test
    public void testBlockSyncCreation() throws Exception{
        Block b=new Block(dataLocation+"block01",Stream.SYNC);
        long l=b.write("foo".getBytes(StandardCharsets.UTF_8));
        b.commit();
        assertEquals(l,0);
    }

    @Test
    public void testBlockFlushCreation() throws Exception{
        Block b=new Block(dataLocation+"block02",Stream.FLUSH);
        long l=b.write("foo".getBytes(StandardCharsets.UTF_8));
        b.commit();
        assertEquals(l,0);
    }

    @Test
    public void testBlockIndexEntryRead() throws Exception{
        Block b=new Block(dataLocation+"block04",Stream.SYNC);
        byte[] raw="foo".getBytes(StandardCharsets.UTF_8);
        b.write(raw);
        b.commit();
        IndexEntry i=new IndexEntry(0l,(short)0,0l,raw.length);
        byte[] out=b.read(i);
        assertEquals(out.length,raw.length);
        assertEquals(out[0],raw[0]);
        assertEquals(out[1],raw[1]);
        assertEquals(out[2],raw[2]);
    }

    @Test
    public void testBlockSize() throws Exception{
      Block b=new Block(dataLocation+"block03",Stream.SYNC);
      byte[] raw="foo".getBytes(StandardCharsets.UTF_8);
      assertEquals(b.getSize(),0);
      long l=b.write(raw);
      assertEquals(b.getSize(),raw.length);
    }

}