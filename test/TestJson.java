import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.ewhine.model.DocumentPackage;
import com.ewhine.redis.RedisQueue;


public class TestJson {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
RedisQueue queue = new RedisQueue("ewhine:search:messages");
		
		queue.start();
		
		System.out.println("sart...");
		
		
		List<byte[]> out = queue.popMessage();
		String json = null;
		try {
			json = new String(out.get(0),"US-ASCII");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("json stirng: " + json);
		ObjectMapper mapper = new ObjectMapper();
		try {
			DocumentPackage doc = mapper.readValue(json, DocumentPackage.class);
			System.out.println("doc:"+doc);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
