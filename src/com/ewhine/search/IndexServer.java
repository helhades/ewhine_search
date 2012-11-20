package com.ewhine.search;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.queryParser.ParseException;

import proj.zoie.api.ZoieException;
import cn.gov.cbrc.wh.log.Log;
import cn.gov.cbrc.wh.log.LogFactory;

import com.ewhine.redis.DocumentMessage;
import com.ewhine.redis.RedisQueue;
import com.ewhine.util.TimerTask;
import com.google.gson.Gson;

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
				Gson gson = new Gson();
				ArrayList<DocumentMessage> recieved = new ArrayList<DocumentMessage>();
				while ((q_msg = queue.readMessage()) != null) {
					try {

						DocumentMessage doc = gson.fromJson(new String(q_msg,
								"utf-8"), DocumentMessage.class);

						if (log.isDebugEnabled()) {
							log.debug("Readed a doc:" + doc);
						}
						recieved.add(doc);

					} catch (IOException e) {
						if (log.isErrorEnabled()) {
							log.error("Read message error.", e);
						}
					}

					if (recieved.size() > 10) {
						break; // try this batch first.
					}
				}
				int n = recieved.size();
				if (n > 0 && log.isInfoEnabled()) {
					log.info("Read:" + recieved.size()
							+ " document messages for indexing...");
				}

				indexService.indexDocument(recieved);

			}

		};

		this.tt = new TimerTask(1000, run, "QueuePicker");
		tt.start();

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
