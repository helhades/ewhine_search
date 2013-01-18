package com.ewhine.search;

import com.ewhine.model.ObjectType;
import com.google.gson.annotations.SerializedName;

public class SearchHitItem {

	@SerializedName("score")
	float score = 0;
	@SerializedName("doc_id")
	private int doc_id;
	@SerializedName("id")
	private long object_id;
	@SerializedName("type_id")
	private int object_type;
	@SerializedName("content")
	private String highlightcontent;
	@SerializedName("name")
	private String name = null;
	@SerializedName("created_at")
	private String created_at;
	@SerializedName("updated_at")
	private String updated_at;
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

	public String getObject_type() {
		if (ObjectType.MESSAGE ==object_type) {
			return "MESSAGE";
		}
		if (ObjectType.GROUP ==object_type) {
			return "GROUP";
		}
		if (ObjectType.USER ==object_type) {
			return "USER";
		}
		if (ObjectType.TAG ==object_type) {
			return "TAG";
		}
		if (ObjectType.ATTACHMENT_FILE ==object_type) {
			return "ATTACHMENT_FILE";
		}
		if (ObjectType.EVENT ==object_type) {
			return "EVENT";
		}
		if (ObjectType.MINI_TASK ==object_type) {
			return "MINI_TASK";
		}
		if (ObjectType.QUESTION ==object_type) {
			return "QUESTION";
		}
		if (ObjectType.POLL ==object_type) {
			return "POLL";
		}
		if (ObjectType.PRIASE ==object_type) {
			return "PRIASE";
		}
		if (ObjectType.ANNOUNCEMENT ==object_type) {
			return "ANNOUNCEMENT";
		}
		return "Unkwon";
	}

	public void setDoc_id(int doc_id) {
		this.doc_id = doc_id;
	}

	public int getDoc_id() {
		return doc_id;
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

	public void setCreated_at(String created_at) {
		this.created_at = created_at;
	}

	public String getCreated_at() {
		return created_at;
	}

	public void setUpdated_at(String updated_at) {
		this.updated_at = updated_at;
	}

	public String getUpdated_at() {
		return updated_at;
	}

	public void setThread_id(long t_id) {
		this.thread_id = t_id;
	}

	public void setName(String highlightName) {
		this.name = highlightName;

	}
	
	public String getName() {
		return name;
	}

}
