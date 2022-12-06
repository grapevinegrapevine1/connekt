package com.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.base.StoreEditController;
import com.model.Option;
import com.service.OptionService;
import com.service.User_optionService;
import com.util.CommonUtil;
import com.util.MailUtil;
import com.util.NewsUtil;
import com.util.StripeUtil;

import jp.fintan.keel.spring.web.token.transaction.TransactionTokenCheck;
import jp.fintan.keel.spring.web.token.transaction.TransactionTokenType;

@Controller
@TransactionTokenCheck("StoreOptionEditController")
public class StoreOptionEditController extends BaseStoreController{
	
	@Autowired private OptionService optionService;
	@Autowired private User_optionService user_optionService;
	@Autowired private StripeUtil stripeUtil;
	@Autowired private MailUtil mailUtil;
	@Autowired private NewsUtil newsUtil;

	@TransactionTokenCheck(type = TransactionTokenType.BEGIN)
	@RequestMapping("/store_option_edit")
	public String disp(Model model, HttpServletRequest req, HttpSession ses) throws Exception {

		// セッション店舗チェック
		String sesErrFoward = CommonUtil.isSesStore(ses);
		if(sesErrFoward != null) return sesErrFoward;
		
		return new StoreEditController("option", stripeUtil, mailUtil, newsUtil, optionService, user_optionService).disp(model, req, ses, false);
	}

	@TransactionTokenCheck
	@Transactional
	@PostMapping("/save_option")
	public String update(@ModelAttribute @Validated Option option, BindingResult bindingResult, Model model, HttpServletRequest req, HttpSession ses) throws Exception {

		// セッション店舗チェック
		String sesErrFoward = CommonUtil.isSesStore(ses);
		if(sesErrFoward != null) return sesErrFoward;
		
		return new StoreEditController("option", stripeUtil, mailUtil, newsUtil, optionService, user_optionService).update(option, bindingResult, model, req, ses, false);
	}

	@TransactionTokenCheck
	@Transactional
	@PostMapping("/delete_option")
	public String delete(@ModelAttribute @Validated Option option, BindingResult bindingResult, Model model, HttpServletRequest req, HttpSession ses) throws Exception {

		// セッション店舗チェック
		String sesErrFoward = CommonUtil.isSesStore(ses);
		if(sesErrFoward != null) return sesErrFoward;
		
		return new StoreEditController("option", stripeUtil, mailUtil, newsUtil, optionService, user_optionService).delete(option, bindingResult, model, req, ses, false);
	}
}
