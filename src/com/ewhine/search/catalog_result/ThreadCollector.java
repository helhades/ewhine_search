package com.ewhine.search.catalog_result;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopFieldCollector;

import cn.gov.cbrc.wh.log.Log;
import cn.gov.cbrc.wh.log.LogFactory;

import com.ewhine.search.DocumentComparatorSource;
import com.ewhine.search.SearchHitItem;

public class ThreadCollector extends Collector {
	final private static Log log = LogFactory.getLog(ThreadCollector.class);

	private TopFieldCollector collector;
	private long[] thread_ids = null;
	private long[] o_ids = null;
	private int[] type_ids = null;
	private int total = 0;
	private HashMap<Long,ArrayList<SearchHitItem>> collected_thread_messages = new HashMap<Long,ArrayList<SearchHitItem>>();

	public ThreadCollector(int keep_size) {
		Sort sort = new Sort(new SortField("updated_at",
				new DocumentComparatorSource()));

		try {
			collector = TopFieldCollector.create(sort, keep_size, false, true, false,
					false);
		} catch (IOException e) {
			log.error("create top field error.", e);
		}

	}

	@Override
	public boolean acceptsDocsOutOfOrder() {
		return true;
	}

	@Override
	public void collect(int docID) throws IOException {
		
		long message_thread_id = this.thread_ids[docID];
		int type_id = this.type_ids[docID];
		long o_id = this.o_ids[docID];
		

		if (!collected_thread_messages.containsKey(message_thread_id)) {
			ArrayList<SearchHitItem> messages = new ArrayList<SearchHitItem>();
			SearchHitItem shi = new SearchHitItem();
			shi.setObject_id(o_id);
			shi.setObject_type(type_id);
			messages.add(shi);
			collected_thread_messages.put(message_thread_id,messages);
			total++;
		} else {
			ArrayList<SearchHitItem> messages = collected_thread_messages.get(message_thread_id);
			SearchHitItem shi = new SearchHitItem();
			shi.setObject_id(o_id);
			shi.setObject_type(type_id);
			messages.add(shi);
		}
		//System.out.println("-------->o_id:" + o_id + ",type_id:"+ type_id+ ",message_thread_id:" + message_thread_id);
		collector.collect(docID);

	}
	
	public int getConverstaionsHitCount() {
		return total;
	}
	
	public ArrayList<SearchHitItem> getHitItemByThreadId(long thread_id) {	
		return collected_thread_messages.remove(thread_id);
	}
	
	public TopDocs topDocs() {
		return collector.topDocs();
	}

	@Override
	public void setNextReader(IndexReader reader, int docBase)
			throws IOException {
		this.type_ids = FieldCache.DEFAULT.getInts(reader, "type");
		this.o_ids = FieldCache.DEFAULT.getLongs(reader, "o_id");
		this.thread_ids = FieldCache.DEFAULT.getLongs(reader, "thread_id");
		collector.setNextReader(reader, docBase);

	}

	@Override
	public void setScorer(Scorer scorer) throws IOException {
		collector.setScorer(scorer);
	}

}
