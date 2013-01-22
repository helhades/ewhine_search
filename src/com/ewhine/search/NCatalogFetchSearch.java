package com.ewhine.search;

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

public class NCatalogFetchSearch implements ISearchModel {

	private static final Logger log = Logger
			.getLogger(NCatalogFetchSearch.class);

	private IndexReaderFactory<ZoieIndexReader<IndexReader>> _idxReaderFactory;

	public NCatalogFetchSearch(
			IndexReaderFactory<ZoieIndexReader<IndexReader>> idxReaderFactory) {
		_idxReaderFactory = idxReaderFactory;
	}

	public ISearchResult search(long user_id, String queryString, String type_id)
			throws ZoieException {

		User user = User.find_by_id(user_id);
		long network_id = user.getNetwork_id();
		// List<Group> u_groups = user.authorizedGroups();
		long query_id = System.currentTimeMillis();

		if (log.isInfoEnabled()) {
			StringBuilder sb = new StringBuilder();
			sb.append("qid:").append(query_id).append(",uid:").append(user_id)
					.append(",query:").append(queryString).append(",type_id:")
					.append(type_id);
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
				qparser.setDefaultOperator(QueryParser.AND_OPERATOR);

				q = qparser.parse(queryString);

				if (log.isInfoEnabled()) {
					log.info("Query user_id:" + user_id + ",Network:"
							+ network_id + ",q paraed:" + q);

				}

			}

			// 2. Add security limit.
			TermQuery network_limit = new TermQuery(new Term("network_id",
					NumericUtils.longToPrefixCoded(network_id)));

			BooleanQuery combine_query = new BooleanQuery();

			combine_query.add(network_limit, BooleanClause.Occur.MUST);

			if (type_id != null) {
				TermQuery type_limit = new TermQuery(
						new Term("type", NumericUtils.intToPrefixCoded(Integer
								.parseInt(type_id))));
				combine_query.add(type_limit, BooleanClause.Occur.MUST);
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
					ObjectType.USER, ObjectType.ATTACHMENT_FILE, ObjectType.TAG };
			NCatalogSearchCollector collector = new NCatalogSearchCollector(
					user.authorizedGroups(), user.conversation_groups(),
					type_ids);

			// 5. Start a search.
			searcher.search(combine_query, collector);

			// Explanation exp = searcher.explain(all_combin, 1);
			// System.out.println("exp:" + exp);

			long end = System.currentTimeMillis();
			long time = query_id - end;
			if (time > 200) {
				log.warn("query_id:" + query_id + " spend:" + time + "ms");
			}

			HashMap<Integer, TopDocs> topdocs = collector.topDocs();

			ISearchResult result = processResult(multiReader, topdocs);

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

	private ISearchResult processResult(MultiReader multiReader,
			HashMap<Integer, TopDocs> topDocs) throws CorruptIndexException,
			IOException {
		TypedSearchResult result = new TypedSearchResult();

		result.setTotalDocs(multiReader.numDocs());

		Set<Integer> keys = topDocs.keySet();
		for (Integer type_id : keys) {
			TopDocs topdoc = topDocs.get(type_id);
			ScoreDoc[] scoreDocs = topdoc.scoreDocs;
			ArrayList<SearchHitItem> hitItems = new ArrayList<SearchHitItem>(
					scoreDocs.length);
			for (ScoreDoc scoreDoc : scoreDocs) {

				SearchHitItem hit = new SearchHitItem();
				// System.out.println("set score:" +
				// ((FieldDoc)scoreDoc).fields[0]);
				// hit.setScore(scoreDoc.score);

				int docid = scoreDoc.doc;

				Document doc = multiReader.document(docid);

				hit.setObject_id(Long.valueOf(doc.get("o_id")));
				hit.setObject_type(Integer.valueOf(doc.get("type")));
				hit.setDoc_id(docid);

				String name = doc.get("name");

				if (name != null) {
					hit.setName(name);
				}

				hitItems.add(hit);

			}
			result.setHitItems(type_id, hitItems);
			result.setHit_numbers(type_id, topdoc.totalHits);
		}

		return result;
	}
}