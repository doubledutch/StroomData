package me.doubledutch.stroom.perf;

import java.io.File;
import org.junit.*;
import static org.junit.Assert.*;
import org.json.JSONObject;
import java.util.List;
import java.util.ArrayList;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class BatchMetricTest{
	@Test
	public void testMetricTimer(){
		BatchMetric m=new BatchMetric();
		assertEquals(0,m.get("foo"));
		m.startTimer("foo");
		try{
			Thread.sleep(5);
		}catch(Exception e){}
		m.stopTimer("foo");
		assertNotEquals(0,m.get("foo"));
	}

	@Test
	public void testSet(){
		BatchMetric m=new BatchMetric();
		assertEquals(0,m.get("foo"));
		m.set("foo",42);
		assertEquals(42,m.get("foo"));
	}

	@Test
	public void testInc(){
		BatchMetric m=new BatchMetric();
		assertEquals(0,m.get("foo"));
		m.inc("foo");
		assertEquals(1,m.get("foo"));
	}

	@Test
	public void testRate(){
		BatchMetric m=new BatchMetric();
		m.set("batch.time",420000);
		m.setSamples(9);
		assertNotEquals(0.0,m.getRate());
	}

	@Test
	public void testInspection(){
		BatchMetric m=new BatchMetric();
		m.set("batch.time",420000);
		m.setSamples(9);
		m.set("foo",99);
		assertNotNull(m.toJSON());
	}
}