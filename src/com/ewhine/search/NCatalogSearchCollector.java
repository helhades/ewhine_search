package com.ewhine.search;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
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

public class NCatalogSearchCollector extends Collector {
	final private static Log log = LogFactory
			.getLog(NCatalogSearchCollector.class);

	private long[] user_group_ids;
	private long[] group_ids;
	private TopFieldCollector[] collectors = new TopFieldCollector[20]; // max
																		// type
																		// size.

	private long[] user_conversation_ids;

	private int[] type_ids;

	public NCatalogSearchCollector(List<Group> u_groups,
			List<Group> conversation_group, int[] type_ids) {

		for (int type_id : type_ids) {
			Sort sort = new Sort(new SortField("updated_at",
					new DocumentComparatorSource()));

			try {
				collectors[type_id] = TopFieldCollector.create(sort, 10, false,
						true, false, false);
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

	public HashMap<Integer, TopDocs> getCollectors() {

		HashMap<Integer, TopDocs> topdocs = new HashMap<Integer, TopDocs>();
		for (int i = 0, n = collectors.length; i < n; i++) {
			TopFieldCollector colletor = collectors[i];
			if (colletor != null) {
				topdocs.put(i, colletor.topDocs());
			}
		}

		return topdocs;
	}

	@Override
	public boolean acceptsDocsOutOfOrder() {
		return true;
	}

	@Override
	public void collect(int docID) throws IOException {

		long g_id = group_ids[docID];

		if ((g_id > 0 && Arrays.binarySearch(user_group_ids, g_id) >= 0)
				|| (g_id < 0 && Arrays.binarySearch(user_conversation_ids,
						(-g_id)) >= 0)) {
			// find a catalog type collector to collect the doc.
			int type_id = type_ids[docID];
			TopFieldCollector collector = collectors[type_id];
			if (collector != null) {
				collector.collect(docID);
			} else { // map the rest type to message
				collector = collectors[ObjectType.MESSAGE];
				if (collector != null) {
					collector.collect(docID);
				}
			}
		}

	}

	public HashMap<Integer, TopDocs> topDocs() {
		HashMap<Integer, TopDocs> tops = new HashMap<Integer, TopDocs>();
		for (int i = 0; i < collectors.length; i++) {
			if (collectors[i] != null) {
				tops.put(i, collectors[i].topDocs());
			}
		}
		return tops;
	}

	@Override
	public void setNextReader(IndexReader reader, int docBase)
			throws IOException {
		this.group_ids = FieldCache.DEFAULT.getLongs(reader, "g_id");
		this.type_ids = FieldCache.DEFAULT.getInts(reader, "type");
		for (TopFieldCollector collector : collectors) {
			if (collector != null) {
				collector.setNextReader(reader, docBase);
			}
		}

	}

	@Override
	public void setScorer(Scorer scorer) throws IOException {

		for (TopFieldCollector collector : collectors) {
			if (collector != null) {
				collector.setScorer(scorer);
			}
		}
	}

}
