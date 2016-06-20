package me.doubledutch.stroom.query.sql;

import java.io.File;
import org.junit.*;
import static org.junit.Assert.*;
import org.json.JSONObject;
import java.util.List;
import java.util.ArrayList;
import java.net.*;

public class TokenizerTypeTest{
	@Test
    public void testReservedWord() throws Exception{
        Tokenizer tkn=new Tokenizer("SELECT");
        ArrayList<Token> list=tkn.tokenize();
        assertNotNull(list);
        assertEquals(list.size(),1);
        Token t=list.get(0);
        assertEquals(t.type,Token.RESERVED_WORD);
        assertEquals(t.data,"SELECT");
    }

    @Test
    public void testIdentifier() throws Exception{
        Tokenizer tkn=new Tokenizer("foo");
        ArrayList<Token> list=tkn.tokenize();
        assertNotNull(list);
        assertEquals(list.size(),1);
        Token t=list.get(0);
        assertEquals(t.type,Token.IDENTIFIER);
        assertEquals(t.data,"foo");
    }

    @Test
    public void testSymbol() throws Exception{
        Tokenizer tkn=new Tokenizer("*");
        ArrayList<Token> list=tkn.tokenize();
        assertNotNull(list);
        assertEquals(list.size(),1);
        Token t=list.get(0);
        assertEquals(t.type,Token.SYMBOL);
        assertEquals(t.data,"*");
    }

    @Test
    public void testString() throws Exception{
        Tokenizer tkn=new Tokenizer("'Hello World!'");
        ArrayList<Token> list=tkn.tokenize();
        assertNotNull(list);
        assertEquals(list.size(),1);
        Token t=list.get(0);
        assertEquals(t.type,Token.STRING);
        assertEquals(t.data,"Hello World!");
    }

    @Test
    public void testInteger() throws Exception{
        Tokenizer tkn=new Tokenizer("1234");
        ArrayList<Token> list=tkn.tokenize();
        assertNotNull(list);
        assertEquals(list.size(),1);
        Token t=list.get(0);
        assertEquals(t.type,Token.INTEGER);
        assertEquals(t.data,"1234");
    }

    @Test
    public void testFloat() throws Exception{
        Tokenizer tkn=new Tokenizer("9.7");
        ArrayList<Token> list=tkn.tokenize();
        assertNotNull(list);
        assertEquals(list.size(),1);
        Token t=list.get(0);
        assertEquals(t.type,Token.FLOAT);
        assertEquals(t.data,"9.7");
    }

    @Test
    public void testComment() throws Exception{
        Tokenizer tkn=new Tokenizer("# all the comments");
        ArrayList<Token> list=tkn.tokenize();
        assertNotNull(list);
        assertEquals(list.size(),1);
        Token t=list.get(0);
        assertEquals(t.type,Token.COMMENT);
        assertEquals(t.data," all the comments");
    }
}