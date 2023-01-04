package com.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.properties.Const;

/**
 * SpringSecurity設定
 */
@Configuration
@EnableWebSecurity
@EnableJpaAuditing
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	
	// フォームの値と比較するDBから取得したパスワードは暗号化されているのでフォームの値も暗号化するために利用
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
		return bCryptPasswordEncoder;
	}
	
	/**
	 * 認証許可ファイル設定
	 */
	@Override
	public void configure(WebSecurity web) throws Exception {
		// 認証を許可するファイルパス
		web.ignoring().antMatchers("/images/**", "/css/**", "/javascript/**", "/json/**",
									"/login_with_storeId", "/login_with_userId",
									"/user_create", "/create_user", "/store_create", "/create_store",
									"/user_valid_error", "/store_valid_error", "/user_forget", "/user_forget_request", "/store_forget",
									"/store_forget_request","/"+Const.PAGE_USER_FORGET_SEND, "/"+Const.PAGE_STORE_FORGET_SEND,
									"/cert_user", "/cert_store", "/user_recert", "/store_recert", "/user_recert_input", "/store_recert_input",
									"/cert_start");
	}
	
	/**
	 * 認証・認可の情報を設定する 画面遷移のURL・パラメータを取得するname属性の値を設定
	 * SpringSecurityのconfigureメソッドをオーバーライドしています。
	 */
	@Override
	protected void configure(HttpSecurity security) throws Exception {
		
		// セキュリティ設定
		security
			// アクセス権限設定
			.authorizeRequests()
			// 認証許可されてないリクエストは認証必須
			.anyRequest().authenticated().and()
			
			// ログイン設定
			.formLogin()
			// ログインページの指定
			.loginPage("/login")
			// ここで指定したURLでのサブミットのみ、認証処理を許可
			.loginProcessingUrl("/login")
			// リクエストパラメータのname属性を明示
			.usernameParameter("email").passwordParameter("password")
			// 認証成功時の遷移先ページ
			.defaultSuccessUrl("/login_foward", true)
			// 認証失敗時の遷移先ページ
			.failureUrl("/login_error")
			// loginPageで指定したページへのアクセス許可(誰でもアクセス可)
			.permitAll().and()
			
			// ログアウト設定
			.logout()
			// ログアウト処理URL
			.logoutUrl("/logout")
			// ログアウト後の遷移先ページ
			.logoutSuccessUrl("/login?logout")
			// logoutUrlで指定したページへのアクセス許可(誰でもアクセス可)
			.permitAll().and()
			
			// csrf無効
			.csrf().disable();
	}
}
