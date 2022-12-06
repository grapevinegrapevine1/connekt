package com.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import com.base.User_count_date;
import com.pk.User_plan_count_datePk;

@Entity
@Table(name = "user_plan_count_date")
@IdClass(value=User_plan_count_datePk.class)
public class User_plan_count_date extends User_count_date implements Serializable{
	
	// シリアル
	private static final long serialVersionUID = 1L;
	
	// プランID
	@Id
	@Column(name="user_plan_count_id")
	private int user_plan_count_id;
	
	// Getter・Setter
	public int getUser_plan_count_id() {
		return user_plan_count_id;
	}
	public void setUser_plan_count_id(int user_plan_count_id) {
		this.user_plan_count_id = user_plan_count_id;
	}
}
