package com.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.model.Non_app_id;


public interface Non_app_idRepository extends JpaRepository<Non_app_id, Integer>, JpaSpecificationExecutor<Non_app_id> {}