package com.ewhine.redis;

public class DocumentMessage {

	long network_id;
	int data_type;
	long object_id;
	
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
	public String toString() {
		
		return "msg[network:"+ network_id + ",data_type:" + data_type+ ",object_id:" + object_id+"]";
	}
	

}
