package com.ewhine.search;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.Scorer;

import com.ewhine.model.Group;

public class EwhineSearchCollector extends Collector {


	private Scorer scorer;
	private List<Group> user_groups;
	private long[] group_ids;
	private IndexReader reader;
	private int docBase;

	public EwhineSearchCollector(List<Group> u_groups) {
		this.user_groups = u_groups;
	}

	@Override
	public boolean acceptsDocsOutOfOrder() {
		return true;
	}

	@Override
	public void collect(int docID) throws IOException {
		// TODO Auto-generated method stub
		Document document = reader.document(docID);
		
	}

	@Override
	public void setNextReader(IndexReader reader, int docBase) throws IOException {
		// TODO Auto-generated method stub
		this.group_ids = FieldCache.DEFAULT.getLongs(reader, "group_id");
		this.reader = reader;
		this.docBase = docBase;
		
	}

	@Override
	public void setScorer(Scorer scorer) throws IOException {
		this.scorer = scorer;
	}

}
