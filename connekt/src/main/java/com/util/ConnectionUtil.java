package com.util;

import java.sql.Connection;
import java.sql.DriverManager;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * コネクション汎用クラス
 */
@Service
public class ConnectionUtil {
	
	// JDBC定義
	@Value("${connection_url}") private String connection_url;
	@Value("${connection_id}") private String connection_id;
	@Value("${connection_password}") private String connection_password;
	// SSL通信可否
	@Value("${useSSL}")private String useSSL;
	// JDBC
	public static final String JDBC = "com.mysql.jdbc.Driver";
	
	/**
	 * redmine DB接続(セッションコンテキストが存在しない場合)
	 * @param contextPath コンテキストパス
	 * @return コネクション
	 * @throws Exception 例外
	 */
	public Connection connect() throws Exception{
		
		try {
			
			// JDBC登録
			//Class.forName(JDBC);
			// コネクション返却
			return DriverManager.getConnection(connection_url + useSSL , connection_id , connection_password);
			
		} catch (Exception e) {
			throw e;
		}
	}
}
