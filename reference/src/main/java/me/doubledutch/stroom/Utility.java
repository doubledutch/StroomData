package me.doubledutch.stroom;

import java.io.*;

public class Utility{
	/**
	 * Read the full contents of a text file
	 */
	public static String readFile(String filename) throws IOException{
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		StringBuilder buf=new StringBuilder();
		char[] data=new char[8192];
		int num=reader.read(data);
		while(num>-1){
			buf.append(data,0,num);
			num=reader.read(data);
		}
		return buf.toString();
	}
}