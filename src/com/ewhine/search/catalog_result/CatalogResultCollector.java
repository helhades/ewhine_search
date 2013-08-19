package com.ewhine.search.catalog_result;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

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

import com.ewhine.model.Group;
import com.ewhine.model.ObjectType;
import com.ewhine.search.DocumentComparatorSource;

public class CatalogResultCollector extends Collector {
	final private static Log log = LogFactory
			.getLog(CatalogResultCollector.class);

	private long[] user_group_ids;
	private long[] user_conversation_ids;

	/**
	 * doc cache
	 */
	private long[] group_ids;
	private int[] type_ids;
	private long[] thread_ids;
	private HashMap<Integer,Collector> collectors = new HashMap<Integer,Collector>();
	//private Collector[] collectors = new Collector[20]; // max
																// type
																// size.

	private HashSet<Long> merged_thread_ids = new HashSet<Long>();

	// private long[] o_ids;

	public CatalogResultCollector(List<Group> u_groups,
			List<Group> conversation_group, int[] type_ids, int keep_size) {

		for (int type_id : type_ids) {
			// System.out.println("add type_id:" + type_id);

			Sort sort = new Sort(new SortField("updated_at",
					new DocumentComparatorSource()));

			try {
				if (type_id == ObjectType.MESSAGE) {
					ThreadCollector t_collector = new ThreadCollector(keep_size);
					collectors.put(type_id,t_collector);
				} else {
					TopFieldCollector t_collector = TopFieldCollector.create(sort,
							keep_size, false, true, false, false);
					collectors.put(type_id,t_collector);
				}

			} catch (IOException e) {
				log.error("create top field error.", e);
			}
		}

		user_group_ids = new long[u_groups.size()];
		for (int i = 0, n = u_groups.size(); i < n; i++) {
			user_group_ids[i] = u_groups.get(i).getId();
		}
		Arrays.sort(user_group_ids);

		user_conversation_ids = new long[conversation_group.size()];
		for (int i = 0, n = conversation_group.size(); i < n; i++) {
			user_conversation_ids[i] = conversation_group.get(i).getId();
		}
		Arrays.sort(user_conversation_ids);

	}

	@Override
	public boolean acceptsDocsOutOfOrder() {
		return true;
	}

	@Override
	public void collect(int docID) throws IOException {

		long g_id = group_ids[docID];
		
		
		if (has_auth(g_id)) {
			
			// find a catalog type collector to collect the doc.
			//System.out.println("doc_id:" + docID + ",g_id:" + g_id + ",type_id=" + type_id);
			
			int type_id = type_ids[docID];
			Collector collector = null;

			if (type_id < ObjectType.USER) { // map the rest type to message
				collector = collectors.get(ObjectType.MESSAGE);
			} else {
				collector = collectors.get(type_id);
			}

			// increase the conservations hit count.

			if (collector != null) {
				
				if (type_id < ObjectType.USER) { // mini_app object.
					long message_thread_id = this.thread_ids[docID];

					if (!merged_thread_ids.contains(message_thread_id)) {
						merged_thread_ids.add(message_thread_id);
					}
				}

				collector.collect(docID);

			}
		}

	}

	private boolean has_auth(long g_id) {
		
		return (g_id > 0 && Arrays.binarySearch(user_group_ids, g_id) >= 0)
				|| (g_id < 0 && Arrays.binarySearch(user_conversation_ids,
						(-g_id)) >= 0);
	}

	public HashMap<Integer, TopDocs> topDocs() {
		HashMap<Integer, TopDocs> tops = new HashMap<Integer, TopDocs>();
		for (int key : collectors.keySet()) {
			if (collectors.get(key) != null) {
				if (key == ObjectType.MESSAGE) {
					tops.put(key, ((ThreadCollector) collectors.get(key)).topDocs());
				} else {
					tops.put(key, ((TopFieldCollector) collectors.get(key)).topDocs());
				}
			}
		}	
		return tops;
	}
	
	public Collector getCollector(int type_id) {
		return collectors.get(type_id);
	}

	public int getConverstaionsHitCount() {
		return this.merged_thread_ids.size();
	}

	@Override
	public void setNextReader(IndexReader reader, int docBase)
			throws IOException {
		this.group_ids = FieldCache.DEFAULT.getLongs(reader, "g_id");
		this.type_ids = FieldCache.DEFAULT.getInts(reader, "type");
		this.thread_ids = FieldCache.DEFAULT.getLongs(reader, "thread_id");

		for (Collector collector : collectors.values()) {
			if (collector != null) {
				collector.setNextReader(reader, docBase);
			}
		}

	}

	@Override
	public void setScorer(Scorer scorer) throws IOException {

		for (Collector collector : collectors.values()) {
			if (collector != null) {
				collector.setScorer(scorer);
			}
		}
	}

}
