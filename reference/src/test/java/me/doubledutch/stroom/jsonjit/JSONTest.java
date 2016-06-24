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
        String str="{\"foo\":\"bar\",\"baz\":{\"test\":9}}";
        JSONObject obj=new JSONObject(str);
        obj=obj.getJSONObject("baz");
        assertNotNull(obj);
        assertEquals(9,obj.getInt("test"));
    }

    @Test
    public void testArrayObjects() throws Exception{
        String str="[{\"foo\":\"bar\",\"baz\":{\"test\":9}}]";
        JSONArray array=new JSONArray(str);
        JSONObject obj=array.getJSONObject(0);
        obj=obj.getJSONObject("baz");
        assertNotNull(obj);
        assertEquals(9,obj.getInt("test"));

        str="[{\"i\":1024,\"b\":2048,\"p\":\"XXXXXXXXXXXXXXXXXXXX\"},{\"i\":2,\"b\":3,\"p\":\"XXXXXXXXXXXXXXXXXXXX\"},{\"i\":1024,\"b\":2048,\"p\":\"XXXXXXXXXXXXXXXXXXXX\"}]";
        array=new JSONArray(str);
        obj=array.getJSONObject(1);
        assertNotNull(obj);
        assertEquals(3,obj.getInt("b"));
    }

    @Test
    public void testArrayValues() throws Exception{
        String str="[\"foo\",\"bar\",42]";
        JSONArray array=new JSONArray(str);
        assertEquals("foo",array.getString(0));
        assertEquals(42,array.getInt(2));
    }
}