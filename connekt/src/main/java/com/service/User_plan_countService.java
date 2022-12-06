package com.service;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.base.User_countService;
import com.model.User_plan_count;

/**
 * プラン利用数
 */
@Service
@Transactional
public class User_plan_countService extends User_countService<User_plan_count>{

	@Autowired private User_plan_count_dateService user_plan_count_dateService;
	
	/**
	 * コンストラクタ
	 */
	public User_plan_countService(){
		super("plan_id");
	}
	
	/**
	 * 期間日からユーザーの利用情報・利用日情報を取得
	 */
	public User_plan_count contain(int user_id, int id,long start_date, long end_date) {
		
		// 利用情報
		User_plan_count user_plan_count = super.contain(user_id, id, start_date, end_date);
		// 利用日情報
		if(user_plan_count != null) user_plan_count.setUser_plan_count_data(user_plan_count_dateService.find_list(user_plan_count.getId()));
		// 返却
		return user_plan_count;
	}
}
