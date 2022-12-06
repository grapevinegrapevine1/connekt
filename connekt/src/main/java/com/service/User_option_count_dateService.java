package com.service;

import javax.transaction.Transactional;
import org.springframework.stereotype.Service;

import com.base.User_count_dateService;
import com.model.User_option_count_date;

/**
 * オプション利用日
 */
@Service
@Transactional
public class User_option_count_dateService extends User_count_dateService<User_option_count_date> {
	
	/**
	 *  コンストラクタ
	 */
	public User_option_count_dateService(){
		super("option");
	}
}
