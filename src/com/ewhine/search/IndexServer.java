package com.ewhine.search;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.queryParser.ParseException;
import org.codehaus.jackson.map.ObjectMapper;

import proj.zoie.api.ZoieException;
import cn.gov.cbrc.wh.log.Log;
import cn.gov.cbrc.wh.log.LogFactory;

import com.ewhine.redis.DocumentMessage;
import com.ewhine.redis.RedisQueue;
import com.ewhine.util.TimerTask;

public class IndexServer {

	final private static Log log = LogFactory.getLog(IndexServer.class);
	private ZoieIndexService indexService = null;
	
	private TimerTask tt = null;

	public IndexServer(ZoieIndexService indexService) {
		this.indexService = indexService;
	}

	public void start() {

		final RedisQueue queue = new RedisQueue("ewhine:search:messages");
		queue.start();

		Runnable run = new Runnable() {

			@Override
			public void run() {
				byte[] q_msg = null;
				ObjectMapper mapper = new ObjectMapper();
				ArrayList<DocumentMessage> recieved = new ArrayList<DocumentMessage>();
				while ((q_msg = queue.readMessage()) != null) {
					try {

						DocumentMessage doc = mapper.readValue(new String(q_msg),
								DocumentMessage.class);
						System.out.println("doc:" + doc);
						
						recieved.add(doc);

					} catch (IOException e) {
						e.printStackTrace();
					}
					
					if (recieved.size()>100) break; //try this batch first.
				}
				if(log.isInfoEnabled()) {
					log.info("Read:" + recieved.size() + " document messages for indexing...");
				}

				indexService.indexDocument(recieved);

			}

		};

		this.tt = new TimerTask(1000, run, "QueuePicker");
		//tt.start();
		
	}

	public void stop() {
		if (tt != null) {
			tt.stop();
		}
	}

	/**
	 * @param args
	 * @throws ZoieException
	 * @throws ParseException
	 * @throws IOException
	 */
	public static void main(String[] args) throws ZoieException,
			ParseException, IOException {
		ZoieIndexService service = new ZoieIndexService();
		service.start();
		IndexServer server = new IndexServer(service);
		server.start();
	}
}
