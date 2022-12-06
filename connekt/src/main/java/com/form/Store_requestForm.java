package com.form;

import java.util.List;

import javax.validation.constraints.Positive;

public class Store_requestForm {

	@Positive
	private int user_id;
	private String user_plan_id;
	private List<String> user_option_id;
	
	public int getUser_id() {
		return user_id;
	}
	public void setUser_id(int user_id) {
		this.user_id = user_id;
	}
	public String getUser_plan_id() {
		return user_plan_id;
	}
	public void setUser_plan_id(String user_plan_id) {
		this.user_plan_id = user_plan_id;
	}
	public List<String> getUser_option_id() {
		return user_option_id;
	}
	public void setUser_option_id(List<String> user_option_id) {
		this.user_option_id = user_option_id;
	}
}
