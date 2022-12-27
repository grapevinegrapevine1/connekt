package com.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.model.Store;
import com.repository.StoreRepository;
import com.specification.CommonSpecification;

@Service
public class StoreService {

	@Autowired	StoreRepository repository;

	public Store find(int id) {
		CommonSpecification<Store> spec = new CommonSpecification<>();
		return repository.findOne(spec.equal("id", id).and(spec.equal("is_delete", false))).orElse(null);
	}
	
	public Store findWithDelete(int id) {
		CommonSpecification<Store> spec = new CommonSpecification<>();
		return repository.findOne(spec.equal("id", id)).orElse(null);
	}


	public Store containByEmailAndName(String email, String store_name) {
		CommonSpecification<Store> spec = new CommonSpecification<>();
		return repository.findOne(spec.equal("email", email)
									.and(spec.equal("store_name", store_name))).orElse(null);
	}
	
	public Store contain(String email) {
		CommonSpecification<Store> spec = new CommonSpecification<>();
		return repository.findOne(spec.equal("email", email).and(spec.equal("is_delete", false))).orElse(null);
	}
	
	public boolean existDiscount_target(String discount_target) {
		CommonSpecification<Store> spec = new CommonSpecification<>();
		return repository.findOne(spec.equal("discount_id", discount_target)).orElse(null) != null;
	}
	
	public List<Store> countDiscount_target(String discount_target) {
		CommonSpecification<Store> spec = new CommonSpecification<>();
		return repository.findAll(spec.equal("discount_target", discount_target));
	}
	
	public List<Store> find_all() {
		return repository.findAll();
	}

	public Store save(Store entity) {
		return repository.save(entity);
	}

	public void delete(Integer id) {
		repository.deleteById(id);
	}
}
