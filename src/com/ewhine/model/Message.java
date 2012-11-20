package com.ewhine.model;

import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;

import redis.clients.jedis.Jedis;
import cn.gov.cbrc.db.StoreManager;
import cn.gov.cbrc.db.TableClass;
import cn.gov.cbrc.db.annotation.Column;

import com.ewhine.redis.DocumentMessage;



public class Message {
	public static final int type = 1;
	@Column(name = "plain")
	String rich;
	@Column(name = "id")
	long id;
	@Column(name = "group_id")
	long group_id;
	
	public long getGroup_id() {
		return group_id;
	}
	
	public long getId() {
		return id;
	}
	
	public String getRich() {
		return rich;
	}
	
	
	@Override
	public String toString() {
		return rich.toString();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TableClass<Message> msgTable = StoreManager.open(Message.class);
		System.out.println("sart...");
		
		List<Message> all = msgTable.find_all();
		Jedis jedis = new Jedis("localhost");
		
		for (int i = 0, n = all.size(); i < n; i++) {
			Message m = all.get(i);
	
			System.out.println("message plain:" + m.rich);
		}
		
		jedis.quit();

	}

	public static Document map(DocumentMessage qm) {
		
		Document d = new Document();

		// network_id
		NumericField f_network_id = new NumericField("network_id", Store.YES,
				true);
		f_network_id.setLongValue(qm.getNetwork_id());
		d.add(f_network_id);
		
		
		// data_type
		NumericField f_type = new NumericField("type", Store.YES, true);
		f_type.setIntValue(qm.getData_type());
		d.add(f_type);
		

		
		long id = qm.getObject_id();
		TableClass<Message> msgTable = StoreManager.open(Message.class);
		Message msg = msgTable.find_by_id(String.valueOf(id));
		// content
		Fieldable f_content = new Field("content", msg.rich,
				Store.YES, Index.ANALYZED);
		d.add(f_content);
		// group_id
		NumericField f_group_id = new NumericField("g_id", Store.YES,
				true);
		f_group_id.setLongValue(msg.group_id);
		d.add(f_group_id);
		
		NumericField f_object_id = new NumericField("o_id", Store.YES,
				true);
		
		f_object_id.setLongValue(msg.id);
		d.add(f_object_id);

		
		
		return d;
	}

}
