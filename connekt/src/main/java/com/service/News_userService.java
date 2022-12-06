package com.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.model.News_user;
import com.repository.News_userRepository;
import com.specification.CommonSpecification;

@Service
public class News_userService {

	@Autowired	News_userRepository repository;

	public List<News_user> findByUserId(int user_id) {
		CommonSpecification<News_user> spec = new CommonSpecification<>();
		return repository.findAll(spec.equal("user_id", user_id));
	}

	public News_user save(News_user entity) {
		return repository.save(entity);
	}

	public void delete(int id) {
		repository.deleteById(id);
	}
}
