package com.ewhine.search;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

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

	public SearchResult search(String user_id, String queryString)
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
				System.out.println("pared:" + q);
				
				if (log.isInfoEnabled()) {
					log.info("Query pared:" + user_id + ",Network:" + network_id
							+ ",Query string:" + queryString);
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

			searcher = new IndexSearcher(multiReader);
			long start = System.currentTimeMillis();

			TopDocs topDocs = searcher.search(combine_query, groupFilter, 10);
			// Explanation exp = searcher.explain(all_combin, 1);
			// System.out.println("exp:" + exp);

			long end = System.currentTimeMillis();

			result.setTime(end - start);
			result.setTotalDocs(multiReader.numDocs());
			result.setTotalHits(topDocs.totalHits);

			ScoreDoc[] scoreDocs = topDocs.scoreDocs;
			ArrayList<SearchHitItem> hitItems = new ArrayList<SearchHitItem>(
					scoreDocs.length);

			Scorer qs = new QueryScorer(q);
			SimpleHTMLFormatter formatter = new SimpleHTMLFormatter(
					"<span class=\"hl\">", "</span>");
			Highlighter hl = new Highlighter(formatter, qs);
			int maxNumFragmentsRequired = 20;
			Analyzer analyzer = _idxReaderFactory.getAnalyzer();
			
			for (ScoreDoc scoreDoc : scoreDocs) {
				
				SearchHitItem hit = new SearchHitItem();
				hit.setScore(scoreDoc.score);
				
				int docid = scoreDoc.doc;

				Document doc = multiReader.document(docid);
				String content = doc.get("content");
				hit.setObject_id(Long.valueOf(doc.get("o_id")));
				hit.setObject_type(Integer.valueOf(doc.get("type")));
				hit.setCreated_at(Integer.valueOf(doc.get("created_at")));
				hit.setUpdated_at(Integer.valueOf(doc.get("updated_at")));
				
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