package com.service;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import com.form.ForgetForm;
import com.model.Store;
import com.model.User;
import com.properties.Const;
import com.security.LoginController;
import com.util.CommonUtil;
import com.util.MailUtil;

@Controller
public class CertUtil {

	@Autowired MailUtil mailUtil;
	@Autowired UserService user_service;
	@Autowired StoreService store_service;
	
	/**
	 * 認証メール送信
	 */
	public void sendCertMail(int id, String email, HttpServletRequest req, boolean isUser) throws Exception {
		
		// 認証メール送信
		certMailSend(id, email, req, isUser);
		
		// メッセージ
		List<String> info_messages = new ArrayList<String>();
		info_messages.add("入力されたメールアドレスへ認証メールを送信しました。送信されたメールに記載されているURLから認証を行って下さい。認証後からログインできるようになります。");
		
		// リクエスト
		req.setAttribute(Const.MSG_INFO, info_messages);
	}
	/**
	 * 認証メール再送信
	 */
	public void resendCertMail(int id, String email, HttpServletRequest req, boolean isUser) throws Exception {
		
		// 認証メール送信
		certMailSend(id,email, req, isUser);
		
		// メッセージ
		List<String> info_messages = new ArrayList<String>();
		String msgPart = isUser ? "ユーザー" : "店舗";
		info_messages.add("この"+msgPart+"は以前登録されており、認証が完了しておりません。入力されたメールアドレスへ認証メールを再送信しました。送信されたメールに記載されているURLから認証を行って下さい。認証後は以前登録したパスワードでログインすることができます。");
		
		// リクエスト
		req.setAttribute(Const.MSG_INFO, info_messages);
	}
	
	public String recertInput(boolean is_user, Model model) {

		// リクエスト
		model.addAttribute("is_user", is_user);
		model.addAttribute("forgetForm", new ForgetForm());
		// 遷移
		return Const.PAGE_COMMON_RECERT;
	}
	
	/**
	 * 認証メール送信
	 */
	private void certMailSend(int id, String email, HttpServletRequest req, boolean isUser) throws Exception {
		
		// サーブレットURL
		String servletUrl = isUser ? "cert_user" : "cert_store";
		
		// ポート
		String urlPort = !req.getScheme().equals("http") ? "" : ":" + req.getServerPort();
		// 認証URL
		String url = req.getScheme() + "://" + req.getServerName() + urlPort + req.getContextPath() + "/"+servletUrl+"?cert=" + CommonUtil.encrypt(id+ "_" + email);
		
		// 認証メール送信
		mailUtil.sendUserCertMail(email, url);
	}
	/**
	 * 認証メール再送信
	 */
	public String recert(ForgetForm forgetForm, BindingResult bindingResult, Model model, HttpServletRequest req, boolean isUser) throws Exception {
		
		// Validationエラーメッセージ取得
		String validMsg = CommonUtil.isValid(bindingResult,req);
		// Validationエラーが存在する場合はエラー画面遷移
		if(validMsg != null) return Const.PAGE_STORE_VALID_ERROR;
		
		// 店舗
		Object obj = null;
		// ステータス、ID
		int status = -1, objId = -1	;
		
		// ユーザーである場合
		if(isUser) {
			// ユーザー
			User user = user_service.contain(forgetForm.getEmail());
			// ユーザーが存在しない場合
			if(user != null) {
				// 変数設定
				objId = user.getId();
				status = user.getStatus();
				obj = user;
			}
		// 店舗である場合
		}else {
			// 店舗
			Store store = store_service.contain(forgetForm.getEmail());
			// 店舗が存在しない場合
			if(store != null) {
				// 設定
				objId = store.getId();
				status = store.getStatus();
				obj = store;
			}
		}
		
		// ユーザー/店舗が存在しない場合
		if(obj == null) {

			// エラーメッセージ
			List<String> error_messages = new ArrayList<String>();
			error_messages.add("入力されたメールアドレスは登録されていません。ログイン画面から新規登録してください。");
			// リクエスト
			req.setAttribute(Const.MSG_ERROR, error_messages);
			
			// 遷移
			return LoginController.dispLogin(model, req, isUser);
			
		// ユーザー/店舗が存在する場合
		}else {

			// 認証前である場合
			if(status == Const.USER_STATUS_DEAULT || status == Const.USER_STATUS_CERT_START) {
				
				// 認証メール送信
				resendCertMail(objId, forgetForm.getEmail(), req, isUser);
				// 遷移
				return LoginController.dispLogin(model, req, isUser);
			
			// 認証済である場合
			}else {
				
				// エラーメッセージ
				List<String> error_messages = new ArrayList<String>();
				error_messages.add("入力されたメールアドレスは既に認証が完了しています。ログイン画面からログインしてください。");
				// リクエスト
				req.setAttribute(Const.MSG_ERROR, error_messages);
				
				// 遷移
				return LoginController.dispLogin(model, req, isUser);
			}
		}
	}
	
	/**
	 * 認証
	 */
	public String cert(String cert, Model model, HttpServletRequest req, boolean isUser) throws Exception {
		
		try {
			
			// 複合化
			String decryptCert = CommonUtil.decrypt(cert);
			
			// ID・メール取得
			String[] split = decryptCert.split("_",2);
			int id = Integer.parseInt(split[0]);
			String email = split[1];
			
			// データ
			Object obj;
			// ステータス、ID
			int status, objId;
			// メッセージパーツ
			String msgPart;
			
			// ユーザーである場合
			if(isUser) {
				// ユーザー
				User user = user_service.contain(email);
				// 変数設定
				objId = user.getId();
				status = user.getStatus();
				obj = user;
				msgPart = "ユーザー";
				
			// 店舗である場合
			}else {
				// 店舗
				Store store = store_service.contain(email);
				// 設定
				objId = store.getId();
				status = store.getStatus();
				obj = store;
				msgPart = "店舗";
			}
			
			// 認証済みである場合
			if(status == Const.USER_STATUS_CERT_END) {

				// エラーメッセージ
				List<String> error_messages = new ArrayList<String>();
				error_messages.add("既に認証が完了しているため、このURLは使用することができません。");
				// リクエスト
				req.setAttribute(Const.MSG_ERROR, error_messages);
				
				// エラー画面遷移
				return Const.PAGE_COMMON_CERT_ERROR;
				
			// IDが異なる場合はエラー
			}else if(objId != id) {

				// エラーメッセージ
				List<String> error_messages = new ArrayList<String>();
				error_messages.add("認証を正常に行うことができませんでした。登録したメールアドレスや、認証URLをご確認の上、再度"+msgPart+"登録を行っていただく、または認証URLへのアクセスを試みてください。");
				// リクエスト
				req.setAttribute(Const.MSG_ERROR, error_messages);
				
				// エラー画面遷移
				return Const.PAGE_COMMON_CERT_ERROR;
			
			// 正常である場合
			}else {
				
				// ユーザーである場合
				if(isUser) {
					// ユーザー
					User user = (User)obj;
					// ステータス変更
					user.setStatus(Const.USER_STATUS_CERT_END);
					// 保存
					user_service.save(user);
					
				// 店舗である場合
				}else {
					// 店舗
					Store store = (Store)obj;
					// ステータス変更
					store.setStatus(Const.USER_STATUS_CERT_END);
					// 保存
					store_service.save(store);
				}
				
				// メッセージ
				List<String> info_messages = new ArrayList<String>();
				info_messages.add("認証および"+msgPart+"の登録が完了しました。ログイン画面からログインすることができます。");
				// リクエスト
				req.setAttribute(Const.MSG_INFO, info_messages);

				// 遷移
				return Const.PAGE_COMMON_CERT_COMPLETE;
			}
			
		}catch(Exception e) {

			// エラーメッセージ
			List<String> error_messages = new ArrayList<String>();
			error_messages.add("認証を正常に行うことができませんでした。登録したメールアドレスや、認証URLをご確認の上、再度ユーザー登録または認証URLへのアクセスを試みてください。");
			// リクエスト
			req.setAttribute(Const.MSG_ERROR, error_messages);
			
			// エラー画面遷移
			return Const.PAGE_COMMON_CERT_ERROR;
		}
	}
}
