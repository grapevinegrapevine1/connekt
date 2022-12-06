package com.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.properties.Const;
import com.service.StoreService;
import com.util.CommonUtil;

@Controller
public class StoreTopController {
	
	@Autowired StoreService storeService;
	
	@RequestMapping("/store_top")
	public String dispTop(Model model, HttpServletRequest req, HttpSession ses) throws Exception {
		return disp(model, req, ses);
	}
	@RequestMapping("/store_top_cert_err")
	public String dispCertErr(Model model, HttpServletRequest req, HttpSession ses) throws Exception {
		
		// エラーメッセージ
		List<String> error_messages = new ArrayList<String>();
		error_messages.add("認証キーが一致しないため、アクセスすることができませんでした。");
		// リクエスト
		req.setAttribute(Const.MSG_ERROR, error_messages);
		
		return disp(model, req, ses);
	}
	
	private String disp(Model model, HttpServletRequest req, HttpSession ses) throws Exception {

		// セッション店舗チェック
		String sesErrFoward = CommonUtil.isSesStore(ses);
		if(sesErrFoward != null) return sesErrFoward;
		
		// リクエスト設定
		CommonUtil.setReqSessionStore(req, ses);
		// 遷移
		return Const.PAGE_STORE_TOP;
	}
}
