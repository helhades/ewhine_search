package com.ewhine.search;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FieldDoc;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.Scorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.util.NumericUtils;
import org.apache.lucene.util.Version;

import proj.zoie.api.IndexReaderFactory;
import proj.zoie.api.ZoieException;
import proj.zoie.api.ZoieIndexReader;

import com.ewhine.model.Group;
import com.ewhine.model.User;

public class EwhineZoieSearchService {

	private static final Logger log = Logger
			.getLogger(EwhineZoieSearchService.class);

	private IndexReaderFactory<ZoieIndexReader<IndexReader>> _idxReaderFactory;

	public EwhineZoieSearchService(
			IndexReaderFactory<ZoieIndexReader<IndexReader>> idxReaderFactory) {
		_idxReaderFactory = idxReaderFactory;
	}

	public SearchResult search(long user_id, String queryString)
			throws ZoieException {

		User user = User.find_by_id(user_id);
		long network_id = user.getNetwork_id();
		List<Group> u_groups = user.authorizedGroups();

		if (log.isInfoEnabled()) {
			log.info("User:" + user_id + ",Network:" + network_id
					+ ",Query string:" + queryString);
		}

		SecurityFilter groupFilter = new SecurityFilter(u_groups);
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
				QueryParser qparser = new QueryParser(Version.LUCENE_35,
						"content", analyzer);
				qparser.setPhraseSlop(1);

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
			combine_query.add(q, BooleanClause.Occur.MUST);

			readers = _idxReaderFactory.getIndexReaders();
			multiReader = new MultiReader(
					readers.toArray(new IndexReader[readers.size()]), false);

			// set the sort.
			Sort sort = new Sort(new SortField("interest_score",
					new DocumentComparatorSource()));

			searcher = new IndexSearcher(multiReader);
			long start = System.currentTimeMillis();

			// start a new search.
			TopFieldDocs topDocs = searcher.search(combine_query, groupFilter, 10,
					sort);
			// Explanation exp = searcher.explain(all_combin, 1);
			// System.out.println("exp:" + exp);

			long end = System.currentTimeMillis();

			result.setTime(end - start);
			result.setTotalDocs(multiReader.numDocs());
			result.setTotalHits(topDocs.totalHits);

			ScoreDoc[] scoreDocs = topDocs.scoreDocs;
			

			//highlight the query word.
			Scorer qs = new QueryScorer(q);
			SimpleHTMLFormatter formatter = new SimpleHTMLFormatter(
					"<span class=\"hl\">", "</span>");
			Highlighter hl = new Highlighter(formatter, qs);
			int maxNumFragmentsRequired = 20;
			
			Analyzer analyzer = _idxReaderFactory.getAnalyzer();
			
			String pattern = "yyyy-MM-dd'T'HH:mm:ss:SSSZZ";
			
			ArrayList<SearchHitItem> hitItems = new ArrayList<SearchHitItem>(
					scoreDocs.length);
			for (ScoreDoc scoreDoc : scoreDocs) {

				SearchHitItem hit = new SearchHitItem();
				//System.out.println("set score:" + ((FieldDoc)scoreDoc).fields[0]);
				//hit.setScore(scoreDoc.score);

				int docid = scoreDoc.doc;

				Document doc = multiReader.document(docid);
				String content = doc.get("content");
				hit.setObject_id(Long.valueOf(doc.get("o_id")));
				hit.setThread_id(Long.valueOf(doc.get("thread_id")));
				hit.setObject_type(Integer.valueOf(doc.get("type")));
				
				hit.setUpdated_at(DateFormatUtils.format(new Date(Long.valueOf(doc.get("created_at") )*1000L), pattern));;
				hit.setUpdated_at(DateFormatUtils.format(new Date(Long.valueOf(doc.get("updated_at") )*1000L), pattern));;

				TokenStream tokenStream = analyzer.tokenStream("content",
						new StringReader(content)); // four
				String fragments = hl.getBestFragments(tokenStream, content,
						maxNumFragmentsRequired, "...");
				hit.setHighlightContent(fragments);
				hitItems.add(hit);

			}

			result.setHitItems(hitItems);
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