package com.service;

import javax.transaction.Transactional;
import org.springframework.stereotype.Service;

import com.base.User_relationService;
import com.model.User_plan;

@Service
@Transactional
public class User_planService extends User_relationService<User_plan>{
	
	public User_plan containByPlanId(int store_id, int user_id, int plan_id) {
		return super.containByPlanId("plan", store_id, user_id, plan_id);
	}
	
	public void deleteAll(int store_id, int user_id) {
		super.deleteAll("user_plan", store_id, user_id);
	}
}
