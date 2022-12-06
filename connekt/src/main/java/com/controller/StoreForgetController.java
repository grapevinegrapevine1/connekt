package com.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.base.ForgetController;
import com.form.ForgetForm;

import jp.fintan.keel.spring.web.token.transaction.TransactionTokenCheck;
import jp.fintan.keel.spring.web.token.transaction.TransactionTokenType;

@Controller
@TransactionTokenCheck("StoreForgetController")
public class StoreForgetController extends ForgetController{
	
	@TransactionTokenCheck(type = TransactionTokenType.BEGIN)
	@RequestMapping("/store_forget")
	public String disp(Model model, HttpServletRequest req, HttpSession ses) throws Exception {
		return super.disp(false, model, req);
	}

	@TransactionTokenCheck
	@Transactional
	@RequestMapping("/store_forget_request")
	public String request_forget(@ModelAttribute @Validated ForgetForm user_forgetForm, BindingResult bindingResult, Model model, HttpServletRequest req, HttpSession ses) throws Exception {
		return super.request_forget(user_forgetForm, bindingResult, false, model, req);
	}
}
