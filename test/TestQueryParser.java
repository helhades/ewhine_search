import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.lucene.IKAnalyzer;



public class TestQueryParser {

	/**
	 * @param args
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws ParseException {
		
		Analyzer analyzer = new IKAnalyzer();
		QueryParser qparser = new MultiFieldQueryParser(
				Version.LUCENE_35, new String[] { "name", "keyword",
						"description", "content" }, analyzer);
		qparser.setPhraseSlop(1);
		
		qparser.setDefaultOperator(QueryParser.AND_OPERATOR);

		Query q = qparser.parse("card*");
		System.out.println("query:" + q.getClass().getName());
		
		HashSet<Term> out = new HashSet<Term>();
		
		QueryScorer qs = new QueryScorer(q);
		
		 SimpleHTMLFormatter simpleHTMLFormatter = new SimpleHTMLFormatter( 
		          "<read>", "</read>");
		 
		 Highlighter highlighter  = new Highlighter(simpleHTMLFormatter, qs);
		 TokenStream ts = analyzer.tokenStream("hello", new StringReader("放在我工作台上的iphone不见了。card"));
		 try {
			String s = highlighter.getBestFragment(ts, "放在我工作台上的iphone不见了。card");
			System.out.println("s:" + s);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidTokenOffsetsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		 q.extractTerms(out);
		HashSet<String> queryText = new HashSet<String>();

		for (Term term : out) {
			if (!queryText.contains(term.text())) {
				queryText.add(term.text());
			}
		}
		
		System.out.println("queryText:"+ queryText);

	}

}
