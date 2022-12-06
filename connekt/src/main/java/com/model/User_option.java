package com.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.base.User_optionPk;
import com.base.User_relation;

@Entity
@Table(name = "user_option")
@IdClass(value=User_optionPk.class)
public class User_option extends User_relation implements Serializable{
	
	// シリアル
	private static final long serialVersionUID = 1L;
	
	// コンストラクタ
	public User_option() {}
	public User_option(int store_id,int user_id,int option_id) {
		super(store_id, user_id);
		this.option_id = option_id;
	}
	
/* フィールド */
	
	// プランID
	@Id
	@Column(name="option_id")
	private int option_id;
	// オプション情報
	@OneToOne
	@JoinColumn(name="option_id", insertable = false, updatable = false)
	private Option option;
	
/* Getter・Setter */
	
	public int getOption_id() {
		return option_id;
	}
	public void setOption_id(int option_id) {
		this.option_id = option_id;
	}
	public Option getOption() {
		return option;
	}
	public void setOption(Option option) {
		this.option = option;
	}
}
