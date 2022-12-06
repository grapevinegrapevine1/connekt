package com.service;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.base.User_countService;
import com.model.User_option_count;

/**
 * オプション利用数
 */
@Service
@Transactional
public class User_option_countService extends User_countService<User_option_count> {
	
	@Autowired private User_option_count_dateService user_option_count_dateService;
	
	/**
	 *  コンストラクタ
	 */
	public User_option_countService(){
		super("option_id");
	}
	
	/**
	 * 期間日からユーザーの利用情報・利用日情報を取得
	 */
	public User_option_count contain(int user_id, int id,long start_date, long end_date) {
		
		// 利用情報
		User_option_count user_option_count = super.contain(user_id, id, start_date, end_date);
		// 利用日情報
		if(user_option_count != null) user_option_count.setUser_option_count_data(user_option_count_dateService.find_list(user_option_count.getId()));
		// 返却
		return user_option_count;
	}
}
