package com.base;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

/**
 * 利用数基本クラス
 */
@MappedSuperclass
public abstract class User_count implements Serializable{
	
	// シリアル
	private static final long serialVersionUID = 1L;
	
/* フィールド */

	// ID
	@Id
	@Column(name="id")
	private int id;
	// ID
	@Id
	@Column(name="user_id")
	private int user_id;
	// 開始日
	@Column(name="start_date")
	private long start_date;
	// 終了日
	@Column(name="end_date")
	private long end_date;
	
	// 利用上限可否
	@Transient
	private boolean is_limit;
	
/* Getter・Setter */
	
	public int getUser_id() {
		return user_id;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public void setUser_id(int user_id) {
		this.user_id = user_id;
	}
	public long getStart_date() {
		return start_date;
	}
	public void setStart_date(long start_date) {
		this.start_date = start_date;
	}
	public long getEnd_date() {
		return end_date;
	}
	public void setEnd_date(long end_date) {
		this.end_date = end_date;
	}
	public abstract int getBaseCount();
	public abstract int getCount();
	public abstract List<Date> getUseDates();
}
