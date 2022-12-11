package com.ajax.response;

public class AjaxSalesResponse {
	// 一覧データ
	private Object list;
	// 頁最終データID
	private String last;
	
	public Object getList() {
		return list;
	}
	public void setList(Object list) {
		this.list = list;
	}
	public String getLast() {
		return last;
	}
	public void setLast(String last) {
		this.last = last;
	}
}
