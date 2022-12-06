package com.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.model.News_user;

public interface News_userRepository extends JpaRepository<News_user, Integer>, JpaSpecificationExecutor<News_user> {}
