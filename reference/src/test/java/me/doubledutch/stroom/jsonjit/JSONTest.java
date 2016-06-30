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
    public void testDeepNestedObjects() throws Exception{
        String str="{\"foo\":\"bar\",\"baz\":{\"test\":9,\"test2\":{\"id\":100},\"second\":33}}";
        JSONObject obj=new JSONObject(str);
        obj=obj.getJSONObject("baz");
        assertNotNull(obj);
        assertEquals(9,obj.getInt("test"));
        obj=obj.getJSONObject("test2");
        assertNotNull(obj);
        assertEquals(100,obj.getInt("id"));
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
       //  System.out.println('Running array test');
        String str="[\"foo\",\"bar\",42]";
        JSONArray array=new JSONArray(str);
        //System.out.println(array.toString(0));
        assertEquals("foo",array.getString(0));
        assertEquals(42,array.getInt(2));
    }


    @Test
    public void testJSONOrgSample1() throws Exception{
        String str="{\n    \"glossary\": {\n        \"title\": \"example glossary\",\n        \"GlossDiv\": {\n            \"title\": \"S\",\n            \"GlossList\": {\n                \"GlossEntry\": {\n                    \"ID\": \"SGML\",\n                    \"SortAs\": \"SGML\",\n                    \"GlossTerm\": \"Standard Generalized Markup Language\",\n                    \"Acronym\": \"SGML\",\n                    \"Abbrev\": \"ISO 8879:1986\",\n                    \"GlossDef\": {\n                        \"para\": \"A meta-markup language, used to create markup languages such as DocBook.\",\n                        \"GlossSeeAlso\": [\"GML\", \"XML\"]\n                    },\n                    \"GlossSee\": \"markup\"\n                }\n            }\n        }\n    }}";
        JSONObject obj=new JSONObject(str);
        JSONObject glo=obj.getJSONObject("glossary");
        assertNotNull(glo);
        assertEquals("example glossary",glo.getString("title"));
    }

    @Test
    public void testJSONOrgSample2() throws Exception{
        String str="{\"menu\": {\n  \"id\": \"file\",\n  \"value\": \"File\",\n  \"popup\": {\n    \"menuitem\": [\n      {\"value\": \"New\", \"onclick\": \"CreateNewDoc()\"},      {\"value\": \"Open\", \"onclick\": \"OpenDoc()\"},\n      {\"value\": \"Close\", \"onclick\": \"CloseDoc()\"}\n    ]\n  }\n}}";
        JSONObject obj=new JSONObject(str);
        JSONObject m=obj.getJSONObject("menu");
        assertNotNull(m);
        assertEquals("file",m.getString("id"));
        m=m.getJSONObject("popup");
        assertNotNull(m);
        JSONArray a=m.getJSONArray("menuitem");
        assertNotNull(a);
        JSONObject o=a.getJSONObject(1);
        assertNotNull(o);
        assertEquals("Open",o.getString("value"));
    }
    @Test
    public void testNickSample() throws Exception{
        String str="[{\"foo\":[{}],\"[]\":\"{}\"}]";
        JSONArray input=new JSONArray(str);
        System.out.println(input.toString(0));
        JSONObject obj=input.getJSONObject(0);
        assertNotNull(obj);
        JSONArray arr=obj.getJSONArray("foo");
        assertNotNull(arr);
        JSONObject obj2=arr.getJSONObject(0);
        assertNotNull(obj2);
        assertEquals(obj.getString("[]"),"{}");
    }

    @Test
    public void testComplexObject() throws Exception{
        String str=createComplexObject();
        JSONObject obj=new JSONObject(str);
        // System.out.println(obj.toString(0));
        JSONObject record=obj.getJSONObject("Record");
        assertNotNull(record);
        // System.out.println(record.toString());
        assertEquals("rating",obj.getString("Type"));
        JSONObject user=record.getJSONObject("User");
        assertNotNull(user);

        // System.out.println(user.toString());
        assertEquals("Ben",user.getString("First"));
        assertEquals("DoubleDutch",user.getString("Company"));
    }


    private String createComplexObject() throws Exception{
        org.json.JSONObject obj=new org.json.JSONObject();
        obj.put("Type","rating");
        obj.put("EventID","174482bc-8223-4207-8ccd-d87b84ac6a78");
        org.json.JSONObject record=new org.json.JSONObject();
        record.put("ID",(int)(Math.random()*10000000));
        org.json.JSONObject user=new org.json.JSONObject();
        user.put("ID","8dee6448-a37c-403f-a904-2e0d43c4cd29");
        user.put("First","Ben");
        user.put("Last","Williams");
        user.put("Email","foo@foo.bar.baz");
        user.put("Title","Chief Blame Officer");
        user.put("Company","DoubleDutch");
        record.put("User",user);
        org.json.JSONObject item=new org.json.JSONObject();
        item.put("ID",(int)(Math.random()*10000000));
        item.put("Name","Foo Bar Baz");
        item.put("List","Agenda");
        item.put("LocalStartTime","2016-06-21 13:15:00");
        item.put("LocalEndTime","2016-06-21 13:30:00");
        item.put("StartTime","2016-06-21 12:15:00");
        item.put("EndTime","2016-06-21 12:30:00");
        record.put("Item",item);
        record.put("Type","");
        record.put("Rating",5);
        record.put("Review","");
        record.put("Created","2016-06-23T23:17:48.009612Z");
        obj.put("Record",record);
        
        return obj.toString();
    }
}