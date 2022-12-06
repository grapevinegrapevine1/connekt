package com.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.model.User_plan;

public interface User_planRepository extends JpaRepository<User_plan, Integer>, JpaSpecificationExecutor<User_plan> {}
