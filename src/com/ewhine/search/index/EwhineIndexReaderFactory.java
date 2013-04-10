package com.ewhine.search.index;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.wltea.analyzer.lucene.IKAnalyzer;


import proj.zoie.api.IndexReaderFactory;
import proj.zoie.api.ZoieIndexReader;

public class EwhineIndexReaderFactory implements IndexReaderFactory<ZoieIndexReader<IndexReader>> {

	private ZoieIndexService indexService;

	public EwhineIndexReaderFactory(ZoieIndexService indexer) {
		this.indexService = indexer;
	}

	@Override
	public List<ZoieIndexReader<IndexReader>> getIndexReaders() throws IOException {
		// TODO Auto-generated method stub
		return indexService.getIndexReaders();
	}

	@Override
	public Analyzer getAnalyzer() {
		IKAnalyzer analyzer = new IKAnalyzer();
		//StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_35);
		return analyzer;
	}

	@Override
	public void returnIndexReaders(List<ZoieIndexReader<IndexReader>> r) {
		
		indexService.returnIndexReaders(r);
	}

	@Override
	public String getCurrentReaderVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	

}
