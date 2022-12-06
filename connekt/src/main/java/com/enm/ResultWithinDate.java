package com.enm;

/**
 * 利用期間チェック結果
 */
public enum ResultWithinDate{
	NOT_ERR(0),
	ERR_PRE(1),
	ERR_AFT(2);
	int id;
	private ResultWithinDate(int id) {
		this.id = id;
	}
}
