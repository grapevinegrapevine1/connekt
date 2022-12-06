package com.base;

import java.util.List;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.specification.CommonSpecification;

@Transactional
public class User_relationService<T> {
	
	// 基本JPA
	@Autowired private JpaRepository<T, Integer> jpaRepository;
	// カスタムJPA
	@Autowired private JpaSpecificationExecutor<T> jpaSpecificationExecutor;;
	
	public List<T> find_list(int user_id) {
		return jpaSpecificationExecutor.findAll(contains("user_id",user_id));
	}

	public T contain(int store_id, int user_id) {
		CommonSpecification<T> spec = new CommonSpecification<>();
		return jpaSpecificationExecutor.findOne(spec.equal("store_id", store_id).and(spec.equal("user_id", user_id))).orElse(null);
	}
	
	protected T containByPlanId(String col, int store_id, int user_id, int plan_id) {
		CommonSpecification<T> spec = new CommonSpecification<>();
		return jpaSpecificationExecutor.findOne(spec.equal("store_id", store_id).and(spec.equal("user_id", user_id)).and(spec.equal(col + "_id", plan_id))).orElse(null);
	}
	
	public List<T> containByUserId(int store_id, int user_id) {
		CommonSpecification<T> spec = new CommonSpecification<>();
		return jpaSpecificationExecutor.findAll(spec.equal("store_id", store_id).and(spec.equal("user_id", user_id)));
	}
	
	public List<T> containByStoreId(int store_id) {
		CommonSpecification<T> spec = new CommonSpecification<>();
		return jpaSpecificationExecutor.findAll(spec.equal("store_id", store_id));
	}
	
	public T containBySubscriptionId(String stripe_subscription_id) {
		CommonSpecification<T> spec = new CommonSpecification<>();
		return jpaSpecificationExecutor.findOne(spec.equal("stripe_subscription_id", stripe_subscription_id)).orElse(null);
	}
	
	private Specification<T> contains(String col, int val) {
		return (root, query, cb) -> {
			return cb.equal(root.get(col), val);
		};
	}
	
	public T saveOne(T entity) {
		return jpaRepository.save(entity);
	}

	public List<T> saveAll(List<T> entity) {
		return jpaRepository.saveAll(entity);
	}

	public void delete(T entity) {
		jpaRepository.delete(entity);
	}
	
	protected void deleteAll(String tableNm, int store_id, int user_id) {
		//jpaRepository.deleteByStoreIdAndUserId(tableNm, store_id, user_id);
	}
}
