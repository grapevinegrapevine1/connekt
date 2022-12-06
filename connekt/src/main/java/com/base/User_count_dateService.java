package com.base;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * ユーザー利用日基本クラス
 */
@Transactional
public class User_count_dateService<T> {
	
	// 基本JPA
	@Autowired JpaRepository<T, Integer> jpaRepository;
	// カスタムJPA
	@Autowired JpaSpecificationExecutor<T> jpaSpecificationExecutor;
	
	// 列名
	private String col;
	
	// コンストラクタ
	protected User_count_dateService(String col){
		this.col = col;
	}
	
	// 利用数IDで取得
	public List<T> find_list(int user_count_id) {
		return jpaSpecificationExecutor.findAll(contains("user_"+col+"_count_id",user_count_id));
	}
	
	/**
	 * フィルター
	 */
	private Specification<T> contains(String col, int val) {
		return (root, query, cb) -> {
			return cb.equal(root.get(col), val);
		};
	}
	
	/**
	 * 保存
	 */
	public T save(T entity) {
		return jpaRepository.save(entity);
	}
}
