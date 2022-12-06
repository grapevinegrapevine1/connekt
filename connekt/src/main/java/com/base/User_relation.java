package com.base;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.model.User;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class User_relation implements Serializable{
	
	// シリアル
	private static final long serialVersionUID = 1L;
	
	// コンストラクタ
	public User_relation() {}
	public User_relation(int store_id,int user_id) {
		setStore_id(store_id);
		setUser_id(user_id);
	}
	
/* フィールド */
	
	// 店舗ＩＤ
	@Id
	@Column(name="store_id")
	private int store_id;
	// ユーザーＩＤ
	@Id
	@Column(name="user_id")
	private int user_id;
	// StripeサブスクＩＤ
	@Column(name="stripe_subscription_id")
	private String stripe_subscription_id;
	// 作成日
	@CreatedDate
	private LocalDateTime created_date;
	
	// 契約詳細テキスト
	@Transient
	private String contract_text;
	// 利用数状況テキスト
	@Transient
	private String count_text;
	// 利用数情報
	@Transient
	private User_count user_count;
	// 利用可否(利用数上限に達しているかどうか)
	@Transient
	private boolean is_limit;
	// 利用数情報
	@Transient
	private boolean stripe_cancel_at_period_end;
	// 更新日(str)
	@Transient
	private String period_end_str;
	// 更新日
	@Transient
	private long period_end;
	// 期間内可否(true:期間内 / false:期間外)
	@Transient
	private boolean within_date;
	// 返品可否
	@Transient
	private boolean is_refund;
	// 決済エラー可否(true:エラー)
	@Transient
	private boolean error_user_pay;
	
	// ユーザー情報
	@OneToOne
	@JoinColumn(name="user_id", insertable = false, updatable = false)
	private User user;
	
/* Getter・Setter */
	
	public int getStore_id() {
		return store_id;
	}
	public void setStore_id(int store_id) {
		this.store_id = store_id;
	}
	public int getUser_id() {
		return user_id;
	}
	public void setUser_id(int user_id) {
		this.user_id = user_id;
	}
	public String getContract_text() {
		return contract_text;
	}
	public void setContract_text(String contract_text) {
		this.contract_text = contract_text;
	}
	public String getStripe_subscription_id() {
		return stripe_subscription_id;
	}
	public void setStripe_subscription_id(String stripe_subscription_id) {
		this.stripe_subscription_id = stripe_subscription_id;
	}
	public String getCount_text() {
		return count_text;
	}
	public void setCount_text(String count_text) {
		this.count_text = count_text;
	}
	public User_count getUser_count() {
		return user_count;
	}
	public void setUser_count(User_count user_count) {
		this.user_count = user_count;
	}
	public boolean getIs_limit() {
		return is_limit;
	}
	public void setIs_limit(boolean is_limit) {
		this.is_limit = is_limit;
	}
	public boolean getStripe_cancel_at_period_end() {
		return stripe_cancel_at_period_end;
	}
	public void setStripe_cancel_at_period_end(boolean stripe_cancel_at_period_end) {
		this.stripe_cancel_at_period_end = stripe_cancel_at_period_end;
	}
	public String getPeriod_end_str() {
		return period_end_str;
	}
	public void setPeriod_end_str(String period_end_str) {
		this.period_end_str = period_end_str;
	}
	public long getPeriod_end() {
		return period_end;
	}
	public void setPeriod_end(long period_end) {
		this.period_end = period_end;
	}
	public boolean getWithin_date() {
		return within_date;
	}
	public void setWithin_date(boolean within_date) {
		this.within_date = within_date;
	}
	public LocalDateTime getCreated_date() {
		return created_date;
	}
	public void setCreated_date(LocalDateTime created_date) {
		this.created_date = created_date;
	}
	public boolean getIs_refund() {
		return is_refund;
	}
	public void setIs_refund(boolean is_refund) {
		this.is_refund = is_refund;
	}
	public boolean getError_user_pay() {
		return error_user_pay;
	}
	public void setError_user_pay(boolean error_user_pay) {
		this.error_user_pay = error_user_pay;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
}
