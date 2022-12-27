package com.base;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Range;

import com.model.Store;

/**
 * 基本クラス
 */
@MappedSuperclass
public class BaseModel {
	
/* フィールド */
	
	// ID
	@Id
	@GeneratedValue
	private int id;
	// 店舗ID
	@Positive
	private int store_id;
	// 店舗名
	@NotBlank
	@Size(max=100)
	private String name;
	// 料金
	@PositiveOrZero
	@Range(min=0, max=999999)
	private int price;
	// Stripe商品ID
	private String stripe_plan_id;
	// 説明
	@Size(max=255)
	private String description;
	// 利用可能数
	@Range(min=1, max=99)
	private int count;
	// 請求期間
	@Range(min=1, max=12)
	private int plan_interval;
	// 削除可否
	@Range(min=0, max=1)
	private int is_delete;
	
	// プラン追加時フラグ
	private boolean is_create;
	
	// 店舗情報
	@OneToOne
	@JoinColumn(name="store_id", insertable = false, updatable = false)
	Store store;
	
/* Getter・Setter */
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getStore_id() {
		return store_id;
	}
	public void setStore_id(int store_id) {
		this.store_id = store_id;
	}
	public int getPrice() {
		return price;
	}
	public void setPrice(int price) {
		this.price = price;
	}
	public String getStripe_plan_id() {
		return stripe_plan_id;
	}
	public void setStripe_plan_id(String stripe_plan_id) {
		this.stripe_plan_id = stripe_plan_id;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public Store getStore() {
		return store;
	}
	public void setStore(Store store) {
		this.store = store;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getPlan_interval() {
		return plan_interval;
	}
	public void setPlan_interval(int plan_interval) {
		this.plan_interval = plan_interval;
	}
	public int getIs_delete() {
		return is_delete;
	}
	public void setIs_delete(int is_delete) {
		this.is_delete = is_delete;
	}
	public boolean getIs_create() {
		return is_create;
	}
	public void setIs_create(boolean is_create) {
		this.is_create = is_create;
	}
}
