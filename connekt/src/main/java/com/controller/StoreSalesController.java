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
import org.springframework.web.bind.annotation.RequestMapping;

import com.base.SalesController;
import com.form.StoreSalesForm;
import com.model.Store;
import com.util.CommonUtil;

import jp.fintan.keel.spring.web.token.transaction.TransactionTokenCheck;

@Controller
@TransactionTokenCheck("StoreSalesController")
public class StoreSalesController{
	
	@Autowired private SalesController salesController;
	
	// 静的ファイル参照用
	@Autowired ResourceLoader resourceLoader;
	// 店舗処理可否
	private static final boolean isStore = true;
	
	/**
	 * 初期表示
	 */
	@RequestMapping("/store_sales")
	public String disp(Model model, HttpServletRequest req, HttpSession ses) throws Exception {
		
		// セッション店舗チェック
		String sesErrFoward = CommonUtil.isSesStore(ses);
		if(sesErrFoward != null) return sesErrFoward;
		
		// セッション店舗情報
		Store store = CommonUtil.getSessionStore(ses);
		return salesController.disp(model, req, ses, isStore, store.getId());
	}
	
	/**
	 * 日付検索表示
	 */
	@RequestMapping("/store_sales_search")
	public String disp(@ModelAttribute @Validated StoreSalesForm storeSalesForm, BindingResult bindingResult, Model model, HttpServletRequest req, HttpSession ses) throws Exception {
		
		// セッション店舗チェック
		String sesErrFoward = CommonUtil.isSesStore(ses);
		if(sesErrFoward != null) return sesErrFoward;
		
		// セッション店舗情報
		Store store = CommonUtil.getSessionStore(ses);
		return salesController.disp(storeSalesForm, bindingResult, model, req, ses, isStore, store.getId());
	}
	
	/**
	 * PDF出力
	 */
	@RequestMapping("/export_store_sales")
	public void export(@ModelAttribute @Validated StoreSalesForm storeSalesForm, BindingResult bindingResult, Model model, HttpServletRequest req, HttpSession ses,  HttpServletResponse res) throws Exception {
		salesController.export(storeSalesForm, bindingResult, model, req, ses, res, true);
	}
}
