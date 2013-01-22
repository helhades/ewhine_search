package com.ewhine.search;

import java.io.IOException;
import java.util.Arrays;
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

public class TopNSearchCollector extends Collector {
	final private static Log log = LogFactory.getLog(TopNSearchCollector.class);

	private long[] user_group_ids;
	private long[] group_ids;
	private TopFieldCollector collector;

	private long[] user_conversation_ids;

	public TopNSearchCollector(List<Group> u_groups,
			List<Group> conversation_group) {
		Sort sort = new Sort(new SortField("updated_at",
				new DocumentComparatorSource()));

		try {
			collector = TopFieldCollector.create(sort, 10, false, true, false,
					false);
		} catch (IOException e) {
			log.error("create top field error.", e);
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

		if ((g_id > 0 && Arrays.binarySearch(user_group_ids, g_id) >= 0)
				|| (g_id < 0 && Arrays.binarySearch(user_conversation_ids,
						(-g_id)) >= 0)) {
			collector.collect(docID);
		}

	}

	public TopDocs topDocs() {
		return collector.topDocs();
	}

	@Override
	public void setNextReader(IndexReader reader, int docBase)
			throws IOException {
		this.group_ids = FieldCache.DEFAULT.getLongs(reader, "g_id");
		collector.setNextReader(reader, docBase);

	}

	@Override
	public void setScorer(Scorer scorer) throws IOException {
		collector.setScorer(scorer);
	}

}
