package me.doubledutch.stroom.perf;

import org.json.*;

public class Counter extends Metrics{
	private static final int BUCKET_COUNT=60;

	private long[] buckets=new long[BUCKET_COUNT];

	private int currentBucket=0;

	public void inc(){
		buckets[currentBucket]++;
	}

	public void inc(long value){
		buckets[currentBucket]+=value;
	}	

	public void cycle(){
		if(currentBucket<BUCKET_COUNT-1){
			buckets[currentBucket+1]=0;
			currentBucket++;
		}else{
			buckets[0]=0;
			currentBucket=0;
		}
	}

	public JSONObject toJSON() throws JSONException{
		JSONObject obj=new JSONObject();
		return obj;
	}
}