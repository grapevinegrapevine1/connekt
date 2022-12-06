package com.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.model.User_plan_count_date;

public interface User_plan_count_dateRepository extends JpaRepository<User_plan_count_date, Integer>, JpaSpecificationExecutor<User_plan_count_date> {

	@Query(value="select * from user_plan_count_date  where user_plan_count_id = :user_plan_count_id and date(use_date) = date(now()) limit 1",
			nativeQuery = true)
	public User_plan_count_date checkUsedToday(@Param("user_plan_count_id") int user_plan_count_id);
}
