package com.service;

import javax.transaction.Transactional;
import org.springframework.stereotype.Service;

import com.base.User_relationService;
import com.model.User_option;

@Service
@Transactional
public class User_optionService extends User_relationService<User_option>{

	public User_option containByPlanId(int store_id, int user_id, int plan_id) {
		return super.containByPlanId("option", store_id, user_id, plan_id);
	}
	
	public void deleteAll(int store_id, int user_id) {
		super.deleteAll("user_option", store_id, user_id);
	}
}
