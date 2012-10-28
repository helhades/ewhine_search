package com.ewhine.search;

public class SearchHitItem {
	
	String content = null;
	float score = 0;
	private long object_id;
	private int object_type;
	private String highlightcontent;
	
	public SearchHitItem(float _score,long object_id,int object_type,String _content) {
		
		this.score = _score;
		this.content = _content;
		this.object_id = object_id;
		this.object_type = object_type;
		
	}
	
	
	
	public SearchHitItem() {
		// TODO Auto-generated constructor stub
	}



	public String getContent() {
		return content;
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
