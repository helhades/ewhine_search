package com.ewhine.search;

import com.google.gson.annotations.SerializedName;

public class SearchHitItem {
	
	@SerializedName("score")
	float score = 0;
	@SerializedName("id")
	private long object_id;
	@SerializedName("type_id")
	private int object_type;
	@SerializedName("content")
	private String highlightcontent;
		
	public SearchHitItem() {
	}

	public long getObject_id() {
		return object_id;
	}
	
	public int getObject_type() {
		return object_type;
	}

	public void setScore(float score) {
		this.score = score;
	}
	
	public float getScore() {
		return score;
	}

	public void setHighlightContent(String content) {
		this.highlightcontent = content;
	}
	
	public String getHighlightContent() {
		return this.highlightcontent;
	}
	
}
