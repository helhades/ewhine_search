package com.ewhine.redis;

import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.NumericField;

import com.ewhine.util.Pinyin4j;

public class DocumentMessage {

	long network_id;
	long group_id = 0;
	int data_type;
	long object_id;
	String name = null;
	String description = null;
	String content = null;
	int created_at;
	int updated_at;
	long thread_id = 0;

	public DocumentMessage() {

	}

	public long getNetwork_id() {
		return network_id;
	}

	public void setNetwork_id(long network_id) {
		this.network_id = network_id;
	}

	public int getData_type() {
		return data_type;
	}

	public void setData_type(int data_type) {
		this.data_type = data_type;
	}

	public long getObject_id() {
		return object_id;
	}

	public void setObject_id(long object_id) {
		this.object_id = object_id;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getContent() {
		return content;
	}

	public long getCreated_at() {
		return created_at;
	}

	public long getUpdated_at() {
		return updated_at;
	}

	public long getGroup_id() {
		return group_id;
	}

	public void setGroup_id(long group_id) {
		this.group_id = group_id;
	}

	public String toString() {

		return "msg[network:" + network_id + ",data_type:" + data_type
				+ ",object_id:" + object_id + ",plain:" + content + "]";
	}

	public Document map() {

		Document d = new Document();

		// network_id
		NumericField f_network_id = new NumericField("network_id", Store.YES,
				true);
		f_network_id.setLongValue(network_id);
		d.add(f_network_id);

		// object_id
		NumericField f_object_id = new NumericField("o_id", Store.YES, true);
		f_object_id.setLongValue(object_id);
		d.add(f_object_id);

		// data_type
		NumericField f_type = new NumericField("type", Store.YES, true);
		f_type.setIntValue(data_type);
		d.add(f_type);

		// group_id
		if (group_id != 0) {
			NumericField f_group_id = new NumericField("g_id", Store.YES, true);
			f_group_id.setLongValue(group_id);
			d.add(f_group_id);
		}

		// name
		if (name != null) {
			//add pinyin to name
			StringBuilder sb = new StringBuilder();
			List<String[]> pinyin = Pinyin4j.ofString(name);
			for (String[] each : pinyin) {
				StringBuilder head = new StringBuilder();
				StringBuilder full = new StringBuilder();
				StringBuilder full_part = new StringBuilder();
				for (String e : each) {
					if (!e.equals(name.toLowerCase())) {
						head.append(e.charAt(0));
						full.append(e);
						full_part.append(" ").append(e);
					}
				}
				sb.append(head).append(" ").append(full).append(" ").append(full_part);
			}

			Fieldable f_name = new Field("name", name, Store.YES,
					Index.ANALYZED);
			d.add(f_name);
			
			Fieldable f_keyword = new Field("keyword", sb.toString(), Store.YES,
					Index.ANALYZED);
			d.add(f_keyword);
			
		}

		// description
		if (description != null) {
			Fieldable f_description = new Field("description", description,
					Store.YES, Index.ANALYZED);
			d.add(f_description);
		}

		// content
		if (content != null) {
			Fieldable f_content = new Field("content", content, Store.YES,
					Index.ANALYZED);
			d.add(f_content);
		}

		NumericField f_updated_at = new NumericField("updated_at", Store.YES,
				true);
		f_updated_at.setIntValue(updated_at);
		d.add(f_updated_at);

		NumericField f_created_at = new NumericField("created_at", Store.YES,
				true);
		f_created_at.setIntValue(created_at);
		d.add(f_created_at);

		if (thread_id != 0) {

			NumericField f_thread_id = new NumericField("thread_id", Store.YES,
					true);
			f_thread_id.setLongValue(thread_id);
			d.add(f_thread_id);
		}

		return d;

	}

}
