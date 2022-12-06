package com.form;

import java.util.ArrayList;
import java.util.List;

import com.model.User_option;
import com.model.User_plan;

public class User_listForm {
	
	private int store_id;
	private String store_name;
	private String contract_text;
	private User_plan user_plan;
	private List<User_option> user_options = new ArrayList<User_option>();
	
	public int getStore_id() {
		return store_id;
	}
	public void setStore_id(int store_id) {
		this.store_id = store_id;
	}
	public String getStore_name() {
		return store_name;
	}
	public void setStore_name(String store_name) {
		this.store_name = store_name;
	}
	public String getContract_text() {
		return contract_text;
	}
	public void setContract_text(String contract_text) {
		this.contract_text = contract_text;
	}
	public User_plan getUser_plan() {
		return user_plan;
	}
	public void setUser_plan(User_plan user_plan) {
		this.user_plan = user_plan;
	}
	public List<User_option> getUser_options() {
		return user_options;
	}
	public void setUser_options(List<User_option> user_options) {
		this.user_options = user_options;
	}
}
