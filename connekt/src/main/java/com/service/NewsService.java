package com.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.model.News;
import com.repository.NewsRepository;
import com.specification.CommonSpecification;

@Service
public class NewsService {

	@Autowired	NewsRepository repository;

	public List<News> findByStoreId(int store_id) {
		CommonSpecification<News> spec = new CommonSpecification<>();
		return repository.findAll(spec.equal("store_id", store_id));
	}

	public News save(News entity) {
		return repository.save(entity);
	}

	public void delete(int id) {
		repository.deleteById(id);
	}
}
