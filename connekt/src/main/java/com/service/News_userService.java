package com.service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
		return repository.findAll(spec.equal("user_id", user_id)).stream()
			.sorted(Comparator.comparing(News_user::getNews_id, Comparator.reverseOrder()))
			.collect(Collectors.toList());
	}

	public News_user save(News_user entity) {
		return repository.save(entity);
	}

	public void delete(int id) {
		repository.deleteById(id);
	}
}
