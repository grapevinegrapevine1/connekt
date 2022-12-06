package com.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.form.Store_readForm;
import com.properties.Const;
import com.util.CommonUtil;

@Controller
public class StoreReadController {
	
	/**
	 * 画面表示
	 */
	@RequestMapping("/store_read")
	public String disp(Model model, HttpServletRequest req, HttpSession ses) throws Exception {

		// セッション店舗チェック
		String sesErrFoward = CommonUtil.isSesStore(ses);
		if(sesErrFoward != null) return sesErrFoward;
		
		// モデル
		model.addAttribute("store_readForm", new Store_readForm());
		// 遷移
		return Const.PAGE_STORE_READ;
	}
}
