package com.form;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.Positive;

import com.model.User_option;
import com.model.User_plan;

public class User_storeForm {

	@Positive
	private int store_id;
	@Positive
	private int user_id;
	private User_plan user_plan;
	private List<Integer> user_options;
	
	// thymeleaf用オプション情報リスト
	private List<User_option> user_options_th;
	public User_option getUserOption(int option_id) {
		return user_options_th != null ? user_options_th.stream().filter(uo->uo.getOption_id() == option_id).findFirst().orElse(null): null;
		
	}
	
	public int getUser_id() {
		return user_id;
	}
	public void setUser_id(int user_id) {
		this.user_id = user_id;
	}
	public int getStore_id() {
		return store_id;
	}
	public void setStore_id(int store_id) {
		this.store_id = store_id;
	}
	public User_plan getUser_plan() {
		return user_plan;
	}
	public void setUser_plan(User_plan user_plan) {
		this.user_plan = user_plan;
	}
	public List<Integer> getUser_options() {
		return user_options;
	}
	public List<User_option> getUser_options(int store_id,int user_id) {
		return user_options.stream().map(option_id->new User_option(store_id,user_id,option_id)).collect(Collectors.toList());
	}
	public void setUser_options(List<Integer> user_options) {
		this.user_options = user_options;
	}
	public void setUser_options_th(List<User_option> user_options_th) {
		this.user_options_th = user_options_th;
	}
}
