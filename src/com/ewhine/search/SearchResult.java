package com.ewhine.search;

import java.util.ArrayList;

import com.google.gson.annotations.SerializedName;

public class SearchResult {

	@SerializedName("time")
	private long time;
	@SerializedName("document_numbers")
	private int doc_numbers;
	@SerializedName("total_hits")
	private int totalHits;
	@SerializedName("hit_items")
	private ArrayList<SearchHitItem> hitItems;

	public void setTime(long l_time) {
		this.time = l_time;
	}

	public void setTotalDocs(int numDocs) {
		this.doc_numbers = numDocs;
	}

	public void setHitItems(ArrayList<SearchHitItem> hitList) {
		this.hitItems = hitList;
	}

	public long getTime() {
		return time;
	}

	public int getTotalDocs() {
		return doc_numbers;
	}

	public ArrayList<SearchHitItem> getHitItems() {
		return hitItems;
	}

	public void setTotalHits(int totalHits) {
		this.totalHits = totalHits;
	}

	public int getTotalHits() {
		return totalHits;
	}

}
