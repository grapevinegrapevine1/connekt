package com.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.base.User_planPk;
import com.base.User_relation;

@Entity
@Table(name = "user_plan")
@IdClass(value=User_planPk.class)
public class User_plan extends User_relation implements Serializable{
	
	// シリアル
	private static final long serialVersionUID = 1L;
	
	// コンストラクタ
	public User_plan() {}
	public User_plan(int store_id,int user_id,int plan_id) {
		super(store_id, user_id);
		this.plan_id = plan_id;
	}
	
/* フィールド */
	
	// プランID
	@Id
	@Column(name="plan_id")
	private int plan_id;
	// プラン情報
	@OneToOne
	@JoinColumn(name="plan_id", insertable = false, updatable = false)
	private Plan plan;
	
/* Getter・Setter */
	
	public int getPlan_id() {
		return plan_id;
	}
	public void setPlan_id(int plan_id) {
		this.plan_id = plan_id;
	}
	public Plan getPlan() {
		return plan;
	}
	public void setPlan(Plan plan) {
		this.plan = plan;
	}
}
