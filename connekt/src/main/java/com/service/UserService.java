package com.service;

import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import com.model.User;
import com.repository.UserRepository;
import com.specification.CommonSpecification;

@Service
@Transactional
@Repository
@EntityScan(basePackageClasses = User.class)
public class UserService {

	@Autowired UserRepository repository;
	
	public User find(int id) {
		return repository.findById(id).orElse(null);
	}
	
	public User contain(String email) {
		CommonSpecification<User> spec = new CommonSpecification<>();
		return repository.findOne(spec.equal("email", email)).orElse(null);
	}

	public User findByStripeCustomerId(String stripe_customer_id) {
		CommonSpecification<User> spec = new CommonSpecification<>();
		return repository.findOne(spec.equal("stripe_customer_id", stripe_customer_id)).orElse(null);
	}
	
	public List<User> contains(String email) {
		CommonSpecification<User> spec = new CommonSpecification<>();
		return repository.findAll(spec.equal("email", email));
	}
	
	public User containByEmailAndName(String email, String name) {
		CommonSpecification<User> spec = new CommonSpecification<>();
		return repository.findOne(spec.equal("email", email).
									and(spec.equal("name", name))).orElse(null);
	}

	public User findNonAppUser(String name, String tel, Date birth) {
		CommonSpecification<User> spec = new CommonSpecification<>();
		return repository.findOne(spec.equal("name", name).
									and(spec.equal("tel", tel)).
									and(spec.equal("birth", birth))).orElse(null);
	}
	
	public List<User> find_all() {
		return repository.findAll();
	}

	public User save(User entity) {
		return repository.save(entity);
	}

	public void delete(Integer id) {
		repository.deleteById(id);
	}
}
