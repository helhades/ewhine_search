package com.ewhine.search;

import org.apache.lucene.document.Document;

import proj.zoie.api.indexing.ZoieIndexable;

import com.ewhine.redis.DocumentMessage;

public class DataDocument implements ZoieIndexable {
	private long uid;
	private Document doc;
	private boolean valid;

	public DataDocument(DocumentMessage qm) {

		int type = qm.getData_type() & 0xF;
		this.uid = qm.getObject_id() << 4 | type;

		this.doc = qm.map();
		this.valid = true;
	}

	//when delete a document.
	public DataDocument(long uid) {
		this.uid = uid;
		this.valid = false;
	}

	@Override
	public long getUID() {
		return uid;
	}

	@Override
	public boolean isDeleted() {
		return !valid;
	}

	@Override
	public boolean isSkip() {
		return false;
	}

	@Override
	public IndexingReq[] buildIndexingReqs() {
		return new IndexingReq[] { new IndexingReq(doc) };
	}

	@Override
	public boolean isStorable() {
		return false;
	}

	@Override
	public byte[] getStoreValue() {
		return null;
	}

}
