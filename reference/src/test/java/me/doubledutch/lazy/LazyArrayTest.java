package me.doubledutch.lazy;

import java.io.File;
import org.junit.*;
import static org.junit.Assert.*;
// import org.json.JSONObject;
import java.util.List;
import java.util.ArrayList;
import java.net.*;

public class LazyArrayTest{
    @Test
    public void testArrayObjects() throws LazyException{
        String str="[{\"foo\":\"bar\",\"baz\":{\"test\":9}}]";
        LazyArray array=new LazyArray(str);
        LazyObject obj=array.getJSONObject(0);
        obj=obj.getJSONObject("baz");
        assertNotNull(obj);
        assertEquals(9,obj.getInt("test"));

        str="[{\"i\":1024,\"b\":2048,\"p\":\"XXXXXXXXXXXXXXXXXXXX\"},{\"i\":2,\"b\":3,\"p\":\"XXXXXXXXXXXXXXXXXXXX\"},{\"i\":1024,\"b\":2048,\"p\":\"XXXXXXXXXXXXXXXXXXXX\"}]";
        array=new LazyArray(str);
        obj=array.getJSONObject(1);
        assertNotNull(obj);
        assertEquals(3,obj.getInt("b"));
    }

    @Test
    public void testArrayValues() throws LazyException{
       //  System.out.println('Running array test');
        String str="[\"foo\",\"bar\",42]";
        LazyArray array=new LazyArray(str);
        //System.out.println(array.toString(0));
        assertEquals("foo",array.getString(0));
        assertEquals(42,array.getInt(2));
    }

    @Test
    public void testNickSample() throws LazyException{
        String str="[{\"foo\":[{}],\"[]\":\"{}\"}]";
        LazyArray input=new LazyArray(str);
        // System.out.println(input.toString(0));
        LazyObject obj=input.getJSONObject(0);
        assertNotNull(obj);
        LazyArray arr=obj.getJSONArray("foo");
        assertNotNull(arr);
        LazyObject obj2=arr.getJSONObject(0);
        assertNotNull(obj2);
        assertEquals(obj.getString("[]"),"{}");
    }
}