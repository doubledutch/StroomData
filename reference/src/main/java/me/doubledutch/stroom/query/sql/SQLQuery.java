package me.doubledutch.stroom.query.sql;

import me.doubledutch.stroom.query.*;
import java.util.*;

public class SQLQuery{
	public static final int SELECT=0;
	public static final int INSERT=1;
	public static final int UPDATE=2;
	public static final int DELETE=3;

	public int type=SELECT; // Only supported option at this time
	public boolean selectAll=false;
	public List<DerivedColumn> selectList=null;
	public List<TableReference> tableList=null;
	public Expression where=null;

	public String toString(){
		StringBuilder buf=new StringBuilder();
		if(type==SELECT){
			buf.append("SELECT ");
			if(selectAll){
				buf.append("*");
			}else{
				buf.append(selectList.get(0).toString());
				for(int i=1;i<selectList.size();i++){
					buf.append(",");
					buf.append(selectList.get(i).toString());
				}
			}
			buf.append(" FROM ");
			for(int i=0;i<tableList.size();i++){
				if(i>0)buf.append(",");
				buf.append(tableList.get(i).toString());
			}
			return buf.toString();
		}
		return "";
	}
}