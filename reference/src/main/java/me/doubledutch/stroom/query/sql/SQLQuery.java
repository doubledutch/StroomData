package me.doubledutch.stroom.query.sql;

import me.doubledutch.lazyjson.*;
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
	public Expression order=null;
	public Expression partition=null;

	public void normalize(){
		if(where!=null){
			// TODO: split the clause and make this real
			tableList.get(0).condition=where;
		}
	}

	public boolean isPartitioned(){
		return partition!=null;
	}

	public String getPartitionKey(LazyObject obj) throws Exception{
		if(partition==null)return "";
		Expression value=partition.evaluate(obj);
		// TODO: this shouldn't happen - we should use null expression types instead
		if(value==null)return null;
		return value.getStringValue();
	}

	public String toString(){
		StringBuilder buf=new StringBuilder();
		if(type==SELECT){
			buf.append("SELECT ");
			if(selectAll){
				buf.append("*");
			}else{
				buf.append(selectList.get(0).toString());
				for(int i=1;i<selectList.size();i++){
					buf.append(", ");
					buf.append(selectList.get(i).toString());
				}
			}
			buf.append(" FROM ");
			for(int i=0;i<tableList.size();i++){
				if(i>0)buf.append(",");
				buf.append(tableList.get(i).toString());
			}
			if(where!=null){
				buf.append(" WHERE ");
				buf.append(where.toString());
			}
			if(partition!=null){
				buf.append(" PARTITION BY "+partition.toString());
			}
			return buf.toString();
		}
		return "";
	}
}