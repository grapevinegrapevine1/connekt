package com.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.base.User_count;
import com.pk.User_plan_countPk;

@Entity
@Table(name = "user_plan_count")
@IdClass(value=User_plan_countPk.class)
public class User_plan_count extends User_count implements Serializable{
	
	// シリアル
	private static final long serialVersionUID = 1L;
	
	// プランID
	@Id
	@Column(name="plan_id")
	private int plan_id;
	// プラン情報
	@OneToOne
	@JoinColumn(name="plan_id", insertable = false, updatable = false)
	private Plan plan;
	// 利用日リスト
	@Transient
	private List<User_plan_count_date> user_plan_count_data;
	
	// Getter・Setter
	public int getPlan_id() {
		return plan_id;
	}
	public void setPlan_id(int plan_id) {
		this.plan_id = plan_id;
	}
	public Plan getPlan() {
		return plan;
	}
	public int getBaseCount() {
		return plan.getCount();
	}
	public void setPlan(Plan plan) {
		this.plan = plan;
	}
	public List<User_plan_count_date> getUser_plan_count_data() {
		return user_plan_count_data;
	}
	public void setUser_plan_count_data(List<User_plan_count_date> user_plan_count_data) {
		this.user_plan_count_data = user_plan_count_data;
	}
	public int getCount() {
		return user_plan_count_data != null ? user_plan_count_data.size() : 0;
	}
	@Override
	public List<Date> getUseDates() {
		if(user_plan_count_data != null) return user_plan_count_data.stream().map(b->b.getUse_date()).collect(Collectors.toList());
		else return null;
	}
}
