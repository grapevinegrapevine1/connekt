package com.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Range;

@Entity
@Table(name = "store")
public class Store {

	@Id
	@GeneratedValue
	private int id;
	@NotBlank
	@Email
	@Size(max=255)
	private String email;
	@NotBlank
	@Size(max=35)
	private String store_name;
	@NotBlank
	private String bank_name;
	@PositiveOrZero
	private Integer bank_code;
	@NotBlank
	private String branch_name;
	@PositiveOrZero
	private Integer branch_code;
	@Range(min=1, max=2)
	private int type;
	@PositiveOrZero
	private Long account_number;
	@NotBlank
	@Size(max=255)
	private String name;
	private String password;
	@PositiveOrZero
	private int status;
	// 設定画面アクセス認証可否
	private boolean is_cert;
	// 店舗削除可否
	private boolean is_delete;
	
	private String discount_id;
	
	private String discount_target;
	
	@Transient 
	private String password_temp;
	
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
	public String getStore_name() {
		return store_name;
	}
	public void setStore_name(String store_name) {
		this.store_name = store_name;
	}
	public String getBank_name() {
		return bank_name;
	}
	public void setBank_name(String bank_name) {
		this.bank_name = bank_name;
	}
	public Integer getBank_code() {
		return bank_code;
	}
	public void setBank_code(Integer bank_code) {
		this.bank_code = bank_code;
	}
	public String getBranch_name() {
		return branch_name;
	}
	public void setBranch_name(String branch_name) {
		this.branch_name = branch_name;
	}
	public Integer getBranch_code() {
		return branch_code;
	}
	public void setBranch_code(Integer branch_code) {
		this.branch_code = branch_code;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public Long getAccount_number() {
		return account_number;
	}
	public void setAccount_number(Long account_number) {
		this.account_number = account_number;
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
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getPassword_temp() {
		return password_temp;
	}
	public void setPassword_temp(String password_temp) {
		this.password_temp = password_temp;
	}
	public boolean getIs_cert() {
		return is_cert;
	}
	public void setIs_cert(boolean is_cert) {
		this.is_cert = is_cert;
	}
	public boolean getIs_delete() {
		return is_delete;
	}
	public void setIs_delete(boolean is_delete) {
		this.is_delete = is_delete;
	}
	public String getDiscount_id() {
		return discount_id;
	}
	public void setDiscount_id(String discount_id) {
		this.discount_id = discount_id;
	}
	public String getDiscount_target() {
		return discount_target;
	}
	public void setDiscount_target(String discount_target) {
		this.discount_target = discount_target;
	}
}
