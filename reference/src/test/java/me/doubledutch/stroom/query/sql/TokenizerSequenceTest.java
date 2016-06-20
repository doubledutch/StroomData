package me.doubledutch.stroom.query.sql;

import java.io.File;
import org.junit.*;
import static org.junit.Assert.*;
import org.json.JSONObject;
import java.util.List;
import java.util.ArrayList;
import java.net.*;

public class TokenizerSequenceTest{
    private boolean tokensMatch(List<Token> l1,Token[] l2){
        if(l1.size()!=l2.length){
            return false;
        }
        for(int i=0;i<l1.size();i++){
            Token t1=l1.get(i);
            Token t2=l2[i];
            if(t1.type!=t2.type){
                return false;
            }
            if(!t1.data.equals(t2.data)){
                return false;
            }
        }
        return true;
    }

	@Test
    public void testSelect() throws Exception{
        Tokenizer tkn=new Tokenizer("SELECT * FROM users");
        ArrayList<Token> list=tkn.tokenize();
        assertNotNull(list);
        assertEquals(list.size(),4);
        assertTrue(tokensMatch(list,new Token[]{
            Token.wordT("SELECT"),
            Token.symbolT("*"),
            Token.wordT("FROM"),
            Token.identifierT("users")
        }));
    }

    @Test
    public void testComplexSelect() throws Exception{
        Tokenizer tkn=new Tokenizer("SELECT a,b,c.x FROM users WHERE a=9 AND b>c.y");
        ArrayList<Token> list=tkn.tokenize();
        assertNotNull(list);
        assertEquals(list.size(),20);
        assertTrue(tokensMatch(list,new Token[]{
            Token.wordT("SELECT"),
            Token.identifierT("a"),
            Token.symbolT(","),
            Token.identifierT("b"),
            Token.symbolT(","),
            Token.identifierT("c"),
            Token.symbolT("."),
            Token.identifierT("x"),
            Token.wordT("FROM"),
            Token.identifierT("users"),
            Token.wordT("WHERE"),
            Token.identifierT("a"),
            Token.symbolT("="),
            Token.integerT("9"),
            Token.wordT("AND"),
            Token.identifierT("b"),
            Token.symbolT(">"),
            Token.identifierT("c"),
            Token.symbolT("."),
            Token.identifierT("y")
        }));
    }
}