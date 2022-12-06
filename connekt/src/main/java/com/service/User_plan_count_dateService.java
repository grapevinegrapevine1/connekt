package com.service;

import javax.transaction.Transactional;
import org.springframework.stereotype.Service;

import com.base.User_count_dateService;
import com.model.User_plan_count_date;

/**
 * プラン利用日
 */
@Service
@Transactional
public class User_plan_count_dateService extends User_count_dateService<User_plan_count_date>{
	
	/**
	 * コンストラクタ
	 */
	public User_plan_count_dateService(){
		super("plan");
	}
}
