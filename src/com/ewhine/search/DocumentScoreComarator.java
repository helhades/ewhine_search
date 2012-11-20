package com.ewhine.search;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.FieldComparator;

public class DocumentScoreComarator extends FieldComparator {
	
	
	

	private String fieldName;
	private float[] values;
	private int[] docUpdated;

	public DocumentScoreComarator(String fieldName, int numHits) {
		this.values = new float[numHits];
		this.fieldName = fieldName;
	}

	@Override
	public int compare(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int compareBottom(int arg0) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void copy(int arg0, int arg1) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setBottom(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setNextReader(IndexReader reader, int docBase) throws IOException {
		docUpdated = FieldCache.DEFAULT.getInts(reader, "updated_at");

	}

	@Override
	public Object value(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
