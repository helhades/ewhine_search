import cn.gov.cbrc.db.StoreManager;
import cn.gov.cbrc.db.TableClass;

import com.ewhine.model.Message;
import com.ewhine.redis.DocumentMessage;
import com.ewhine.search.index.ZoieIndexService;


public class TestIndex {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TableClass<Message> msgTable = StoreManager.open(Message.class);
		Message msg = msgTable.find_by_id(String.valueOf(843));
		
		DocumentMessage dm = new DocumentMessage();
		dm.setNetwork_id(1);
		dm.setData_type(1);
		dm.setObject_id(msg.getId());
		dm.setContent(msg.getRich());
		dm.setGroup_id(msg.getGroup_id());
		
		
		ZoieIndexService index_service = new ZoieIndexService();
		index_service.start();
		index_service.indexDocument(dm);
		index_service.stop();

	}

}
