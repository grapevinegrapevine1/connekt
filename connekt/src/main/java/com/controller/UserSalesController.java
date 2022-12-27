package com.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.base.SalesController;
import com.form.StoreSalesForm;
import com.model.User;
import com.util.CommonUtil;

import jp.fintan.keel.spring.web.token.transaction.TransactionTokenCheck;

@Controller
@TransactionTokenCheck("UserSalesController")
public class UserSalesController{
	
	@Autowired private SalesController salesController;
	
	// 静的ファイル参照用
	@Autowired ResourceLoader resourceLoader;
	// 店舗処理可否
	private static final boolean isStore = false;
	
	/**
	 * 初期表示
	 */
	@RequestMapping("/user_sales")
	public String disp(Model model, HttpServletRequest req, HttpSession ses) throws Exception {
		
		// セッションユーザーチェック
		String sesErrFoward = CommonUtil.isSesUser(ses);
		if(sesErrFoward != null) return sesErrFoward;
		
		// セッションユーザー情報
		User user = CommonUtil.getSessionUser(ses);
		return salesController.disp(model, req, ses, isStore, user.getId());
	}
	
	/**
	 * 日付検索表示
	 */
	@RequestMapping("/user_sales_search")
	public String disp(@ModelAttribute @Validated StoreSalesForm storeSalesForm, BindingResult bindingResult, Model model, HttpServletRequest req, HttpSession ses) throws Exception {
		
		// セッションユーザーチェック
		String sesErrFoward = CommonUtil.isSesUser(ses);
		if(sesErrFoward != null) return sesErrFoward;
		
		// セッションユーザー情報
		User user = CommonUtil.getSessionUser(ses);
		return salesController.disp(storeSalesForm, bindingResult, model, req, ses, isStore, user.getId());
	}
	
	/**
	 * Ajax 追加一覧データ取得
	 */
	@RequestMapping("/user_sales_search_ajax")@ResponseBody
	public Object dispAjax_charge(@RequestBody @Validated StoreSalesForm storeSalesForm, BindingResult bindingResult, Model model, HttpServletRequest req, HttpSession ses) throws Exception {
		return salesController.dispAjax(false, true, storeSalesForm, bindingResult, model, req, ses);
	}
	@RequestMapping("/user_refund_search_ajax")@ResponseBody
	public Object dispAjax_refund(@RequestBody @Validated StoreSalesForm storeSalesForm, BindingResult bindingResult, Model model, HttpServletRequest req, HttpSession ses) throws Exception {
		return salesController.dispAjax(false, false, storeSalesForm, bindingResult, model, req, ses);
	}
	
	/**
	 * PDF出力
	 */
	@RequestMapping("/export_user_sales")
	public void export(@ModelAttribute @Validated StoreSalesForm storeSalesForm, BindingResult bindingResult, Model model, HttpServletRequest req, HttpSession ses,  HttpServletResponse res) throws Exception {
		salesController.export(storeSalesForm, bindingResult, model, req, ses, res, false);
	}
}
