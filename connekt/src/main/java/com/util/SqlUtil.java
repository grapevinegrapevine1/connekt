package com.util;

import java.sql.PreparedStatement;
import java.util.ArrayList;

/**
 * SQL汎用クラス
 */
public class SqlUtil {
	
	/**
	 * SQLパラメータ設定
	 * @param ps PreparedStatement
	 * @param param パラメータ
	 * @throws Exception 例外
	 */
	public void setParam(PreparedStatement ps , ArrayList<Object> params) throws Exception{
		setParam(ps, params.toArray(new Object[params.size()]));
	}
	
	/**
	 * SQLパラメータ設定
	 * @param ps PreparedStatement
	 * @param param パラメータ
	 * @throws Exception 例外
	 */
	public void setParam(PreparedStatement ps , Object[] params) throws Exception{
		
		try{
			
			// パラメータ数分繰り返す
			for( int i = 0; i < params.length; i++ ) {
				
				// 設定(空文字はnullに変換)
				ps.setObject( i+1 , ( params[i] == null || params[i].equals("") ) ? null : params[i] );
			}
			
		}catch(Exception e){
			throw e;
		}
	}
}