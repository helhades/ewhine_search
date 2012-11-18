import java.io.UnsupportedEncodingException;
import java.util.List;

import com.ewhine.redis.DocumentMessage;
import com.ewhine.redis.RedisQueue;
import com.google.gson.Gson;

public class TestGson {

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
			json = new String(out.get(0), "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("json stirng: " + json);
		
		Gson gson = new Gson();
		try {
			DocumentMessage doc = gson.fromJson(json, DocumentMessage.class);
			System.out.println("doc:" + doc);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
