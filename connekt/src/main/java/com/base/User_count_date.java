package com.base;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * 利用数基本クラス
 */
@MappedSuperclass
public abstract class User_count_date implements Serializable{
	
	// シリアル
	private static final long serialVersionUID = 1L;
	
/* フィールド */

	// ID
	@Column(name="use_date")
	private Date use_date;
	
/* Getter・Setter */
	
	public Date getUse_date() {
		return use_date;
	}
	public void setUse_date(Date use_date) {
		this.use_date = use_date;
	}
}
