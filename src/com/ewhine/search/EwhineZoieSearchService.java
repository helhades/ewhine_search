package com.ewhine.search;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
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
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.Scorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.util.NumericUtils;
import org.apache.lucene.util.Version;

import proj.zoie.api.IndexReaderFactory;
import proj.zoie.api.ZoieException;
import proj.zoie.api.ZoieIndexReader;

import com.ewhine.model.User;

public class EwhineZoieSearchService {

	private static final Logger log = Logger
			.getLogger(EwhineZoieSearchService.class);

	private IndexReaderFactory<ZoieIndexReader<IndexReader>> _idxReaderFactory;

	public EwhineZoieSearchService(
			IndexReaderFactory<ZoieIndexReader<IndexReader>> idxReaderFactory) {
		_idxReaderFactory = idxReaderFactory;
	}

	public List<Term> terms() {
		MultiReader multiReader = null;
		List<Term> t_list = new ArrayList<Term>();
		try {
			List<ZoieIndexReader<IndexReader>> readers = _idxReaderFactory
					.getIndexReaders();
			multiReader = new MultiReader(
					readers.toArray(new IndexReader[readers.size()]), true);
			for (IndexReader rd : readers) {
				TermEnum ts = rd.terms();
				while (ts.next()) {
					Term term = ts.term();
					System.out.println("term:" + term);
				}
			}

			TermEnum tms = multiReader.terms();
			while (tms.next()) {
				Term term = tms.term();
				t_list.add(term);
			}

			return t_list;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (multiReader != null) {
				try {
					multiReader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return t_list;

	}

	public SearchResult search(long user_id, String queryString,String type_id)
			throws ZoieException {

		User user = User.find_by_id(user_id);
		long network_id = user.getNetwork_id();
		// List<Group> u_groups = user.authorizedGroups();

		if (log.isInfoEnabled()) {
			log.info("qid:3217," + "uid:" + user_id + ",query:" + queryString+",type_id:" + type_id);
		}

		// SecurityFilter groupFilter = new SecurityFilter(u_groups);
		List<ZoieIndexReader<IndexReader>> readers = null;

		MultiReader multiReader = null;
		IndexSearcher searcher = null;

		SearchResult result = new SearchResult();

		try {

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

			TermQuery network_limit = new TermQuery(new Term("network_id",
					NumericUtils.longToPrefixCoded(network_id)));
			
			

			BooleanQuery combine_query = new BooleanQuery();
			
			combine_query.add(network_limit, BooleanClause.Occur.MUST);
			
			if (type_id != null) {
				TermQuery type_limit = new TermQuery(new Term("type",
						NumericUtils.intToPrefixCoded(Integer.parseInt(type_id))));
				combine_query.add(type_limit, BooleanClause.Occur.MUST);
			}
			
			combine_query.add(q, BooleanClause.Occur.MUST);

			readers = _idxReaderFactory.getIndexReaders();
			multiReader = new MultiReader(
					readers.toArray(new IndexReader[readers.size()]), false);

			searcher = new IndexSearcher(multiReader);
			long start = System.currentTimeMillis();

			// set the sort.
			// Sort sort = new Sort(new SortField("updated_at",
			// new DocumentComparatorSource()));

			// start a new search.
			// TopDocs topDocs = searcher.search(combine_query, groupFilter,
			// 10, sort);

			EwhineSearchCollector collector = new EwhineSearchCollector(
					user.authorizedGroups(), user.conversation_groups());
			searcher.search(combine_query, collector);
			TopDocs topDocs = collector.topDocs();

			// Explanation exp = searcher.explain(all_combin, 1);
			// System.out.println("exp:" + exp);

			long end = System.currentTimeMillis();

			result.setTime(end - start);
			result.setTotalDocs(multiReader.numDocs());
			result.setTotalHits(topDocs.totalHits);

			ScoreDoc[] scoreDocs = topDocs.scoreDocs;

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

			result.setHitItems(hitItems);
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
}