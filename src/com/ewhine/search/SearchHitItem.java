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
	@SerializedName("created_at")
	private int created_at;
	@SerializedName("updated_at")
	private int updated_at;
	@SerializedName("thread_id")
	private long thread_id;

	public SearchHitItem() {
	}

	public long getObject_id() {
		return object_id;
	}

	public void setObject_id(long object_id) {
		this.object_id = object_id;
	}

	public void setObject_type(int object_type) {
		this.object_type = object_type;
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

	public void setCreated_at(int created_at) {
		this.created_at = created_at;
	}

	public int getCreated_at() {
		return created_at;
	}

	public void setUpdated_at(int updated_at) {
		this.updated_at = updated_at;
	}

	public int getUpdated_at() {
		return updated_at;
	}

	public void setThread_id(long t_id) {
		this.thread_id = t_id;
	}

}
