package com.ewhine.model;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.gov.cbrc.db.StoreManager;
import cn.gov.cbrc.db.TableClass;
import cn.gov.cbrc.db.annotation.Column;
import cn.gov.cbrc.db.annotation.Table;

@Table(name = "activities")
public class Activity {
	@Column(name = "id")
	long id;

	@Column(name = "user_id")
	long user_id;

	@Column(name = "action_id")
	int action_id;

	@Column(name = "updated_at")
	Timestamp updated_at;
	@Column(name = "created_at")
	Timestamp created_at;

	Action action = null;

	int target_type;

	long target_id;
	
	public long getId() {
		return id;
	}

	public int getAction_id() {
		return action_id;
	}

	public long getTarget_id() {
		return target_id;
	}

	public long getUser_id() {
		return user_id;
	}

	public long getTarget_type() {
		return target_type;
	}

	public Action getAction() {
		if (action == null) {
			TableClass<Action> act_table = StoreManager.open(Action.class);
			action = act_table.find_by_id(action_id);
		}
		return action;
	}

	public Timestamp getCreated_at() {
		return created_at;
	}

	public Timestamp getUpdated_at() {
		return updated_at;
	}
	
	public static List<Activity> find_by_user_id(long id) {
		TableClass<Activity> tc = StoreManager.open(Activity.class);
		List<Activity> activities = tc.where("user_id", new Long(id));
		Collections.sort(activities, new Comparator<Activity>() {

			@Override
			public int compare(Activity a, Activity b) {
				return a.created_at.compareTo(b.created_at);
			}
			
		});
		
		return activities;
	}
	

	public static Activity find_by_id(long id) {
		TableClass<Activity> tc = StoreManager.open(Activity.class);
		Activity activity = tc.find_by_id(id);
		
		return activity;
	}

}
