package com.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.model.User;
import com.properties.Const;
import com.service.StoreService;
import com.service.UserService;
import com.util.CommonUtil;

@Controller
public class StoreTopController {
	
	@Autowired StoreService storeService;
	@Autowired UserService userService;
	
	@RequestMapping("/store_top")
	public String dispTop(Model model, HttpServletRequest req, HttpSession ses) throws Exception {
		return disp(model, req, ses);
	}
	/**
	 * 設定画面アクセス時メール認証エラー
	 */
	@RequestMapping("/store_top_cert_err")
	public String dispCertErr(Model model, HttpServletRequest req, HttpSession ses) throws Exception {
		
		// エラーメッセージ
		List<String> error_messages = new ArrayList<String>();
		error_messages.add("認証キーが一致しないため、アクセスすることができませんでした。");
		// リクエスト
		req.setAttribute(Const.MSG_ERROR, error_messages);
		
		return disp(model, req, ses);
	}
	
	/**
	 * 画面表示
	 */
	private String disp(Model model, HttpServletRequest req, HttpSession ses) throws Exception {

		// セッション店舗チェック
		String sesErrFoward = CommonUtil.isSesStore(ses);
		if(sesErrFoward != null) return sesErrFoward;
		
		// リクエスト設定
		CommonUtil.setReqSessionStore(req, ses);
		// ログイン時セッション店舗ID設定
		setReqLoginUserId(req, ses);
		
		// 遷移
		return Const.PAGE_STORE_TOP;
	}
	/**
	 * ログイン時セッションユーザーID設定
	 */
	private void setReqLoginUserId(HttpServletRequest req, HttpSession ses) {
		
		// ログイン時セッション店舗ID
		Object login_user_id = ses.getAttribute(Const.LOGIN_USER_ID);
		// ログイン時セッション店舗IDキー削除
		ses.removeAttribute(Const.LOGIN_USER_ID);
		// リクエスト設定
		if(login_user_id != null && !login_user_id.toString().equals("")) {
			// 店舗
			User loginUser = userService.find(Integer.parseInt(login_user_id.toString()));
			// リクエスト設定
			req.setAttribute(Const.LOGIN_USER_ID, login_user_id + "_" + loginUser.getName());
		}
	}
}
