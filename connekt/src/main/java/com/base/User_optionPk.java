package com.base;

import java.io.Serializable;
import lombok.Data;

/**
 * 基本PK指定クラス
 */
@Data
public class User_optionPk implements Serializable {
	
	// シリアル
	private static final long serialVersionUID = 1L;
	
	// 店舗ID
	public int store_id;
	// ユーザーID
	public int user_id;
	// オプションID
	public int option_id;
}