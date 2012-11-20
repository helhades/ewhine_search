package com.ewhine.redis;

import java.util.ArrayList;
import java.util.List;

import redis.clients.jedis.Jedis;
import cn.gov.cbrc.db.StoreManager;
import cn.gov.cbrc.db.TableClass;
import cn.gov.cbrc.wh.log.Log;
import cn.gov.cbrc.wh.log.LogFactory;

import com.ewhine.model.Message;

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
		System.out.println("start running message queue.");

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
		log.debug("entry the block method now ...");
		List<byte[]> results = jedis.blpop(timeout, queueName.getBytes());
		if (results == null || results.isEmpty()) {
			log.debug("with timeout : " + timeout
					+ " get empty list. will continue now ...");
			return null;
		}

		log.debug("with timeout : " + timeout + " get not empty list : ");
		if (results != null && results.size() == 2) {
			return results.get(1);
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
	// public byte[] getValue(String key) {
	// return jedis.get(key.getBytes());
	// }

	public void stop() {

		if (runThread != null) {
			runThread.interrupt();
		}

		if (jedis != null) {
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
		TableClass<Message> msgTable = StoreManager.open(Message.class);
		RedisQueue queue = new RedisQueue("messages");

		queue.start();

		System.out.println("sart...");

		Message m = msgTable.find_by_id(3);
		System.out.println("message:" + m);
		queue.stop();

		List<byte[]> out = queue.popMessage();
		System.out.println("out length:" + out.size());
		try {
			Thread.currentThread().sleep(12000);
			System.out.println("Finished!");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
