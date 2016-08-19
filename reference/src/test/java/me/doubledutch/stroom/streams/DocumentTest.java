package me.doubledutch.stroom.streams;

import java.io.File;
import org.junit.*;
import static org.junit.Assert.*;
import org.json.JSONObject;
import java.util.List;
import java.util.ArrayList;
import java.net.*;

public class DocumentTest{
	@Test
	public void testSimpleConstructor(){
		Document doc=new Document("foo","{\"foo\":42}");
		assertEquals(doc.getTopic(),"foo");
		assertEquals(doc.getStringData(),"{\"foo\":42}");
	}

	@Test
	public void testLocationConstructor(){
		Document doc=new Document("foo","{\"foo\":42}",42);
		assertEquals(doc.getTopic(),"foo");
		assertEquals(doc.getStringData(),"{\"foo\":42}");
		assertEquals(doc.getLocation(),42);
	}
}