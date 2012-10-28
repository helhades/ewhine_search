import com.ewhine.redis.DocumentMessage;
import com.ewhine.search.ZoieIndexService;


public class TestIndex {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DocumentMessage dm = new DocumentMessage();
		dm.setNetwork_id(1);
		dm.setData_type(1);
		dm.setObject_id(31);
		ZoieIndexService index_service = new ZoieIndexService();
		index_service.start();
		index_service.indexDocument(dm);
		index_service.stop();
		

	}

}
