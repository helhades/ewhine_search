package com.ewhine.search;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import proj.zoie.api.IndexReaderFactory;
import proj.zoie.api.ZoieException;
import proj.zoie.api.ZoieIndexReader;

public class EwhineZoieSearchService {

	private static final Logger log = Logger
			.getLogger(EwhineZoieSearchService.class);

	private IndexReaderFactory<ZoieIndexReader<IndexReader>> _idxReaderFactory;

	public EwhineZoieSearchService(
			IndexReaderFactory<ZoieIndexReader<IndexReader>> idxReaderFactory) {
		_idxReaderFactory = idxReaderFactory;
	}

	public ISearchModel createSearchMode(String mode) {

		if (log.isDebugEnabled()) {
			log.debug("Create search model by parameter:" + mode);
		}

		int smode = 0;
		if (mode != null) {
			try {
				smode = Integer.parseInt(mode);
			} catch (Exception e) {
				log.error("Input mode is not valid,mode:" + mode);
			}
		}

		if (smode == 0) {
			return new NCatalogFetchSearch(_idxReaderFactory);
		} else if (smode == 1) {
			return new TopNFetchSearch(_idxReaderFactory);
		}
		return new NCatalogFetchSearch(_idxReaderFactory);

	}
	public static void main(String[] args) {
		ZoieIndexService indexer = new ZoieIndexService();
		indexer.start();
		EwhineIndexReaderFactory factory = new EwhineIndexReaderFactory(indexer);
		EwhineZoieSearchService service = new EwhineZoieSearchService(factory);
		ISearchModel sm = service.createSearchMode("1");
		ISearchResult result = null;
		try {
			result = sm.search(1, "card", null);
		} catch (ZoieException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Gson gson = new GsonBuilder().setFieldNamingPolicy(
				FieldNamingPolicy.LOWER_CASE_WITH_DASHES).create();
		System.out.println("json:" + gson.toJson(result));
		indexer.stop();
	}
}