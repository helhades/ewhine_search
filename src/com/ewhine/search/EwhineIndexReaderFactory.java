package com.ewhine.search;

import java.io.IOException;
import java.util.List;

import org.ansj.lucene3.AnsjAnalysis;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;

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
		AnsjAnalysis analyzer = new AnsjAnalysis(false);
		//StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_35);
		return analyzer;
	}

	@Override
	public void returnIndexReaders(List<ZoieIndexReader<IndexReader>> r) {
		// TODO Auto-generated method stub
		indexService.returnIndexReaders(r);
	}

	@Override
	public String getCurrentReaderVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	

}
