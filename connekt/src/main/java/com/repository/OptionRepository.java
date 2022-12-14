package com.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.model.Option;

public interface OptionRepository extends JpaRepository<Option, Integer>, JpaSpecificationExecutor<Option> {}
