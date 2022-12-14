package com.form;

import java.util.Date;

public class StoreSalesListForm {
	
	// 支払日
	private Date use_date;
	// 金額
	private long price;
	// 手数料
	private long balance;
	// プラン名
	private String name;
	// ユーザー名
	private String user_name;
	// メール
	private String email;
	// 頁最後の支払情報ID
	private String last_ch;
	// 頁最後の返金情報ID
	private String last_re;
	
	public Date getUse_date() {
		return use_date;
	}
	public void setUse_date(Date use_date) {
		this.use_date = use_date;
	}
	public long getPrice() {
		return price;
	}
	public void setPrice(long price) {
		this.price = price;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUser_name() {
		return user_name;
	}
	public void setUser_name(String user_name) {
		this.user_name = user_name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public long getBalance() {
		return balance;
	}
	public void setBalance(long balance) {
		this.balance = balance;
	}
	public String getLast_ch() {
		return last_ch;
	}
	public void setLast_ch(String last_ch) {
		this.last_ch = last_ch;
	}
	public String getLast_re() {
		return last_re;
	}
	public void setLast_re(String last_re) {
		this.last_re = last_re;
	}
}
