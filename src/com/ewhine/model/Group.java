package com.ewhine.model;

import cn.gov.cbrc.db.annotation.Column;

public class Group {
	
	@Column(name = "id")
	long id;
	
	public long getId() {
		return id;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
