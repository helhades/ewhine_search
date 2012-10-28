package com.ewhine.search;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Scorer;

public class EwhineSearchCollector extends Collector {


	private Scorer scorer;

	@Override
	public boolean acceptsDocsOutOfOrder() {
		return true;
	}

	@Override
	public void collect(int docID) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setNextReader(IndexReader reader, int docBase) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setScorer(Scorer scorer) throws IOException {
		this.scorer = scorer;
	}

}
