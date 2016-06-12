package me.doubledutch.stroom.query.sql;

public class TableReference{
	public String as=null;
	public String identifier=null;
	public String url=null;
	public SQLQuery query=null;

	public String toString(){
		StringBuilder buf=new StringBuilder();
		if(identifier!=null){
			buf.append(identifier);
		}else if(url!=null){
			buf.append("'");
			buf.append(url);
			buf.append("'");
		}else{
			buf.append("(");
			buf.append(query.toString());
			buf.append(")");
		}
		if(as!=null){
			buf.append(" AS ");
			buf.append(as);
		}
		return buf.toString();
	}
}