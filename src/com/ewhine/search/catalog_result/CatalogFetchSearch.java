package com.ewhine.search.catalog_result;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.NumericUtils;
import org.apache.lucene.util.Version;

import proj.zoie.api.IndexReaderFactory;
import proj.zoie.api.ZoieException;
import proj.zoie.api.ZoieIndexReader;

import com.ewhine.model.ObjectType;
import com.ewhine.model.User;
import com.ewhine.search.SearchHitItem;
import com.ewhine.search.faceted.ISearchModel;
import com.ewhine.search.faceted.ISearchResult;

public class CatalogFetchSearch implements ISearchModel {

	private static final Logger log = Logger
			.getLogger(CatalogFetchSearch.class);

	private IndexReaderFactory<ZoieIndexReader<IndexReader>> _idxReaderFactory;

	public CatalogFetchSearch(
			IndexReaderFactory<ZoieIndexReader<IndexReader>> idxReaderFactory) {
		_idxReaderFactory = idxReaderFactory;
	}

	public ISearchResult search(long user_id, String queryString,
			String type_name, int i_page_size, int i_page) throws ZoieException {

		User user = User.find_by_id(user_id);
		long network_id = user.getNetwork_id();
		// List<Group> u_groups = user.authorizedGroups();
		long query_id = System.currentTimeMillis();

		if (log.isInfoEnabled()) {
			StringBuilder sb = new StringBuilder();
			sb.append("qid:").append(query_id).append(",uid:").append(user_id)
					.append(",query:").append(queryString)
					.append(",type_name:").append(type_name)
					.append(",i_page_size:").append(i_page_size)
					.append(",i_page:").append(i_page);
			log.info(sb);
		}

		// SecurityFilter groupFilter = new SecurityFilter(u_groups);
		List<ZoieIndexReader<IndexReader>> readers = null;

		MultiReader multiReader = null;
		IndexSearcher searcher = null;

		try {

			// 1. Build query parser and query
			Query q = null;
			if (queryString == null || queryString.length() == 0) {
				q = new MatchAllDocsQuery();
			} else {

				Analyzer analyzer = _idxReaderFactory.getAnalyzer();
				QueryParser qparser = new MultiFieldQueryParser(
						Version.LUCENE_35, new String[] { "name", "keyword",
								"description", "content" }, analyzer);
				qparser.setPhraseSlop(1);
				//qparser.setDefaultOperator(QueryParser.OR_OPERATOR);

				q = qparser.parse(queryString);

				if (log.isInfoEnabled()) {
					log.info("query_id:" + query_id + ",user_id:" + user_id
							+ ",Network:" + network_id + ",q paraed:" + q);

				}

			}

			// 2. Add security limit.
			TermQuery network_limit = new TermQuery(new Term("network_id",
					NumericUtils.longToPrefixCoded(network_id)));

			BooleanQuery combine_query = new BooleanQuery();

			combine_query.add(network_limit, BooleanClause.Occur.MUST);

			if (type_name != null) {
				int type_id = -1;
				if ("users".equals(type_name)) {
					type_id = ObjectType.USER;
				} else if ("groups".equals(type_name)) {
					type_id = ObjectType.GROUP;
				} else if ("uploaded_files".equals(type_name)) {
					type_id = ObjectType.ATTACHMENT_FILE;
				} else if ("topics".equals(type_name)) {
					type_id = ObjectType.TOPIC;
				} else if ("conversations".equals(type_name)) {
					type_id = ObjectType.MESSAGE;
				}
				if (type_id == ObjectType.USER || type_id == ObjectType.GROUP
						|| type_id == ObjectType.ATTACHMENT_FILE
						|| type_id == ObjectType.TOPIC) {

					TermQuery type_limit = new TermQuery(new Term("type",
							NumericUtils.intToPrefixCoded(type_id)));
					combine_query.add(type_limit, BooleanClause.Occur.MUST);

				}
				if (type_id == ObjectType.MESSAGE) {

					TermQuery type_limit = new TermQuery(new Term("thread_id",
							NumericUtils.longToPrefixCoded(0)));
					// only message and mini_apps
					combine_query.add(type_limit, BooleanClause.Occur.MUST_NOT);

				}
			}

			combine_query.add(q, BooleanClause.Occur.MUST);

			// 3. Build search object.
			readers = _idxReaderFactory.getIndexReaders();
			multiReader = new MultiReader(
					readers.toArray(new IndexReader[readers.size()]), false);

			searcher = new IndexSearcher(multiReader);

			// set the sort.
			// Sort sort = new Sort(new SortField("updated_at",
			// new DocumentComparatorSource()));

			// start a new search.
			// TopDocs topDocs = searcher.search(combine_query, groupFilter,
			// 10, sort);

			// 4. Build custome's collector.
			int[] type_ids = new int[] { ObjectType.MESSAGE, ObjectType.GROUP,
					ObjectType.USER, ObjectType.ATTACHMENT_FILE,
					ObjectType.TOPIC };

			int page_end = i_page * i_page_size;
			int page_start = page_end - i_page_size;

			CatalogResultCollector collector = new CatalogResultCollector(
					user.authorizedGroups(), user.conversation_groups(),
					type_ids, page_end);

			// 5. Start a search.
			searcher.search(combine_query, collector);

			// Explanation exp = searcher.explain(all_combin, 1);
			// System.out.println("exp:" + exp);

			long end = System.currentTimeMillis();
			long time = query_id - end;
			if (time > 200) {
				log.warn("query_id:" + query_id + " spend:" + time + "ms");
			}

			CatalogedSearchResult result = processResult(multiReader,
					collector, page_start, page_end);

			// 6. Extract the query terms.
			HashSet<Term> out = new HashSet<Term>();
			q.extractTerms(out);
			HashSet<String> queryText = new HashSet<String>();

			for (Term term : out) {
				if (!queryText.contains(term.text())) {
					result.setQuery_term(term.text());
					queryText.add(term.text());
				}
			}

			if (log.isInfoEnabled()) {
				log.info("query_id:" + query_id + ",result: hit:"
						+ result.getTotal_hit());
			}

			return result;

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new ZoieException(e.getMessage(), e);
		} finally {
			try {
				if (searcher != null) {
					try {
						searcher.close();
					} catch (IOException e) {
						log.error(e.getMessage(), e);
					} finally {
						if (multiReader != null) {
							try {
								multiReader.close();
							} catch (IOException e) {
								log.error(e.getMessage(), e);
							}
						}
					}
				}
			} finally {
				_idxReaderFactory.returnIndexReaders(readers);
			}
		}
	}

	private CatalogedSearchResult processResult(MultiReader multiReader,
			CatalogResultCollector collector, int start_doc, int end_doc)
			throws CorruptIndexException, IOException {

		HashMap<Integer, TopDocs> topDocs = collector.topDocs();

		CatalogedSearchResult result = new CatalogedSearchResult();
		result.setTotalDocs(multiReader.numDocs());
		Set<Integer> keys = topDocs.keySet();

		for (Integer type_id : keys) {

			TopDocs topdoc = topDocs.get(type_id);

			ScoreDoc[] scoreDocs = topdoc.scoreDocs;
			ArrayList<SearchHitItem> hitItems = new ArrayList<SearchHitItem>(
					scoreDocs.length);

			int end_index = (scoreDocs.length < end_doc ? scoreDocs.length
					: end_doc);
			int start_index = (end_index < start_doc ? (scoreDocs.length - (end_doc - start_doc))
					: start_doc);

			if (start_index < 0) {
				start_index = 0;
			}

			Collector coll = collector.getCollector(type_id);
			for (int i = start_doc; i < end_index; i++) {
				ScoreDoc scoreDoc = scoreDocs[i];

				SearchHitItem hit = new SearchHitItem();
				// System.out.println("set score:" +
				// ((FieldDoc)scoreDoc).fields[0]);
				// hit.setScore(scoreDoc.score);

				int docid = scoreDoc.doc;

				Document doc = multiReader.document(docid);
				long obj_id = Long.valueOf(doc.get("o_id"));

				hit.setObject_id(obj_id);
				hit.setObject_type(type_id);
				hit.setDoc_id(docid);

				if (type_id == ObjectType.MESSAGE) {

					Long thread_id = Long.valueOf(doc.get("thread_id"));
					ArrayList<SearchHitItem> items = ((ThreadCollector) coll)
							.getHitItemByThreadId(thread_id);
					if (items != null) {
						for (SearchHitItem msg_hit : items) {
							hitItems.add(msg_hit);
						}
					}

				} else {
					hitItems.add(hit);
				}

			}

			// System.out.println("topdoc:" + topdoc.totalHits + ",type_id:"
			// + type_id + ",score length:" + scoreDocs.length + ",start:"
			// + start_doc + ",end:" + end_doc);

			result.setHitItems(type_id, hitItems);
			result.setHit_numbers(type_id, topdoc.totalHits);

		}
		// add the conversation hitcount to result.
		if (topDocs.containsKey(ObjectType.MESSAGE)) {
			Collector coll = collector.getCollector(ObjectType.MESSAGE);
			result.setHit_numbers(ObjectType.MESSAGE,
					((ThreadCollector) coll).getConverstaionsHitCount());
		}

		return result;
	}
}