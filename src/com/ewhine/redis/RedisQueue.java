package com.ewhine.redis;

import java.util.ArrayList;
import java.util.List;

import redis.clients.jedis.Jedis;
import cn.gov.cbrc.wh.log.Log;
import cn.gov.cbrc.wh.log.LogFactory;

public class RedisQueue {
	final private static Log log = LogFactory.getLog(RedisQueue.class);
	Jedis jedis = null;
	private String queueName = null;
	private int timeout = 5; // seconds
	private Thread runThread;

	public RedisQueue(String queueName) {
		this.queueName = queueName;

	}

	public void clear() {
		if (jedis != null) {
			jedis.del(queueName.getBytes());
		}
	}

	public void start() {

		jedis = new Jedis("localhost");
		// this.clear();
		// runThread.start();
		log.info("start running message queue.");

	}

	public List<byte[]> popMessage() {

		List<byte[]> collection = new ArrayList<byte[]>();
		for (int i = 0; i < 20; i++) {

			List<byte[]> results = jedis.blpop(timeout, queueName.getBytes());
			if (results == null || results.isEmpty()) {
				return collection;
			}
			if (results != null && results.size() == 2) {
				collection.add(results.get(1));
			} else {
				log.error("Data format error,expected: results.size=2,but:"
						+ results.size());
			}
		}

		return collection;

	}

	public byte[] readMessage() {
		List<byte[]> results = null;
		try {
			log.debug("entry the block method now ,read key: " + queueName
					+ ",timeout:" + timeout + "...");
			 results = jedis.blpop(timeout, queueName.getBytes());

			// log.debug("read result: " + results + "...");
		} catch (redis.clients.jedis.exceptions.JedisException je) {
			log.error("Connect redis error:",je);
			jedis.disconnect();
			jedis.connect();
		}

		if (results != null && results.size() == 2) {
			log.debug("with timeout : " + timeout + " get not empty list : ");
			return results.get(1);
		} else if (results == null || results.isEmpty()) {
			log.debug("with timeout : " + timeout
					+ " get empty list. will continue now ...");
			return null;
		} else {

			log.error("Data format error,expected: results.size=2,but:"
					+ results.size());
			return null;
		}
	}

	// public void setValue(String key,byte[] value) {
	// jedis.set(key.getBytes(), value);
	// }
	//
	// public List<byte[]> getValue(String key) {
	// return jedis.blpop(5, key.getBytes());
	// }

	public void stop() {

		if (runThread != null) {
			runThread.interrupt();
		}

		if (jedis != null) {
			jedis.disconnect();
			jedis.quit();
			jedis = null;
		}
	}

	public long pushMessage(byte[] m) {
		if (jedis != null) {
			System.out.println("push queue:" + queueName);
			return jedis.rpush(queueName.getBytes(), m);
		} else {
			System.err.println("Failed push message.jedis is null.");
			return 0;
		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// RedisQueue queue = new RedisQueue("ewhine:search:messages");
		// queue.start();
		// List<byte[]> queue_list = queue.getValue("ewhine:search:messages");
		// System.out.println("queue list:" + queue_list.size());
		// System.out.println("queue empty:" + queue_list.isEmpty());

	}

}
