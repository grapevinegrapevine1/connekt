package com.security;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.model.Store;
import com.model.User;
import com.properties.Const;
import com.util.CommonUtil;

@Controller
public class LoginController {

	/**
	 * ログイン画面表示
	 */
	@GetMapping("/login")
	public String login(Model model, HttpServletRequest req, HttpSession ses) throws Exception {
		
		// ログインフォーム
		model.addAttribute("user", new User());
		// セッション店舗情報削除
		CommonUtil.removeSessionStore(ses);
		// セッションユーザー情報削除
		CommonUtil.removeSessionUser(ses);
		
		// 遷移
		return Const.PAGE_LOGIN;
	}
	
	/**
	 * ログイン画面表示
	 */
	public static String dispLogin(Model model, HttpServletRequest req, boolean isUser) throws Exception {
		
		// ログインフォーム
		model.addAttribute("user", new User());
		model.addAttribute(Const.IS_USER_FORM_NM, isUser ? Const.TAB_USER : Const.TAB_STORE);
		// 遷移
		return Const.PAGE_LOGIN;
	}
	
	/**
	 * ログイン遷移
	 */
	@RequestMapping("/login_foward")
	public String foward(Model model, HttpServletRequest req, HttpSession ses) throws Exception {
		
		// セッションユーザー情報
		User sesUser = CommonUtil.getSessionUser(ses);
		// セッション店舗情報
		Store sesStore = CommonUtil.getSessionStore(ses);
		
		// ユーザーフォームである場合
		if(sesUser != null) {
			
			// カード登録が完了していない場合はエラー画面表示
			if(sesUser.getStatus() == Const.USER_STATUS_SET_CARD) return Const.REDIRECT_HEADER + "login_card_error";
			// 認証が行われていない場合は認証エラー画面表示
			else if(sesUser.getStatus() == Const.USER_STATUS_CERT_START) return Const.REDIRECT_HEADER + "login_cert_error";
			// 入力エラーである場合はエラー遷移
			else if( isValid(sesUser.getEmail()) ) return Const.REDIRECT_HEADER + "login_error";
			// ユーザートップ画面
			else return Const.REDIRECT_HEADER + Const.PAGE_USER_TOP;
			
		// 店舗用フォームである場合
		}else if(sesStore != null){
			
			// 認証が行われていない場合は認証エラー画面表示
			if(sesStore.getStatus() == Const.USER_STATUS_CERT_START) return Const.REDIRECT_HEADER + "login_cert_error";
			// 入力エラーである場合はエラー遷移
			else if( isValid(sesStore.getEmail()) ) return Const.REDIRECT_HEADER + "login_error";
			// 店舗トップ画面
			else return Const.REDIRECT_HEADER +  Const.PAGE_STORE_TOP;
		
		// セッションが存在しない場合
		}else {
			// ログイン画面
			return Const.REDIRECT_HEADER + Const.PAGE_LOGIN;
		}
	}
	
	/**
	 * 入力エラーチェック
	 * @return true:エラーあり / false:エラーなし
	 */
	private boolean isValid(String email) {
		
		// メールアドレスの先頭に削除済み文字列が存在するかをチェック
		Pattern p = Pattern.compile("^" + Const.USER_DELETED_EMAIL);
		Matcher m = p.matcher(email);
		// 削除済みメールアドレスに一致する場合はエラー
		return m.find();
	}
	
/* -------------------------------------------------------------------------------------------------------------- */
	
	@RequestMapping("/login_created")
	public String loginCreated(Model model, HttpServletRequest req, HttpSession ses) throws Exception {
		// エラーメッセージ
		setLoginErrReq(model, req, ses, "ログインIDまたはパスワードが間違えています。");
		// 遷移
		return Const.PAGE_LOGIN;
	}
	
	/**
	 * ログインエラー
	 */
	@RequestMapping("/login_error")
	public String loginError(Model model, HttpServletRequest req, HttpSession ses) throws Exception {
		// エラーメッセージ
		setLoginErrReq(model, req, ses, "ログインIDまたはパスワードが間違えています。");
		// 遷移
		return Const.PAGE_LOGIN;
	}
	@RequestMapping("/login_card_error")
	public String loginCardError(Model model, HttpServletRequest req, HttpSession ses) throws Exception {
		// エラーメッセージ
		setLoginErrReq(model, req, ses, "カードの登録時に失敗しているようです。お手数ですがユーザーの新規登録を行って頂きますようお願い致します。");
		// 遷移
		return Const.PAGE_LOGIN;
	}
	
	/**
	 * 認証エラー
	 */
	@RequestMapping("/login_cert_error")
	public String loginCertError(Model model, HttpServletRequest req, HttpSession ses) throws Exception {
		// エラーメッセージ
		setLoginErrReq(model, req, ses, "認証が完了していないためログインすることができません。登録したメールアドレス宛てに送信した認証メールをご確認の上、認証を完了させた後にログインを試みてください。");
		// 遷移
		return Const.PAGE_LOGIN;
	}
	
	/**
	 * セッションエラー
	 */
	@RequestMapping("/session_error")
	public String sessionError(Model model, HttpServletRequest req, HttpSession ses) throws Exception {
		// エラーメッセージ
		setLoginErrReq(model, req, ses, "ログイン情報が存在しないため自動でログアウトしました。");
		// 遷移
		return Const.PAGE_LOGIN;
	}
	
	/**
	 * 店舗削除後
	 */
	@RequestMapping("/deleted_store")
	public String deleteStore(Model model, HttpServletRequest req, HttpSession ses) throws Exception {
		// エラーメッセージ
		setLoginInfoReq(model, req, ses, "店舗が削除されました。ご利用ありがとうございました。");
		// 遷移
		return Const.PAGE_LOGIN;
	}
	
	/**
	 * パスワード忘れメール送信後
	 */
	@RequestMapping("/forget_send_user")
	public String forgetSend_user(Model model, HttpServletRequest req, HttpSession ses) throws Exception {
		return forgetSend(model, req, ses, true);
	}
	@RequestMapping("/forget_send_store")
	public String forgetSend_store(Model model, HttpServletRequest req, HttpSession ses) throws Exception {
		return forgetSend(model, req, ses, false);
	}
	private String forgetSend(Model model, HttpServletRequest req, HttpSession ses, boolean isUser) throws Exception {
		// エラーメッセージ
		setLoginInfoReq(model, req, ses, "入力されたメールアドレスへメールを送信しましたのでご確認ください。また、メールが届かない場合は迷惑メールボックスに転送されていないかをご確認ください。");
		model.addAttribute("user", new User());
		model.addAttribute(Const.IS_USER_FORM_NM, isUser ? Const.TAB_USER : Const.TAB_STORE);
		// 遷移
		return Const.PAGE_LOGIN;
	}
	
	/**
	 * ログインエラー時リクエスト設定
	 */
	private void setLoginErrReq(Model model, HttpServletRequest req, HttpSession ses, String errMsg) {
		setLoginMsgReq(model, req, ses, Const.MSG_ERROR, errMsg);
	}
	private void setLoginInfoReq(Model model, HttpServletRequest req, HttpSession ses, String errMsg) {
		setLoginMsgReq(model, req, ses, Const.MSG_INFO, errMsg);
	}
	private void setLoginMsgReq(Model model, HttpServletRequest req, HttpSession ses, String type, String msg) {

		// ログインフォーム設定
		setLoginAttr(model, ses);
		
		// エラーメッセージ
		List<String> messages = new ArrayList<String>();
		// エラーメッセージ
		messages.add(msg);
		// リクエスト
		req.setAttribute(type, messages);
	}
	/**
	 * ログイン時リクエスト設定
	 */
	private void setLoginAttr(Model model, HttpSession ses) {
		
		// ログインフォーム
		model.addAttribute("user", new User());
		model.addAttribute(Const.IS_USER_FORM_NM, ses.getAttribute(Const.IS_USER_FORM_NM));
		
		// セッション店舗情報削除
		CommonUtil.removeSessionStore(ses);
		// セッションユーザー情報削除
		CommonUtil.removeSessionUser(ses);
	}
}
