import java.io.IOException;
import java.io.StringReader;

import org.ansj.lucene3.AnsjAnalysis;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;


public class TestAnalyzer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		AnsjAnalysis analyzer = new AnsjAnalysis(false);
		
		TokenStream ts = analyzer.tokenStream("hello", new StringReader("ÎÒµÄiphone¶ªÁË"));

		Token token;
		try
	    {
			 while(ts.incrementToken())
	      {
	        System.out.println(ts.getAttribute(TermAttribute.class).term());
	      }
	    }
	    catch (IOException ioe)
	    {
	      ioe.printStackTrace();
	    }

	}

}
