package me.doubledutch.stroom.client;

import java.util.*;
import java.io.*;

public interface KVStoreConnection{
	public String get(String key) throws IOException;
	public List<String> list() throws IOException;
	public List<String> search(String pattern) throws IOException;
}