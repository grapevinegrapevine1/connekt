package com.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.model.User_plan_count;

public interface User_plan_countRepository extends JpaRepository<User_plan_count, Integer>, JpaSpecificationExecutor<User_plan_count> {}
