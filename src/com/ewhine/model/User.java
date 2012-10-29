package com.ewhine.model;

import cn.gov.cbrc.db.StoreManager;
import cn.gov.cbrc.db.TableClass;
import cn.gov.cbrc.db.annotation.Column;

public class User {
	@Column(name = "name")
	String name;
	@Column(name = "id")
	long id;
	
	public Group[] groups() {
		TableClass<Group> groupTable = StoreManager.open(Group.class);
		
		return null;
	}
	
	
	public static User find_by_id(String id) {
		
		TableClass<User> userTable = StoreManager.open(User.class);
		User user = userTable.find_by_id(id);
		return user;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
