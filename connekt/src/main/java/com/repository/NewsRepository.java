package com.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.model.News;

public interface NewsRepository extends JpaRepository<News, Integer>, JpaSpecificationExecutor<News> {}
