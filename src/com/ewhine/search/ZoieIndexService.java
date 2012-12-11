package com.ewhine.search;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.lucene.index.IndexReader;
import org.wltea.analyzer.lucene.IKAnalyzer;

import proj.zoie.api.DataConsumer.DataEvent;
import proj.zoie.api.ZoieException;
import proj.zoie.api.ZoieIndexReader;
import proj.zoie.impl.indexing.ZoieConfig;
import proj.zoie.impl.indexing.ZoieSystem;

import cn.gov.cbrc.wh.log.Log;
import cn.gov.cbrc.wh.log.LogFactory;

import com.ewhine.redis.DocumentMessage;

public class ZoieIndexService {

	final private static Log log = LogFactory.getLog(ZoieIndexService.class);

	ZoieSystem<IndexReader, DataDocument> zoie = null;

	public ZoieIndexService() {

	}

	public List<ZoieIndexReader<IndexReader>> getIndexReaders() {
		try {
			return zoie.getIndexReaders();
		} catch (IOException e) {
			if (log.isErrorEnabled()) {
				log.error("Get index readers error.", e);
			}
		}

		return null;

	}

	public ZoieSystem<IndexReader, DataDocument> getSystem() {
		return zoie;
	}

	public void start() {
		
		String index_dir = System.getProperties().getProperty("indexs.dir","indexs");

		File idxDir = new File(index_dir);
		if (!idxDir.exists()) {
			log.error("Fatal find index directory:" + idxDir.getAbsolutePath());
		}
		
		if (log.isInfoEnabled()) {
			log.info("Starting index server,using directory:" + idxDir.getAbsolutePath());
		}

		ZoieConfig zConfig = new ZoieConfig();
		IKAnalyzer analyzer = new IKAnalyzer();
		zConfig.setAnalyzer(analyzer);

		zoie = ZoieSystem.buildDefaultInstance(idxDir, // index direcotry
				new DataDocumentInterpreter(), zConfig); // true for realtime

		zoie.start();

	}

	public void stop() {

		if (zoie != null) {
			zoie.stop();
		}

	}

	public void indexDocument(DocumentMessage docMessage) {
		Collection<DataEvent<DataDocument>> eventList = new ArrayList<DataEvent<DataDocument>>(
				1);

		DataEvent<DataDocument> de = new DataEvent<DataDocument>(
				new DataDocument(docMessage), "1");
		eventList.add(de);

		try {
			zoie.consume(eventList);
			zoie.flushEvents(10000);
		} catch (ZoieException e) {
			if (log.isErrorEnabled()) {
				log.error("Index document message:" + docMessage + "error.", e);
			}
		}
		if (log.isInfoEnabled()) {
			log.info("Document Package:" + docMessage + " indexed!");
		}

	}

	public void indexDocument(ArrayList<DocumentMessage> docPackages) {
		Collection<DataEvent<DataDocument>> eventList = new ArrayList<DataEvent<DataDocument>>(
				100);
		for (DocumentMessage docPackage : docPackages) {

			DataEvent<DataDocument> de = new DataEvent<DataDocument>(
					new DataDocument(docPackage), "1");
			eventList.add(de);

		}

		try {
			zoie.consume(eventList);
			zoie.flushEvents(10000);
		} catch (ZoieException e) {
			if (log.isErrorEnabled()) {
				log.error("Index document messages error.", e);
			}
		}

	}

	public void returnIndexReaders(List<ZoieIndexReader<IndexReader>> readerList) {

		if (zoie != null) {
			zoie.returnIndexReaders(readerList);
		}
	}

}
