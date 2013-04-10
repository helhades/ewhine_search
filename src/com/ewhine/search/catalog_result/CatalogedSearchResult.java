package com.ewhine.search.catalog_result;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.velocity.VelocityContext;

import com.ewhine.search.SearchHitItem;
import com.ewhine.search.faceted.ISearchResult;
import com.google.gson.annotations.SerializedName;

public class CatalogedSearchResult implements ISearchResult {

	@SerializedName("document_numbers")
	private int doc_numbers;
	@SerializedName("query_terms")
	private ArrayList<String> query_terms = new ArrayList<String>();
	@SerializedName("type_ids")
	private ArrayList<Integer> type_ids = new ArrayList<Integer>();
	
	@SerializedName("hit_items")
	private HashMap<Integer,List<SearchHitItem>> hitItems = new HashMap<Integer,List<SearchHitItem>>();
	
	@SerializedName("totalhit_numbers")
	private HashMap<Integer,Integer> hit_numbers = new HashMap<Integer,Integer>();


	public void setTotalDocs(int numDocs) {
		this.doc_numbers = numDocs;
	}
	
	public int getTotal_hit() {
		int total_hit = 0;
		for (Integer num : hit_numbers.values()) {
			total_hit = total_hit + num;
		}
		return total_hit;
	}

	public void setHitItems(int type_id,ArrayList<SearchHitItem> hitList) {
		this.type_ids.add(type_id);
		this.hitItems.put(type_id, hitList);
		
	}
	
	public void setHit_numbers(int type_id, int hit_number) {
		this.hit_numbers.put(type_id, hit_number);
	}

	/* (non-Javadoc)
	 * @see com.ewhine.search.ISearchResult#getTotalDocs()
	 */
	@Override
	public int getTotalDocs() {
		return doc_numbers;
	}
	
	public void setQuery_term(String query_term) {
		this.query_terms.add(query_term);
	}
	
	/* (non-Javadoc)
	 * @see com.ewhine.search.ISearchResult#getQuery_terms()
	 */
	@Override
	public List<String> getQuery_terms() {
		return query_terms;
	}

	public List<SearchHitItem> getHitItems(int type_id) {
		return hitItems.get(type_id);
	}

	@Override
	public void addToContext(VelocityContext context) throws IOException {
		// TODO Auto-generated method stub
		
	}

	

}
