package com.ewhine.search;

import java.util.ArrayList;

public class SearchResult {

	private long time;
	private int doc_numbers;
	private ArrayList<SearchHitItem> hitItems;
	private int totalHits;

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
