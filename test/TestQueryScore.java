import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.lucene.IKAnalyzer;


public class TestQueryScore {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		IKAnalyzer analyzer = new IKAnalyzer();
		//Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_35);
		QueryParser qparser = new MultiFieldQueryParser(
				Version.LUCENE_35, new String[] { "name", "keyword",
						"description", "content" }, analyzer);
		Query q;
		try {
			q = qparser.parse("ËÑË÷ and Çæ");
			Set<Term> ts = new HashSet<Term>();
			q.extractTerms(ts);
			for(Term t : ts) {
				System.out.println("q:" + t.text());
			}
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		

	}

}
