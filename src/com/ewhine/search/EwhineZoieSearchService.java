package com.ewhine.search;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Explanation;
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

	public SearchResult search(String network_id, String user_id,
			String queryString) throws ZoieException {

		System.out.println("query string:" + queryString);
		System.out.println("network_id:" + network_id);
		

		SearchResult result = new SearchResult();

		User user = User.find_by_id(user_id);
		List<Group> u_groups = user.authorizedGroups();
		SecurityFilter groupFilter = new SecurityFilter(u_groups);

		List<ZoieIndexReader<IndexReader>> readers = null;

		MultiReader multiReader = null;
		IndexSearcher searcher = null;
		try {
			Query q = null;
			if (queryString == null || queryString.length() == 0) {
				q = new MatchAllDocsQuery();
			} else {
				//Analyzer analyzer = new WhitespaceAnalyzer(Version.LUCENE_35);
				Analyzer analyzer = _idxReaderFactory.getAnalyzer();
				QueryParser qparser = new QueryParser(Version.LUCENE_35,
						"content", analyzer);
				qparser.setPhraseSlop(1);
				
				
				q = qparser.parse(queryString);
				System.out.println("pared:" +q);

			}

			TermQuery network_limit = new TermQuery(new Term("network_id",
					NumericUtils.longToPrefixCoded(Long.valueOf(network_id))));
			BooleanQuery all_combin = new BooleanQuery();
			all_combin.add(network_limit, BooleanClause.Occur.MUST);
			all_combin.add(q, BooleanClause.Occur.MUST);
			readers = _idxReaderFactory.getIndexReaders();
			multiReader = new MultiReader(
					readers.toArray(new IndexReader[readers.size()]), false);

			searcher = new IndexSearcher(multiReader);
			long start = System.currentTimeMillis();
			// EwhineSearchCollector results = new
			// EwhineSearchCollector(u_groups);

			// searcher.search(q, filter, results);

			// TopDocs topDocs = searcher.search(q, filter, 100);
			
			TopDocs topDocs = searcher.search(all_combin, null, 100);
			//Explanation exp = searcher.explain(all_combin, 1);
			//System.out.println("exp:" + exp);

			long end = System.currentTimeMillis();

			result.setTime(end - start);
			result.setTotalDocs(multiReader.numDocs());
			result.setTotalHits(topDocs.totalHits);

			ScoreDoc[] scoreDocs = topDocs.scoreDocs;
			System.out.println("score docs:" + scoreDocs);
			ArrayList<SearchHitItem> hitList = new ArrayList<SearchHitItem>(
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
				System.out.println("content:" + content);
				TokenStream tokenStream = analyzer.tokenStream("content",
						new StringReader(content)); // four
				String fragments = hl.getBestFragments(tokenStream, content,
						maxNumFragmentsRequired, "...");

				hit.setHighlightContent(fragments);

				hitList.add(hit);
			}

			result.setHitItems(hitList);
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