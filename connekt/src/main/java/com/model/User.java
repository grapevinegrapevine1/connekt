package com.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;

@Entity
@Table(name = "user")
public class User {

	@Id
	@GeneratedValue
	private int id;
	@NotBlank
	private String email;
	@NotBlank
	private String name;
	private String password;
	private String stripe_customer_id;
	private String tel;
	private Date birth;
	// 0:認証前 / 1:認証済 / 2:削除済
	@PositiveOrZero
	private int status;
	
	@Transient
	private String password_temp;
	
	/*
	// クレジットカード番号
	@Transient
	private String cc_number;
	// クレジットカード有効期限
	@Transient
	private String cc_exe;
	// クレジットカードセキュリティコード
	@Transient
	private String cc_csc;
	*/
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getStripe_customer_id() {
		return stripe_customer_id;
	}
	public void setStripe_customer_id(String stripe_customer_id) {
		this.stripe_customer_id = stripe_customer_id;
	}
	public String getPassword_temp() {
		return password_temp;
	}
	public void setPassword_temp(String password_temp) {
		this.password_temp = password_temp;
	}
	public String getTel() {
		return tel;
	}
	public void setTel(String tel) {
		this.tel = tel;
	}
	public Date getBirth() {
		return birth;
	}
	public void setBirth(Date birth) {
		this.birth = birth;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
}
