package com.ewhine.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.gov.cbrc.db.StoreManager;
import cn.gov.cbrc.db.TableClass;
import cn.gov.cbrc.db.annotation.Column;
import cn.gov.cbrc.db.annotation.Table;

@Table(name="users")
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
	//Fixme: use the memcached to improve performance.
	public List<Group> groups() {
		TableClass<Group> groupTable = StoreManager.open(Group.class);
		List<Group> ret = groupTable.find_by_sql("SELECT `groups`.id as id FROM `groups` INNER JOIN `groups_users` ON `groups`.`id` = `groups_users`.`group_id` WHERE `groups`.`deleted` = 0 AND `groups_users`.`user_id` = ?", id);
		return ret;
	}
	
	public List<Group> conversation_groups() {
		TableClass<Group> groupTable = StoreManager.open(Group.class);
		List<Group> ret = groupTable.find_by_sql("SELECT `conversations`.id as id FROM `conversations` INNER JOIN `conversations_users` ON `conversations`.`id` = `conversations_users`.`conversation_id` WHERE `conversations_users`.`user_id` = ?", id);
		return ret;
	}
	
	public List<Group> authorizedGroups() {
		
		TableClass<Group> groupTable = StoreManager.open(Group.class);
		List<Group> user_groups = groups();
		List<Group> public_groups = groupTable.find_by_sql("select id from groups where network_id = ? and public_group=1 and deleted=0", network_id);
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
		// remove the duplicated group
		for (int i=0,n=public_groups.size();i<n;i++){
			Group g = public_groups.get(i);
			if (last != null && last.id != g.id) {
				ret.add(g);
			}
			last = g;
		}
		
		return ret;
	}
 	
	
	public static User find_by_id(long id) {
		
		TableClass<User> userTable = StoreManager.open(User.class);
		User user = userTable.find_by_id(id);
		return user;
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		User u = User.find_by_id(1L);
		System.out.println("user:" + u);
		List<Group> groups = u.authorizedGroups();
		//List<Group> groups = u.groups();
		//System.out.println("groups:" + groups);
		for(Group g : groups) {
			System.out.println("group:" + g.id);
		}
		List<Group> gs = u.conversation_groups();
		for(Group g : gs) {
			System.out.println("c_group:" + g.id);
		}
		

	}

}
