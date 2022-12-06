package com.service;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.model.Non_app_id;
import com.repository.Non_app_idRepository;

@Service
@Transactional
public class Non_app_idService {

	@Autowired Non_app_idRepository repository;

	public Non_app_id createId() {
		return repository.save(new Non_app_id());
	}

	public void delete(int id) {
		repository.deleteById(id);
	}
}