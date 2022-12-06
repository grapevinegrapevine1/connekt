package com.base;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import com.form.ForgetForm;
import com.model.Store;
import com.model.User;
import com.properties.Const;
import com.service.StoreService;
import com.service.UserService;
import com.util.CommonUtil;
import com.util.MailUtil;

@Controller
public class ForgetController {

	@Autowired UserService user_service;
	@Autowired StoreService store_service;
	@Autowired MailUtil mailUtil;

	protected String disp(boolean isUser, Model model, HttpServletRequest req) throws Exception {

		// モデル
		model.addAttribute("forgetForm", new ForgetForm());
		model.addAttribute("is_user", isUser);
		// 遷移
		return Const.PAGE_COMMON_FORGET;
	}

	protected String request_forget(ForgetForm forgetForm, BindingResult bindingResult, boolean isUser, Model model, HttpServletRequest req) throws Exception {

		// Validationエラーメッセージ取得
		String validMsg = CommonUtil.isValid(bindingResult,req);
		// Validationエラーが存在する場合はエラー画面遷移
		if(validMsg != null) {
			req.setAttribute("page_forget", true);
			return isUser ? Const.PAGE_USER_VALID_ERROR : Const.PAGE_STORE_VALID_ERROR;
		}
		
		// ユーザー検索
		Object obj = isUser ? user_service.containByEmailAndName(forgetForm.getEmail(), forgetForm.getName()):
							  store_service.containByEmailAndName(forgetForm.getEmail(), forgetForm.getName());
		
		// ユーザーが存在する場合
		if(obj != null) {
			
			// 新パスワード
			String newPw = getRandomString(15);
			// ユーザーである場合
			if(isUser) {
				
				// ユーザー
				User user = (User)obj;
				// 新パスワード
				user.setPassword(newPw);
				// メール送信
				mailUtil.sendForgetMail(user.getEmail() ,user.getPassword());
				// ユーザー情報保存
				user_service.save(user);
				
			// 店舗である場合
			}else {
				
				// ユーザー
				Store store = (Store)obj;
				// 新パスワード
				store.setPassword(newPw);
				// メール送信
				mailUtil.sendForgetMail(store.getEmail() ,store.getPassword());
				// ユーザー情報保存
				store_service.save(store);
			}
			
			// ログイン画面遷移
			return Const.REDIRECT_HEADER + (isUser ? Const.PAGE_USER_FORGET_SEND : Const.PAGE_STORE_FORGET_SEND);
			
		// ユーザーが存在しない場合
		}else {

			// エラーメッセージ
			List<String> error_messages = new ArrayList<String>();
			error_messages.add("入力されたメールアドレスと氏名に合致するユーザー情報が見当たりません。入力内容を再度ご確認の上、送信ボタンを押してください。");
			// リクエスト
			req.setAttribute(Const.MSG_ERROR, error_messages);
			
			// モデル
			model.addAttribute("is_user", isUser);
			//遷移
			return Const.PAGE_COMMON_FORGET;
		}
	}
	
	/**
	 * ランダム文字列生成
	 * @param i 桁数
	 * @return ランダム文字列
	 */
	private static String getRandomString(int i) {

		byte[] bytearray;
		String mystring;
		StringBuffer thebuffer;

		bytearray = new byte[256];
		new Random().nextBytes(bytearray);

		mystring = new String(bytearray, Charset.forName("UTF-8"));

		// Create the StringBuffer
		thebuffer = new StringBuffer();

		for (int m = 0; m < mystring.length(); m++) {
			char n = mystring.charAt(m);
			if (((n >= 'A' && n <= 'Z') || (n >= '0' && n <= '9')) && (i > 0)) {
				thebuffer.append(n);
				i--;
			}
		}

		// resulting string
		return thebuffer.toString();
	}
}
