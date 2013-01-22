package com.ewhine.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.velocity.VelocityContext;

import com.google.gson.annotations.SerializedName;

public class SearchResult implements ISearchResult {

	@SerializedName("document_numbers")
	private int doc_numbers;
	@SerializedName("total_hits")
	private int totalHits;
	@SerializedName("query_terms")
	private ArrayList<String> query_terms = new ArrayList<String>();
	@SerializedName("hit_items")
	private ArrayList<SearchHitItem> hitItems;

	public void setTotalDocs(int numDocs) {
		this.doc_numbers = numDocs;
	}

	public void setHitItems(ArrayList<SearchHitItem> hitList) {
		this.hitItems = hitList;
	}

	public int getTotalDocs() {
		return doc_numbers;
	}

	public void setQuery_term(String query_term) {
		this.query_terms.add(query_term);
	}

	public List<String> getQuery_terms() {
		return query_terms;
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

	@Override
	public void addToContext(VelocityContext context) throws IOException {

		context.put("hits", this.getHitItems());

	}

}
