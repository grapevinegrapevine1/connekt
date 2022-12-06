package com.security;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.model.Store;
import com.model.User;
import com.properties.Const;
import com.service.StoreService;
import com.service.UserService;
import com.util.CommonUtil;

/**
 * Spring Securityのユーザ検索用のサービスの実装クラス DataSourceの引数として指定することで認証にDBを利用できるようになる
 */
@Component
public class UserDetailsServiceImpl implements UserDetailsService {
	
	@Autowired private HttpServletRequest req;
	@Autowired private HttpSession ses;
	@Autowired private UserService userService;
	@Autowired private StoreService storeService;
	
	/**
	 * フォームから取得したメールアドレスでDBを検索し、合致するものが存在したとき、 パスワード、権限情報と共にUserDetailsオブジェクトを生成
	 * コンフィグクラスで上入力値とDBから取得したパスワードと比較し、ログイン判定を行う
	 */
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		
		// フォームフラグ
		String isUserForm = req.getParameter(Const.IS_USER_FORM_NM);
		ses.setAttribute(Const.IS_USER_FORM_NM, isUserForm);
		
		// パスワード
		String password;
		// ユーザーフォームである場合
		if(isUserForm.equals(Const.IS_USER_FORM)) {
			
			// ユーザー情報取得
			User user = userService.contain(email);
			// パスワード設定
			password = user.getPassword();
			// セッション設定
			CommonUtil.setSessionUser(ses, user);
			// セッション店舗情報削除
			CommonUtil.removeSessionStore(ses);
			
		// 店舗フォームである場合
		}else {
			
			// 店舗情報取得
			Store store = storeService.contain(email);
			// パスワード設定
			password = store.getPassword();
			// セッション設定
			CommonUtil.setSessionStore(ses, store);
			// セッションユーザー情報削除
			CommonUtil.removeSessionUser(ses);
		}
		
		// 権限リスト
		List<GrantedAuthority> grantList = new ArrayList<GrantedAuthority>();
		// 権限生成
		GrantedAuthority authority = new SimpleGrantedAuthority("USER");
		// 権限追加
		grantList.add(authority);
		
		// エンコーダー
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		// ユーザー情報
		LoginForm userDetails = new LoginForm(email, encoder.encode(password),grantList);
		
		// 返却
		return userDetails;
	}
}
