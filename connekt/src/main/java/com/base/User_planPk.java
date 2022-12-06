package com.base;

import java.io.Serializable;
import lombok.Data;

/**
 * 基本PK指定クラス
 */
@Data
public class User_planPk implements Serializable {
	
	// シリアル
	private static final long serialVersionUID = 1L;
	
	// 店舗ID
	public int store_id;
	// ユーザーID
	public int user_id;
	// プランID
	public int plan_id;
}