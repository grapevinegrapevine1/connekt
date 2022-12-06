package com.base;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.specification.CommonSpecification;

/**
 * ユーザー利用数基本クラス
 */
@Transactional
public class User_countService<T> {
	
	// 基本JPA
	@Autowired JpaRepository<T, Integer> jpaRepository;
	// カスタムJPA
	@Autowired JpaSpecificationExecutor<T> jpaSpecificationExecutor;
	
	// 列名
	private String col;
	
	// コンストラクタ
	protected User_countService(String col){
		this.col = col;
	}
	
	/**
	 * ユーザーID・プランID・開始日・終了日で取得
	 */
	protected T contain(int user_id, int id,long start_date, long end_date) {
		CommonSpecification<T> spec = new CommonSpecification<>();
		return jpaSpecificationExecutor.findOne(spec.equal("user_id", user_id)
				.and(spec.equal(col, id))
				.and(spec.equal("start_date", start_date))
				.and(spec.equal("end_date", end_date))).orElse(null);
	}
	
	/**
	 * 保存
	 */
	public T save(T entity) {
		return jpaRepository.save(entity);
	}
}
