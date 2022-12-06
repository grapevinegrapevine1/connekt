package com.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.model.User_option_count;

public interface User_option_countRepository extends JpaRepository<User_option_count, Integer>, JpaSpecificationExecutor<User_option_count> {}
