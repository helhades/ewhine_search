package com.ewhine.model;

import cn.gov.cbrc.db.annotation.Column;
import cn.gov.cbrc.db.annotation.Table;

@Table(name="actions")
public class Action {
	
	@Column(name = "id")
	long id;
	
	@Column(name = "type")
	String type = null;
	
	@Column(name = "target_id")
	long target_id;
	
	public long getId() {
		return id;
	}

	public long getTarget_id() {
		return target_id;
	}
	
	public String getType() {
		return type;
	}
	
	public int getTarget_type_id() {
		if ("Actions::ReplyMessageAction".equals(type)) {
			return ActionType.REPLY_MESSSAGE;
		}
		return -1;
	}
	
}
