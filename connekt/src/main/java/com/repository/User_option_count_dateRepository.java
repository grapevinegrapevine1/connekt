package com.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.model.User_option_count_date;

public interface User_option_count_dateRepository extends JpaRepository<User_option_count_date, Integer>, JpaSpecificationExecutor<User_option_count_date> {

	@Query(value = "SELECT * FROM user_option_count_date where user_option_count_id = :user_option_count_id and date(use_date) = date(now())",
			nativeQuery = true)
	public User_option_count_date checkUsedToday(@Param("user_option_count_id") int user_option_count_id);
}
