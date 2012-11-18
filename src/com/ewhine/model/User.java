package com.ewhine.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.gov.cbrc.db.StoreManager;
import cn.gov.cbrc.db.TableClass;
import cn.gov.cbrc.db.annotation.Column;

public class User {
	@Column(name = "name")
	String name;
	@Column(name = "id")
	long id;
	@Column(name = "network_id")
	long network_id;
	
	public long getNetwork_id() {
		return network_id;
	}
	
	public List<Group> groups() {
		TableClass<Group> groupTable = StoreManager.open(Group.class);
		List<Group> ret = groupTable.find_by_sql("select group_id as id from groups_users where user_id = ?", id);
		return ret;
	}
	
	public List<Group> authorizedGroups() {
		
		TableClass<Group> groupTable = StoreManager.open(Group.class);
		List<Group> user_groups = groupTable.find_by_sql("select group_id as id from groups_users where user_id = ?", id);
		List<Group> public_groups = groupTable.find_by_sql("select id from groups where network_id = ? and privacy='public'", network_id);
		public_groups.addAll(user_groups);
		Comparator<Group> c = new Comparator<Group>() {

			@Override
			public int compare(Group b, Group a) {
				
				if (a.id == b.id){
					return 0;
				}
				else{
					return a.id > b.id ? 1 : -1;
				}
					
			}
			
		};
		Collections.sort(public_groups, c );
		
		List<Group> ret = new ArrayList<Group>();
		Group last = null;
		
		for (int i=0,n=public_groups.size();i<n;i++){
			Group g = public_groups.get(i);
			if (last != null && last.id != g.id) {
				ret.add(g);
			}
			last = g;
		}
		
		return ret;
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
		User u = User.find_by_id("1");
		System.out.println("user:" + u);
		List<Group> groups = u.authorizedGroups();
		//List<Group> groups = u.groups();
		//System.out.println("groups:" + groups);
		for(Group g : groups) {
			System.out.println("group:" + g.id);
		}

	}

}
