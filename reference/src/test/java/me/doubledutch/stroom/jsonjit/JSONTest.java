package me.doubledutch.stroom.query.sql;

import java.io.File;
import org.junit.*;
import static org.junit.Assert.*;
// import org.json.JSONObject;
import java.util.List;
import java.util.ArrayList;
import java.net.*;
import me.doubledutch.stroom.jsonjit.*;

public class JSONTest{
	@Test
    public void testStringFields() throws Exception{
        String str="{\"foo\":\"bar\",\"baz\":\"\"}";
        JSONObject obj=new JSONObject(str);
        String value=obj.getString("foo");
        assertNotNull(value);
        assertEquals(value,"bar");
        value=obj.getString("baz");
        assertNotNull(value);
        assertEquals(value,"");
    }

    @Test
    public void testIntegerFields() throws Exception{
        String str="{\"foo\":999,\"baz\":42}";
        JSONObject obj=new JSONObject(str);
        assertEquals(999,obj.getInt("foo"));
        assertEquals(42,obj.getInt("baz"));
    }

     @Test
    public void testObjectSpaces() throws Exception{
        String str=" {    \"foo\" :\"bar\" ,   \"baz\":  42}   ";
        JSONObject obj=new JSONObject(str);
        assertEquals("bar",obj.getString("foo"));
        assertEquals(42,obj.getInt("baz"));
    }

    @Test
    public void testObjectTabs() throws Exception{
        String str="\t{\t\"foo\"\t:\"bar\"\t,\t\t\"baz\":\t42\t}\t";
        JSONObject obj=new JSONObject(str);
        assertEquals("bar",obj.getString("foo"));
        assertEquals(42,obj.getInt("baz"));
    }

    @Test
    public void testNestedObjects() throws Exception{
        String str="{\"foo\":\"bar\",\"baz\":{\"test\":9}";
        JSONObject obj=new JSONObject(str);
        obj=obj.getJSONObject("baz");
        assertNotNull(obj);
        assertEquals(9,obj.getInt("test"));
    }
}