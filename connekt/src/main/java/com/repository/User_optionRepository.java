package com.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.model.User_option;

public interface User_optionRepository extends JpaRepository<User_option, Integer>, JpaSpecificationExecutor<User_option> {}
