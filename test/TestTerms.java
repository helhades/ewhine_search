import java.util.List;

import org.apache.lucene.index.Term;

import com.ewhine.search.EwhineIndexReaderFactory;
import com.ewhine.search.EwhineZoieSearchService;
import com.ewhine.search.ZoieIndexService;


public class TestTerms {
	public static void main(String[] args) {
		ZoieIndexService index_service = new ZoieIndexService();
		index_service.start();
		
		EwhineIndexReaderFactory factory = new EwhineIndexReaderFactory(index_service);
		EwhineZoieSearchService searcher = new EwhineZoieSearchService(factory);
		List<Term> tms = searcher.terms();
		System.out.println("tms:" + tms);
		for(Term t : tms) {
			System.out.println("t:" + t);
		}
 

		
		
		index_service.stop();
	}

}
