package me.doubledutch.stroom.jsonjit;

public class JSONArray{
	private JSONToken root;
	private String source;

	protected JSONArray(JSONToken root,String source){
		this.root=root;
		this.source=source;
	}

	public int length(){
		if(root.children==null){
			return 0;
		}
		return root.children.size();
	}

	public JSONObject getJSONObject(int index){
		if(root.children==null){
			return null;
		}
		if(root.children.size()-1<index){
			return null;
		}
		JSONToken obj=root.children.get(index);
		if(obj.type!=JSONToken.OBJECT){
			// Throw error
		}
		return new JSONObject(obj,source);
	}
}