package com.base;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.specification.CommonSpecification;

/**
 * サービス基本クラス
 */
@Transactional
public class BaseService<T> {

	// 基本JPA
	@Autowired JpaRepository<T, Integer> jpaRepository;
	// カスタムJPA
	@Autowired JpaSpecificationExecutor<T> jpaSpecificationExecutor;
	
	private static final int NOT_EXIST = 0;
	
	/**
	 * IDで取得
	 */
	public T find(int id) {
		CommonSpecification<T> spec = new CommonSpecification<>();
		return jpaSpecificationExecutor.findOne(spec.equal("id", id).and(spec.equal("is_delete", NOT_EXIST))).orElse(null);
	}
	public T findWithDeleted(int id) {
		CommonSpecification<T> spec = new CommonSpecification<>();
		return jpaSpecificationExecutor.findOne(spec.equal("id", id)).orElse(null);
	}
	
	/**
	 * 店舗IDで取得
	 */
	public List<T> find_list(int store_id) {
		return jpaSpecificationExecutor.findAll(contains("store_id",store_id).and(contains("is_delete", NOT_EXIST)));
	}
	
	/**
	 * Stripe商品IDで取得
	 */
	public T containByStripePlanId(String stripe_plan_id) {
		CommonSpecification<T> spec = new CommonSpecification<>();
		return jpaSpecificationExecutor.findOne(spec.equal("stripe_plan_id", stripe_plan_id)).orElse(null);
	}
	
	/**
	 * 保存
	 */
	public T save(T entity) {
		return jpaRepository.save(entity);
	}
	
	/**
	 * IDで削除
	 
	public void delete(int id) {
		jpaRepository.deleteById(id);
	}*/
	public void delete(int id) {
		T entity = find(id);
		((BaseModel) entity).setIs_delete(1);
		jpaRepository.save(entity);
	}
	
	/**
	 * フィルター
	 */
	private Specification<T> contains(String col, int store_id) {
		return (root, query, cb) -> {
			return cb.equal(root.get(col), store_id);
		};
	}
}
