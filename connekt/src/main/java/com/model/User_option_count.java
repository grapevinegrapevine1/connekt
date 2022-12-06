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
import com.pk.User_option_countPk;

@Entity
@Table(name = "user_option_count")
@IdClass(value=User_option_countPk.class)
public class User_option_count extends User_count implements Serializable{
	
	// シリアル
	private static final long serialVersionUID = 1L;
	
	// オプションID
	@Id
	@Column(name="option_id")
	private int option_id;
	// オプション情報
	@OneToOne
	@JoinColumn(name="option_id", insertable = false, updatable = false)
	private Option option;
	@Transient
	private List<User_option_count_date> user_option_count_data;
	
	// Getter・Setter
	public int getOption_id() {
		return option_id;
	}
	public void setOption_id(int option_id) {
		this.option_id = option_id;
	}
	public Option getOption() {
		return option;
	}
	public int getBaseCount() {
		return option.getCount();
	}
	public void setOption(Option option) {
		this.option = option;
	}
	public List<User_option_count_date> getUser_option_count_data() {
		return user_option_count_data;
	}
	public void setUser_option_count_data(List<User_option_count_date> user_option_count_data) {
		this.user_option_count_data = user_option_count_data;
	}
	public int getCount() {
		return user_option_count_data != null ? user_option_count_data.size() : 0;
	}
	@Override
	public List<Date> getUseDates() {
		if(user_option_count_data != null) return user_option_count_data.stream().map(b->b.getUse_date()).collect(Collectors.toList());
		else return null;
	}
}
